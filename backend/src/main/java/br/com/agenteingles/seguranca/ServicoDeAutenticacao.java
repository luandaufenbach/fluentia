package br.com.agenteingles.seguranca;

import br.com.agenteingles.usuario.Usuario;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.time.LocalDateTime;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro e verificacao de credencial.
 *
 * <p>Quatro cuidados sustentam esta classe:
 *
 * <ol>
 *   <li><b>Nao revelar quais contas existem.</b> Conta inexistente, conta inativa e
 *       senha errada devolvem exatamente a mesma resposta. Distinguir os casos
 *       entrega ao atacante uma lista de e-mails validos de graca.</li>
 *   <li><b>Nao vazar pelo tempo de resposta.</b> Quando a conta nao existe, um hash
 *       descartavel e verificado assim mesmo. Sem isso, "conta inexistente"
 *       responderia em microssegundos e "senha errada" em centenas de milissegundos,
 *       e a diferenca entregaria a mesma lista.</li>
 *   <li><b>O que registra a falha nao pode ser desfeito pela falha.</b> Contador e
 *       auditoria vao para {@link RegistroDeSeguranca}, em transacao propria: aqui a
 *       recusa lanca excecao, e excecao desfaz a transacao corrente.</li>
 *   <li><b>Bloqueio temporario, nunca permanente.</b> Bloqueio definitivo transforma
 *       tentativa de invasao em negacao de servico contra o dono da conta.</li>
 * </ol>
 */
@Service
public class ServicoDeAutenticacao {

    private static final Logger log = LoggerFactory.getLogger(ServicoDeAutenticacao.class);

    /**
     * Hash descartavel usado quando a conta nao existe, para o custo da verificacao
     * ser o mesmo dos dois lados. E o hash de um valor aleatorio: nao ha senha que
     * bata com ele.
     */
    private static final String HASH_FALSO =
            "{bcrypt}$2a$12$C6UzMDM.H6dfI/f/IKcEe.6i9pQhVXqvE3sWjnKgs8Y8g/L9Yt3Ke";

    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroDeSeguranca registro;
    private final PasswordEncoder codificador;
    private final PropriedadesDeSeguranca propriedades;

    public ServicoDeAutenticacao(UsuarioRepositorio usuarioRepositorio,
                                 RegistroDeSeguranca registro,
                                 PasswordEncoder codificador,
                                 PropriedadesDeSeguranca propriedades) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.registro = registro;
        this.codificador = codificador;
        this.propriedades = propriedades;
    }

    /**
     * Cria a conta.
     *
     * @throws EmailJaCadastradoException quando o e-mail ja existe. Aqui a distincao e
     *         inevitavel — nao da para cadastrar duas contas com o mesmo e-mail
     */
    @Transactional
    public Usuario cadastrar(String nome, String email, String senha, String origem) {
        String emailNormalizado = normalizar(email);

        if (usuarioRepositorio.existeComEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }

        Usuario salvo = usuarioRepositorio.save(
                new Usuario(nome.trim(), emailNormalizado, codificador.encode(senha)));

        registro.registrarEventoJunto(salvo.getId(), emailNormalizado,
                TipoDeEventoDeAutenticacao.CADASTRO, origem, null);
        log.info("Conta criada para o usuario {}", salvo.getId());
        return salvo;
    }

    /**
     * Verifica a credencial e devolve o usuario autenticado.
     *
     * <p>Deliberadamente <b>sem</b> {@code @Transactional}: cada escrita aqui acontece
     * em transacao propria, porque o caminho de recusa termina em excecao e desfaria
     * tudo que tivesse sido gravado no mesmo escopo — inclusive a prova de que a
     * tentativa aconteceu.
     *
     * @throws CredencialInvalidaException para qualquer motivo de recusa — a mensagem
     *         e sempre a mesma, de proposito
     * @throws ContaBloqueadaException quando ha bloqueio temporario em vigor. Este caso
     *         precisa ser distinguido para o dono da conta saber por que nao entra
     */
    public Usuario autenticar(String email, String senha, String origem) {
        String emailNormalizado = normalizar(email);
        LocalDateTime agora = LocalDateTime.now();

        Usuario usuario = buscar(emailNormalizado);

        if (usuario == null || !usuario.podeAutenticar()) {
            // Verifica assim mesmo: o tempo de resposta precisa ser igual ao do caminho
            // em que a conta existe, senao ele denuncia quais e-mails estao cadastrados.
            codificador.matches(senha, HASH_FALSO);
            registro.registrarEvento(usuario == null ? null : usuario.getId(), emailNormalizado,
                    TipoDeEventoDeAutenticacao.LOGIN_RECUSADO, origem, "conta inexistente ou inativa");
            throw new CredencialInvalidaException();
        }

        if (usuario.estaBloqueado(agora)) {
            registro.registrarEvento(usuario.getId(), emailNormalizado,
                    TipoDeEventoDeAutenticacao.CONTA_BLOQUEADA, origem, "tentativa durante bloqueio");
            throw new ContaBloqueadaException();
        }

        if (!codificador.matches(senha, usuario.getSenhaHashParaVerificacao())) {
            boolean bloqueou = registro.registrarFalhaEVerificarBloqueio(
                    usuario.getId(),
                    propriedades.tentativasAteBloquear(),
                    agora.plusMinutes(propriedades.minutosDeBloqueio()));

            registro.registrarEvento(usuario.getId(), emailNormalizado,
                    TipoDeEventoDeAutenticacao.LOGIN_RECUSADO, origem, "senha incorreta");

            if (bloqueou) {
                log.warn("Conta {} bloqueada por tentativas falhas seguidas", usuario.getId());
                registro.registrarEvento(usuario.getId(), emailNormalizado,
                        TipoDeEventoDeAutenticacao.CONTA_BLOQUEADA, origem, "limite de tentativas atingido");
                throw new ContaBloqueadaException();
            }
            throw new CredencialInvalidaException();
        }

        registro.registrarAcessoBemSucedido(usuario.getId(), agora);
        registro.registrarEvento(usuario.getId(), emailNormalizado,
                TipoDeEventoDeAutenticacao.LOGIN_COM_SUCESSO, origem, null);
        return usuario;
    }

    public void registrarSaida(String email, String origem) {
        registro.registrarEvento(null, normalizar(email),
                TipoDeEventoDeAutenticacao.LOGOUT, origem, null);
    }

    @Transactional(readOnly = true)
    protected Usuario buscar(String email) {
        return usuarioRepositorio.buscarPorEmail(email).orElse(null);
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Recusa generica: o motivo real fica na trilha de auditoria, nao na resposta. */
    public static class CredencialInvalidaException extends RuntimeException {
        public CredencialInvalidaException() {
            super("E-mail ou senha incorretos.");
        }
    }

    public static class ContaBloqueadaException extends RuntimeException {
        public ContaBloqueadaException() {
            super("Conta temporariamente bloqueada por tentativas seguidas. Tente novamente mais tarde.");
        }
    }

    public static class EmailJaCadastradoException extends RuntimeException {
        public EmailJaCadastradoException() {
            super("Este e-mail ja esta cadastrado.");
        }
    }
}
