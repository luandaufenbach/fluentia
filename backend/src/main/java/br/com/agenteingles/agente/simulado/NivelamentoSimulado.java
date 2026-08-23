package br.com.agenteingles.agente.simulado;

import br.com.agenteingles.agente.AgenteDeNivelamento;
import br.com.agenteingles.agente.PedidoDeNivelamento;
import br.com.agenteingles.agente.ResultadoDoNivelamento;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Nivelamento simulado: estima o nivel pelo teto da escada.
 *
 * <p>A heuristica e deliberadamente simples e conservadora — o nivel e o alvo da ultima
 * pergunta que a pessoa tentou responder com alguma substancia. Ela nao julga a
 * qualidade do ingles, so ate onde a pessoa foi, entao serve para rodar o fluxo sem
 * custo de API e nao substitui o agente real.
 */
@Component
@ConditionalOnProperty(name = "agente-ingles.usar-claude", havingValue = "false", matchIfMissing = true)
public class NivelamentoSimulado implements AgenteDeNivelamento {

    /**
     * Abaixo disso a resposta e tratada como tentativa vazia. Cinco palavras nao
     * sustentam nem a pergunta de A1, que pede nome, origem e ocupacao.
     */
    private static final int PALAVRAS_MINIMAS = 5;

    private static final String NIVEL_INICIAL = "A1";

    @Override
    public ResultadoDoNivelamento estimar(PedidoDeNivelamento pedido) {
        String tetoAlcancado = NIVEL_INICIAL;
        int respondidas = 0;

        for (PedidoDeNivelamento.TurnoDoNivelamento turno : pedido.turnos()) {
            if (temSubstancia(turno.resposta())) {
                tetoAlcancado = turno.nivelAlvo();
                respondidas++;
            }
        }

        return new ResultadoDoNivelamento(
                tetoAlcancado,
                montarResumo(respondidas, tetoAlcancado),
                respondidas == 0 ? "a disposicao de comecar" : "sustentar a resposta ate o nivel " + tetoAlcancado,
                "os conceitos do proprio nivel, que a trilha ja apresenta em ordem");
    }

    private boolean temSubstancia(String resposta) {
        return resposta != null && resposta.trim().split("\s+").length >= PALAVRAS_MINIMAS;
    }

    private String montarResumo(int respondidas, String teto) {
        if (respondidas == 0) {
            return "Começamos do início, então. A trilha abre nas primeiras frases e sobe daí.";
        }
        return "Você respondeu %d pergunta(s) e chegou até o nível %s. A trilha começa daí."
                .formatted(respondidas, teto.toUpperCase(Locale.ROOT));
    }
}
