package br.com.agenteingles.custo;

/** O que provocou a chamada, para saber onde o dinheiro esta indo. */
public enum TipoDeChamada {

    /** Lote de desafios. Uma chamada rende varios desafios. */
    GERACAO_DE_DESAFIO("geracao de desafio"),

    /** Correcao de uma resposta. Uma chamada por resposta, e onde a saida pesa. */
    AVALIACAO_DE_RESPOSTA("avaliacao de resposta"),

    /** Material de estudo dos modulos. Rotina avulsa, roda uma vez. */
    GERACAO_DE_CONTEUDO("geracao de conteudo");

    private final String descricao;

    TipoDeChamada(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
