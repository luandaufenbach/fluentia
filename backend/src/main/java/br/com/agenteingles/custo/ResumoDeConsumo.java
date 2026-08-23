package br.com.agenteingles.custo;

import java.math.BigDecimal;
import java.util.List;

/**
 * O que a conta consumiu da API.
 *
 * @param custoMedioPorDesafioRespondido custo total dividido pelos desafios que o aluno
 *                                       de fato respondeu — a geracao em lote ja entra
 *                                       diluida aqui
 * @param desafiosNaFila desafios ja pagos e ainda nao apresentados. Numero alto significa
 *                       lote grande demais para o ritmo de quem esta estudando
 * @param modelosSemPreco modelos que rodaram sem preco configurado. Enquanto a lista nao
 *                        estiver vazia, o custo mostrado esta abaixo do real
 */
public record ResumoDeConsumo(TotalDeConsumo hoje,
                              TotalDeConsumo ultimosSeteDias,
                              TotalDeConsumo total,
                              List<ConsumoPorTipo> porTipo,
                              BigDecimal custoMedioPorDesafioRespondido,
                              long desafiosGerados,
                              long desafiosRespondidos,
                              long desafiosNaFila,
                              List<String> modelosSemPreco) {
}
