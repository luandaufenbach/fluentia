package br.com.agenteingles.sessao;

import br.com.agenteingles.nota.FaixaDeNota;
import java.math.BigDecimal;

/**
 * Um conceito que esta caindo por falta de pratica.
 *
 * <p>O decaimento ja existia e ninguem era avisado: a nota caia sozinha na tela e o
 * aluno so descobria ao ver o modulo mudar de cor sem ter errado nada.
 *
 * @param notaQuandoPraticou a nota que ficou gravada na ultima pratica
 * @param notaHoje a mesma nota com o esquecimento ja descontado
 * @param queda quanto o tempo tirou — e o que ordena a lista
 * @param mudouDeFaixa se a queda passou o conceito para uma faixa de cor pior; e o caso
 *                     mais urgente, porque ai o conceito pode ter deixado de liberar o
 *                     modulo seguinte
 * @param diasSemPraticar dias desde a ultima pratica de verdade
 */
public record RevisaoPendente(String moduloCodigo,
                              String moduloNome,
                              BigDecimal notaQuandoPraticou,
                              BigDecimal notaHoje,
                              BigDecimal queda,
                              FaixaDeNota faixaHoje,
                              boolean mudouDeFaixa,
                              long diasSemPraticar) {
}
