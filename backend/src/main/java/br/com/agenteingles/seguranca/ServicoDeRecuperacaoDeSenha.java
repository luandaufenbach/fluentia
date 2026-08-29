package br.com.agenteingles.seguranca;

import br.com.agenteingles.usuario.Usuario;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recuperacao de senha por e-mail.
 *
 * <p>Cinco decisoes sustentam esta classe:
 *
 * <ol>
 *   <li><b>A resposta e sempre a mesma.</b> E-mail com conta, sem conta ou com conta
 *       inativa recebem exatamente o mesmo retorno. Distinguir transformaria este
 *       endpoint numa lista de quem tem cadastro — publica e sem autenticacao.</li>
 *   <li><b>O token e aleatorio de verdade e so o hash e guardado.</b> 256 bits de
 *       {@link SecureRandom}; o banco recebe SHA-256. Vazar a tabela nao da acesso a
 *       conta nenhuma.</li>
 *   <li><b>Vale uma vez e por pouco tempo.</b> Prazo curto limita a janela de um e-mail
 *       vazado; uso unico impede reaproveitar o mesmo link depois.</li>
 *   <li><b>Pedir de novo queima o anterior.</b> Senao cada tentativa deixaria mais uma
 *       chave valida circulando na caixa de entrada.</li>
 *   <li><b>Redefinir derruba as sessoes.</b> Quem troca a senha por ter perdido o acesso
 *       precisa expulsar quem ja estava dentro — e o unico jeito de recuperar uma conta
 *       de fato invadida.</li>
 * </ol>
 */
@Service
public class ServicoDeRecuperacaoDeSenha {

    private static final Logger log = LoggerFactory.getLogger(ServicoDeRecuperacaoDeSenha.class);

    /** 32 bytes = 256 bits. Nao ha forca bruta contra isso dentro do prazo do link. */
    private static final int BYTES_DO_TOKEN = 32;

    private static final SecureRandom SORTEIO = new SecureRandom();

    private final UsuarioRepositorio usuarioRepositorio;
    private final RecuperacaoDeSenhaRepositorio recuperacaoRepositorio;
    private final RegistroDeSeguranca registro;
    private final SessoesAtivas sessoesAtivas;
    private final EnviadorDeEmail email;
    private final PasswordEncoder codificador;
    private final PropriedadesDeSeguranca propriedades;
    private final ServicoDeAutenticacao autenticacao;
    private final String urlBase;

    public ServicoDeRecuperacaoDeSenha(UsuarioRepositorio usuarioRepositorio,
                                       RecuperacaoDeSenhaRepositorio recuperacaoRepositorio,
                                       RegistroDeSeguranca registro,
                                       SessoesAtivas sessoesAtivas,
                                       EnviadorDeEmail email,
                                       PasswordEncoder codificador,
                                       PropriedadesDeSeguranca propriedades,
                                       ServicoDeAutenticacao autenticacao,
                                       @Value("${agente-ingles.url-base:http://localhost:5173}") String urlBase) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.recuperacaoRepositorio = recuperacaoRepositorio;
        this.registro = registro;
        this.sessoesAtivas = sessoesAtivas;
        this.email = email;
        this.codificador = codificador;
        this.propriedades = propriedades;
        this.autenticacao = autenticacao;
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    /**
     * Cria o pedido e manda o link, <b>se</b> a conta existir e puder autenticar.
     *
     * <p>Nao devolve nada e nao lanca quando a conta nao existe: quem chama responde
     * igual nos dois casos.
     */
    @Transactional
    public void solicitar(String emailInformado, String origem) {
        String normalizado = emailInformado.trim().toLowerCase(Locale.ROOT);
        LocalDateTime agora = LocalDateTime.now();

        // Reusa o limitador do cadastro e do login em vez de escrever um terceiro: logica
        // de seguranca duplicada e logica que diverge, e a copia esquecida vira o buraco.
        //
        // Sem teto, este endpoint seria uma maquina de mandar e-mail para endereco alheio:
        // basta repetir o pedido com o e-mail de outra pessoa para encher a caixa dela, e
        // quem paga a conta de reputacao do remetente e o dono do app.
        autenticacao.exigirDentroDoLimite(origem, normalizado,
                TipoDeEventoDeAutenticacao.RECUPERACAO_PEDIDA,
                propriedades.recuperacoesPorOrigemPorHora(),
                "Muitos pedidos a partir deste dispositivo. Tente novamente mais tarde.");

        Optional<Usuario> talvez = usuarioRepositorio.buscarPorEmail(normalizado)
                .filter(Usuario::podeAutenticar);

        if (talvez.isEmpty()) {
            // Sem conta, sem e-mail — e sem contar isso para quem perguntou. Fica na
            // trilha de auditoria, que e onde a informacao serve para investigar.
            registro.registrarEvento(null, normalizado,
                    TipoDeEventoDeAutenticacao.RECUPERACAO_PEDIDA, origem, "conta inexistente ou inativa");
            log.info("Pedido de recuperacao para e-mail sem conta ativa");
            return;
        }

        Usuario usuario = talvez.get();
        recuperacaoRepositorio.invalidarPendentesDoUsuario(usuario.getId(), agora);

        String token = novoToken();
        recuperacaoRepositorio.save(new RecuperacaoDeSenha(
                usuario.getId(),
                resumir(token),
                agora.plusMinutes(propriedades.minutosDoLinkDeRecuperacao()),
                origem));

        registro.registrarEventoJunto(usuario.getId(), normalizado,
                TipoDeEventoDeAutenticacao.RECUPERACAO_PEDIDA, origem, null);

        email.enviar(usuario.getEmail(), "Redefinir a sua senha do Fluentia", montarCorpo(usuario, token));
    }

    /**
     * Troca a senha a partir do token.
     *
     * @throws LinkInvalidoException quando o token nao existe, ja foi usado ou venceu.
     *         Os tres casos dao a mesma mensagem: saber qual deles e nao ajuda quem
     *         perdeu a senha e ajuda quem esta sondando links
     */
    @Transactional
    public void redefinir(String token, String novaSenha, String origem) {
        LocalDateTime agora = LocalDateTime.now();

        RecuperacaoDeSenha pedido = recuperacaoRepositorio.findByTokenHash(resumir(token))
                .filter(candidato -> candidato.utilizavel(agora))
                .orElseThrow(LinkInvalidoException::new);

        Usuario usuario = usuarioRepositorio.findById(pedido.getUsuarioId())
                .filter(Usuario::podeAutenticar)
                .orElseThrow(LinkInvalidoException::new);

        usuario.trocarSenha(codificador.encode(novaSenha));
        usuarioRepositorio.save(usuario);
        pedido.marcarComoUsado(agora);

        // Quem redefine a senha por ter perdido o acesso precisa expulsar quem esta
        // dentro. Sem isto, um invasor com sessao aberta continuaria navegando depois
        // da troca — e a recuperacao teria falhado justamente no caso que importa.
        sessoesAtivas.derrubarSessoesDe(usuario.getEmail(), null);

        registro.registrarEventoJunto(usuario.getId(), usuario.getEmail(),
                TipoDeEventoDeAutenticacao.SENHA_TROCADA, origem, "por link de recuperacao");
        log.info("Senha redefinida para o usuario {}", usuario.getId());
    }

    private String novoToken() {
        byte[] bytes = new byte[BYTES_DO_TOKEN];
        SORTEIO.nextBytes(bytes);
        // Sem padding e seguro para URL: o token viaja na query string do link.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 do token, em hexadecimal. Ver o comentario da migration V13. */
    private String resumir(String token) {
        try {
            MessageDigest digestor = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digestor.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossivel) {
            throw new IllegalStateException("SHA-256 e obrigatorio em toda JVM", impossivel);
        }
    }

    private String montarCorpo(Usuario usuario, String token) {
        return """
                Olá, %s.

                Alguém pediu para redefinir a senha da sua conta no Fluentia. Se foi você,
                abra o endereço abaixo:

                %s/?recuperacao=%s

                O link vale por %d minutos e só pode ser usado uma vez.

                Se não foi você, ignore esta mensagem: a sua senha continua a mesma e
                ninguém consegue entrar sem abrir este link.
                """.formatted(
                usuario.getNome(),
                urlBase,
                token,
                propriedades.minutosDoLinkDeRecuperacao());
    }

    /** Token inexistente, vencido ou ja usado — a mensagem nao distingue os tres. */
    public static class LinkInvalidoException extends RuntimeException {
        public LinkInvalidoException() {
            super("Este link de recuperação não vale mais. Peça um novo.");
        }
    }
}
