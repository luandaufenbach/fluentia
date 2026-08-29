package br.com.agenteingles.admin;

import br.com.agenteingles.custo.ConsumoPorTipo;
import br.com.agenteingles.custo.TotalDeConsumo;
import java.util.List;

/**
 * O que o administrador ve numa consulta.
 *
 * @param modelosSemPreco modelos usados sem preco configurado. Enquanto esta lista nao
 *                        estiver vazia, os totais estao <b>incompletos</b> — a tela
 *                        precisa dizer isso, senao o numero e lido como se fosse o gasto
 *                        inteiro e a diferenca so aparece na fatura
 */
public record PainelDoAdministrador(
        TotalDeConsumo hoje,
        TotalDeConsumo ultimosSeteDias,
        TotalDeConsumo total,
        List<ConsumoPorTipo> porTipo,
        long contasAtivas,
        long contasNoTotal,
        List<LinhaDoPainel> contas,
        List<String> modelosSemPreco) {
}
