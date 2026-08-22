package br.com.agenteingles;

import br.com.agenteingles.usuario.Usuario;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Conta usada pelos testes, criada pelo mesmo caminho de uma conta real.
 *
 * <p>O usuario semeado nas migrations deixou de servir de proposito: ele nasce inativo
 * e sem senha, justamente para nao existir credencial conhecida em quem clonar o
 * repositorio. Testar autenticacao com uma conta que burla a autenticacao nao testaria
 * nada — entao aqui a conta e criada de verdade, com hash de verdade.
 */
@Component
public class ContaDeTeste {

    public static final String EMAIL = "aluno.de.teste@fluentia.local";
    public static final String SENHA = "senha-longa-de-teste-2026";

    /** Segunda conta, para provar que uma nao enxerga os dados da outra. */
    public static final String EMAIL_DO_OUTRO = "outro.aluno@fluentia.local";
    public static final String SENHA_DO_OUTRO = "outra-senha-longa-2026";

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder codificador;

    public ContaDeTeste(UsuarioRepositorio usuarioRepositorio, PasswordEncoder codificador) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.codificador = codificador;
    }

    /**
     * Garante a conta no banco e devolve o usuario.
     *
     * <p>{@code REQUIRES_NEW} para a conta sobreviver ao rollback da transacao do
     * teste: sem isso, o teste seguinte nao a encontraria.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario garantirQueExiste() {
        return garantir("Aluno de Teste", EMAIL, SENHA);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario garantirQueOOutroExiste() {
        return garantir("Outro Aluno", EMAIL_DO_OUTRO, SENHA_DO_OUTRO);
    }

    /** Apaga e recria: bloqueio ou tentativa de um teste nao pode vazar para o seguinte. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario recriar() {
        usuarioRepositorio.buscarPorEmail(EMAIL).ifPresent(usuarioRepositorio::delete);
        return garantirQueExiste();
    }

    private Usuario garantir(String nome, String email, String senha) {
        return usuarioRepositorio.buscarPorEmail(email).orElseGet(() ->
                usuarioRepositorio.save(new Usuario(nome, email, codificador.encode(senha))));
    }

    /** Coloca a conta no contexto de seguranca, como um login faria. */
    public Usuario autenticar() {
        Usuario usuario = garantirQueExiste();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        usuario.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority(usuario.getPapel().autoridade()))));
        return usuario;
    }

    public void limparContexto() {
        SecurityContextHolder.clearContext();
    }
}
