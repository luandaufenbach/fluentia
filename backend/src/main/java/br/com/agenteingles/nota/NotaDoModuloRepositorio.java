package br.com.agenteingles.nota;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotaDoModuloRepositorio extends JpaRepository<NotaDoModulo, Long> {

    @Query("select n from NotaDoModulo n where n.usuario.id = :usuarioId")
    List<NotaDoModulo> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select n from NotaDoModulo n where n.usuario.id = :usuarioId and n.modulo.id = :moduloId")
    Optional<NotaDoModulo> buscarPorUsuarioEModulo(@Param("usuarioId") Long usuarioId,
                                                   @Param("moduloId") Long moduloId);
}
