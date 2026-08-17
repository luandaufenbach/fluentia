package br.com.agenteingles.agente;

import java.math.BigDecimal;
import java.util.List;

/**
 * Veredito do avaliador sobre uma resposta.
 *
 * @param notaObtida nota de 0 a 10 apenas para esta resposta, antes de entrar na media do modulo
 */
public record ResultadoDaAvaliacao(
        BigDecimal notaObtida,
        String feedback,
        List<ErroApontado> erros) {
}
