package br.com.agenteingles.desafio;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * O historico de erros por tipo.
 *
 * <p>E o que permite dizer "voce ja errou isto tres vezes" com um numero de verdade, e
 * nao com uma impressao.
 */
public interface ErroDetectadoRepositorio extends JpaRepository<ErroDetectado, Long> {

    /** Quantas vezes a conta ja cometeu este tipo de erro, em qualquer modulo. */
    @Query("""
            select count(e) from ErroDetectado e
            where e.avaliacao.desafio.usuario.id = :usuarioId and e.tipo = :tipo
            """)
    long contarDoTipo(@Param("usuarioId") Long usuarioId, @Param("tipo") String tipo);

    /**
     * As vezes anteriores em que o mesmo erro apareceu, da mais recente para a mais antiga.
     *
     * <p>A avaliacao atual fica de fora: repetir na tela o erro que o aluno acabou de ver
     * logo acima nao acrescenta nada.
     */
    @Query("""
            select e from ErroDetectado e
            where e.avaliacao.desafio.usuario.id = :usuarioId
              and e.tipo = :tipo
              and e.avaliacao.id <> :avaliacaoAtual
            order by e.avaliacao.avaliadoEm desc
            """)
    List<ErroDetectado> listarAnteriores(@Param("usuarioId") Long usuarioId,
                                         @Param("tipo") String tipo,
                                         @Param("avaliacaoAtual") Long avaliacaoAtual,
                                         Limit limite);

    /**
     * O modulo onde este erro mais aparece, para apontar o material certo.
     *
     * <p>E um vinculo derivado do proprio historico, e nao um palpite por semelhanca de
     * texto: um erro de concordancia do verbo "to be" cometido praticando artigos continua
     * sendo ensinado no modulo do verbo "to be", e e para la que o aluno precisa ir.
     */
    @Query("""
            select e.avaliacao.desafio.modulo.codigo from ErroDetectado e
            where e.avaliacao.desafio.usuario.id = :usuarioId and e.tipo = :tipo
            group by e.avaliacao.desafio.modulo.codigo
            order by count(e) desc
            """)
    List<String> modulosOndeMaisAcontece(@Param("usuarioId") Long usuarioId,
                                         @Param("tipo") String tipo,
                                         Limit limite);
}
