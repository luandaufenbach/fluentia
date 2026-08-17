package br.com.agenteingles.agente;

/**
 * Analisa a resposta do usuario, atribui nota e detecta o erro especifico.
 * A qualidade da correcao e o coracao do produto: e o que alimenta o reforco dirigido.
 */
public interface AgenteAvaliador {

    ResultadoDaAvaliacao avaliar(PedidoDeAvaliacao pedido);
}
