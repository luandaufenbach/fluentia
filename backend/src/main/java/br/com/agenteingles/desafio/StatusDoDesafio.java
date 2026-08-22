package br.com.agenteingles.desafio;

/** Ciclo de vida de um desafio gerado. */
public enum StatusDoDesafio {
    /** Gerado em lote e ainda nao apresentado: proxima pratica do modulo sai daqui. */
    NA_FILA,
    AGUARDANDO_RESPOSTA,
    AVALIADO,
    DESCARTADO
}
