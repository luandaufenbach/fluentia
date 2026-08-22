package br.com.agenteingles.seguranca;

import br.com.agenteingles.usuario.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/** Cadastro, entrada e saida. */
@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

    /**
     * Comprimento minimo da senha. A NIST 800-63B recomenda exigir tamanho em vez de
     * composicao obrigatoria de caracteres: regra de "uma maiuscula e um simbolo"
     * produz Senha@123, que esta em qualquer dicionario de ataque, enquanto uma frase
     * longa e facil de lembrar e cara de quebrar.
     */
    private static final int TAMANHO_MINIMO_DA_SENHA = 10;

    private static final int TAMANHO_MAXIMO_DA_SENHA = 128;

    private final ServicoDeAutenticacao servicoDeAutenticacao;
    private final SessoesAtivas sessoesAtivas;
    private final SecurityContextRepository repositorioDeContexto = new HttpSessionSecurityContextRepository();

    public AutenticacaoController(ServicoDeAutenticacao servicoDeAutenticacao,
                                  SessoesAtivas sessoesAtivas) {
        this.servicoDeAutenticacao = servicoDeAutenticacao;
        this.sessoesAtivas = sessoesAtivas;
    }

    public record CadastroRequisicao(
            @NotBlank(message = "Informe o seu nome.")
            @Size(max = 120, message = "Nome muito longo.")
            String nome,

            @NotBlank(message = "Informe o e-mail.")
            @Email(message = "E-mail invalido.")
            @Size(max = 180, message = "E-mail muito longo.")
            String email,

            // O maximo existe para nao aceitar entrada gigante: o BCrypt e caro por
            // construcao, e senha sem limite vira vetor de exaustao de CPU.
            @NotBlank(message = "Informe a senha.")
            @Size(min = TAMANHO_MINIMO_DA_SENHA, max = TAMANHO_MAXIMO_DA_SENHA,
                    message = "A senha precisa ter no minimo " + TAMANHO_MINIMO_DA_SENHA + " caracteres.")
            String senha) {
    }

    public record LoginRequisicao(
            @NotBlank(message = "Informe o e-mail.")
            @Size(max = 180)
            String email,

            @NotBlank(message = "Informe a senha.")
            @Size(max = TAMANHO_MAXIMO_DA_SENHA)
            String senha) {
    }

    /** Nunca inclui hash, papel interno nem estado de bloqueio. */
    public record UsuarioAutenticadoResposta(Long id, String nome, String email) {
        static UsuarioAutenticadoResposta de(Usuario usuario) {
            return new UsuarioAutenticadoResposta(usuario.getId(), usuario.getNome(), usuario.getEmail());
        }
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAutenticadoResposta cadastrar(@Valid @RequestBody CadastroRequisicao requisicao,
                                                HttpServletRequest http,
                                                HttpServletResponse resposta) {
        Usuario usuario = servicoDeAutenticacao.cadastrar(
                requisicao.nome(), requisicao.email(), requisicao.senha(), origemDe(http));

        // Ja entra logado: obrigar um login logo depois do cadastro nao acrescenta
        // seguranca nenhuma — a credencial acabou de ser provada.
        abrirSessao(usuario, http, resposta);
        return UsuarioAutenticadoResposta.de(usuario);
    }

    @PostMapping("/login")
    public UsuarioAutenticadoResposta entrar(@Valid @RequestBody LoginRequisicao requisicao,
                                             HttpServletRequest http,
                                             HttpServletResponse resposta) {
        Usuario usuario = servicoDeAutenticacao.autenticar(
                requisicao.email(), requisicao.senha(), origemDe(http));

        abrirSessao(usuario, http, resposta);
        return UsuarioAutenticadoResposta.de(usuario);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(HttpServletRequest http) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null) {
            servicoDeAutenticacao.registrarSaida(autenticacao.getName(), origemDe(http));
        }

        // Invalida no servidor, e nao apenas limpa o cookie: sessao invalidada no
        // cliente mas viva no servidor continua servindo para quem tiver o cookie.
        HttpSession sessao = http.getSession(false);
        if (sessao != null) {
            if (autenticacao != null) {
                sessoesAtivas.esquecer(autenticacao.getName(), sessao);
            }
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    private void abrirSessao(Usuario usuario, HttpServletRequest http, HttpServletResponse resposta) {
        // Sessao nova a cada login: o identificador anterior deixa de valer, o que
        // fecha a porta para fixacao de sessao.
        HttpSession anterior = http.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }

        // Derruba as sessoes que esta conta ja tinha em outros dispositivos. Feito a
        // mao porque a estrategia de concorrencia do Spring roda no filtro de
        // autenticacao, e esta autenticacao nao passa por la — configurar
        // maximumSessions e esperar que funcione sozinho nao funciona aqui.
        Authentication autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                usuario.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(usuario.getPapel().autoridade())));

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        repositorioDeContexto.saveContext(contexto, http, resposta);

        HttpSession nova = http.getSession(false);
        if (nova != null) {
            // Derruba as sessoes que esta conta tinha em outros dispositivos. A sessao
            // recem-criada e poupada pelo identificador.
            sessoesAtivas.derrubarSessoesDe(usuario.getEmail(), nova.getId());
            sessoesAtivas.registrar(usuario.getEmail(), nova);
        }
    }

    /**
     * Origem da requisicao para a trilha de auditoria.
     *
     * <p>Usa o endereco da conexao e ignora {@code X-Forwarded-For}: esse cabecalho e
     * escrito pelo cliente e pode ser forjado, o que envenenaria a auditoria e o
     * limite por origem. Atras de proxy reverso, a traducao correta e feita por
     * {@code ForwardedHeaderFilter} com a lista de proxies confiaveis configurada.
     */
    private String origemDe(HttpServletRequest http) {
        String endereco = http.getRemoteAddr();
        return endereco == null || endereco.length() > 45 ? null : endereco;
    }
}
