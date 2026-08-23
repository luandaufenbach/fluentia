package br.com.agenteingles.agente;

/** Estima o nivel CEFR a partir da conversa curta de entrada. */
public interface AgenteDeNivelamento {

    ResultadoDoNivelamento estimar(PedidoDeNivelamento pedido);
}
