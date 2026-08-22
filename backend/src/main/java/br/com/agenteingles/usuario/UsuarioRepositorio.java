package br.com.agenteingles.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    /**
     * Busca pelo e-mail sem diferenciar caixa.
     *
     * <p>O e-mail e a credencial de login, e comparar com caixa faria
     * {@code Joao@x.com} e {@code joao@x.com} virarem contas distintas — a restricao
     * de unicidade nao pegaria, e o mesmo endereco teria duas contas. O indice unico
     * criado na migration tambem e sobre {@code LOWER(email)}, para que a garantia
     * exista no banco e nao so aqui.
     *
     * <p>Os nomes de metodo estao em portugues, entao a consulta e declarada
     * explicitamente: o Spring Data so deriva consultas dos prefixos em ingles.
     */
    @Query("select u from Usuario u where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);

    @Query("select count(u) > 0 from Usuario u where lower(u.email) = lower(:email)")
    boolean existeComEmail(@Param("email") String email);
}
