package br.com.agenteingles.desafio;

import br.com.agenteingles.nota.FaixaDeNota;
import java.math.BigDecimal;

/**
 * O que o loop devolve depois de avaliar uma resposta.
 *
 * @param notaDoModulo nota do modulo ja recalculada com esta resposta incluida
 */
public record ResultadoDaResposta(
        AvaliacaoDoDesafio avaliacao,
        BigDecimal notaDoModulo,
        FaixaDeNota faixaDoModulo) {
}
