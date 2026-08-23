package br.com.agenteingles.desafio;

import br.com.agenteingles.agente.ErroApontado;
import br.com.agenteingles.nota.FaixaDeNota;
import java.math.BigDecimal;
import java.util.List;

/**
 * O que o loop devolve depois de avaliar uma resposta, ja resolvido dentro da transacao.
 *
 * @param notaDaResposta nota apenas desta resposta
 * @param notaDoModulo nota do modulo ja recalculada com esta resposta incluida
 * @param reforco aviso de erro repetido, ou nulo quando nenhum tipo bateu o limite
 */
public record ResultadoDaResposta(
        Long desafioId,
        BigDecimal notaDaResposta,
        String feedback,
        List<ErroApontado> erros,
        BigDecimal notaDoModulo,
        FaixaDeNota faixaDoModulo,
        String moduloNome,
        ReforcoDeErro reforco) {
}
