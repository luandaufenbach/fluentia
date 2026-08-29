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

    // ---------- visao do administrador: todas as contas ----------

    @Query("""
            select new br.com.agenteingles.custo.TotalDeConsumo(
                count(c), coalesce(sum(c.tokensDeEntrada), 0), coalesce(sum(c.tokensDeSaida), 0),
                coalesce(sum(coalesce(c.custoUsd, 0)), 0))
            from ConsumoDeApi c
            where c.ocorridoEm >= :desde
            """)
    TotalDeConsumo somarDesde(@Param("desde") LocalDateTime desde);

    @Query("""
            select new br.com.agenteingles.custo.TotalDeConsumo(
                count(c), coalesce(sum(c.tokensDeEntrada), 0), coalesce(sum(c.tokensDeSaida), 0),
                coalesce(sum(coalesce(c.custoUsd, 0)), 0))
            from ConsumoDeApi c
            """)
    TotalDeConsumo somarTudo();

    @Query("""
            select new br.com.agenteingles.custo.ConsumoPorTipo(
                c.tipoDeChamada, count(c),
                coalesce(sum(coalesce(c.custoUsd, 0)), 0), coalesce(sum(c.itensProduzidos), 0))
            from ConsumoDeApi c
            group by c.tipoDeChamada
            order by sum(coalesce(c.custoUsd, 0)) desc
            """)
    List<ConsumoPorTipo> agruparPorTipoGeral();

    /**
     * Uma linha por conta que gastou.
     *
     * <p>O custo usa {@code sum(c.custoUsd)} SEM o coalesce dos outros metodos, e a
     * diferenca e proposital: aqui, se algum registro da conta tiver custo nulo, o
     * somatorio precisa poder sair nulo para a tela avisar "nao da para saber". Com
     * coalesce, o desconhecido viraria zero e o total apareceria menor do que e —
     * exatamente o erro que ninguem percebe ate a fatura chegar.
     */
    @Query("""
            select new br.com.agenteingles.admin.TotalDoUsuario(
                c.usuarioId, count(c),
                coalesce(sum(c.tokensDeEntrada), 0), coalesce(sum(c.tokensDeSaida), 0),
                sum(c.custoUsd))
            from ConsumoDeApi c
            where c.usuarioId is not null
            group by c.usuarioId
            """)
    List<br.com.agenteingles.admin.TotalDoUsuario> somarPorUsuario();

    @Query("select distinct c.modelo from ConsumoDeApi c where c.custoUsd is null")
    List<String> modelosSemPrecoGeral();
}
