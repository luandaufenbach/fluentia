package br.com.agenteingles.modulo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModuloRepositorio extends JpaRepository<Modulo, Long> {

    @Query("select m from Modulo m where m.codigo = :codigo")
    Optional<Modulo> buscarPorCodigo(@Param("codigo") String codigo);

    /** Carrega os modulos com os pre-requisitos ja resolvidos, na ordem do curriculo. */
    @Query("select distinct m from Modulo m left join fetch m.preRequisitos order by m.ordem")
    List<Modulo> listarTodosComPreRequisitos();
}
