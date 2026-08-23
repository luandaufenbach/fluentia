package br.com.agenteingles.desafio;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /** Quantas respostas a conta ja avaliou a partir de um instante. E a conta do dia. */
    @Query("""
            select count(a) from AvaliacaoDoDesafio a
            where a.desafio.usuario.id = :usuarioId and a.avaliadoEm >= :desde
            """)
    long contarDesde(@Param("usuarioId") Long usuarioId, @Param("desde") LocalDateTime desde);

    /**
     * Os dias em que a conta praticou, do mais recente para o mais antigo.
     *
     * <p>Consulta nativa porque o recorte para data e do banco: trazer todas as
     * avaliacoes so para agrupar em memoria cresce junto com o historico, e quem estuda
     * todo dia e justamente quem teria a lista maior.
     */
    @Query(value = """
            SELECT DISTINCT CAST(a.avaliado_em AS date) AS dia
            FROM avaliacao_do_desafio a
            JOIN desafio d ON d.id = a.desafio_id
            WHERE d.usuario_id = :usuarioId
            ORDER BY dia DESC
            """, nativeQuery = true)
    List<LocalDate> listarDiasPraticados(@Param("usuarioId") Long usuarioId);

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
