package br.com.agenteingles.custo;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsumoDeApiRepositorio extends JpaRepository<ConsumoDeApi, Long> {

    /**
     * Total da conta a partir de uma data. Sempre filtrado por usuario: consumo de
     * uma conta nao aparece para outra.
     *
     * <p>O custo entra com {@code coalesce} para que uma linha sem preco configurado
     * nao anule a soma inteira; o endpoint avisa a parte quais modelos ficaram de fora.
     */
    @Query("""
            select new br.com.agenteingles.custo.TotalDeConsumo(
                count(c), coalesce(sum(c.tokensDeEntrada), 0), coalesce(sum(c.tokensDeSaida), 0),
                coalesce(sum(coalesce(c.custoUsd, 0)), 0))
            from ConsumoDeApi c
            where c.usuarioId = :usuarioId and c.ocorridoEm >= :desde
            """)
    TotalDeConsumo somarDoUsuarioDesde(@Param("usuarioId") Long usuarioId,
                                       @Param("desde") LocalDateTime desde);

    /** Total de sempre da conta. */
    @Query("""
            select new br.com.agenteingles.custo.TotalDeConsumo(
                count(c), coalesce(sum(c.tokensDeEntrada), 0), coalesce(sum(c.tokensDeSaida), 0),
                coalesce(sum(coalesce(c.custoUsd, 0)), 0))
            from ConsumoDeApi c
            where c.usuarioId = :usuarioId
            """)
    TotalDeConsumo somarDoUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
            select new br.com.agenteingles.custo.ConsumoPorTipo(
                c.tipoDeChamada, count(c),
                coalesce(sum(coalesce(c.custoUsd, 0)), 0), coalesce(sum(c.itensProduzidos), 0))
            from ConsumoDeApi c
            where c.usuarioId = :usuarioId
            group by c.tipoDeChamada
            order by sum(coalesce(c.custoUsd, 0)) desc
            """)
    List<ConsumoPorTipo> agruparPorTipo(@Param("usuarioId") Long usuarioId);

    /** Modelos que rodaram sem preco configurado — o custo deles esta faltando no total. */
    @Query("""
            select distinct c.modelo from ConsumoDeApi c
            where c.usuarioId = :usuarioId and c.custoUsd is null
            """)
    List<String> modelosSemPreco(@Param("usuarioId") Long usuarioId);
}
