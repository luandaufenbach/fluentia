package br.com.agenteingles.desafio;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvaliacaoDoDesafioRepositorio extends JpaRepository<AvaliacaoDoDesafio, Long> {

    /**
     * Avaliacoes de um modulo, da mais recente para a mais antiga.
     * E a base do calculo da nota: as recentes pesam mais.
     */
    @Query("""
            select a from AvaliacaoDoDesafio a
            where a.desafio.usuario.id = :usuarioId and a.desafio.modulo.id = :moduloId
            order by a.avaliadoEm desc
            """)
    List<AvaliacaoDoDesafio> listarRecentesDoModulo(@Param("usuarioId") Long usuarioId,
                                                    @Param("moduloId") Long moduloId,
                                                    Limit limite);

    /** Se ja houve pratica neste modulo. Decide o tamanho do lote a gerar. */
    @Query("""
            select count(a) from AvaliacaoDoDesafio a
            where a.desafio.usuario.id = :usuarioId and a.desafio.modulo.id = :moduloId
            """)
    long contarDoModulo(@Param("usuarioId") Long usuarioId, @Param("moduloId") Long moduloId);

    @Query("""
            select e.tipo from AvaliacaoDoDesafio a join a.errosDetectados e
            where a.desafio.usuario.id = :usuarioId and a.desafio.modulo.id = :moduloId
            order by a.avaliadoEm desc
            """)
    List<String> listarTiposDeErroRecentes(@Param("usuarioId") Long usuarioId,
                                           @Param("moduloId") Long moduloId,
                                           Limit limite);
}
