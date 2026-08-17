package br.com.agenteingles.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    // Os nomes de metodo estao em portugues, entao a consulta e declarada explicitamente:
    // o Spring Data so deriva consultas a partir dos prefixos em ingles (find/read/get...).
    @Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);
}
