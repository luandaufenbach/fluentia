package br.com.agenteingles.nivelamento;

public enum StatusDoNivelamento {

    EM_ANDAMENTO,

    CONCLUIDO,

    /** Encerrado sem chegar ao fim: o aluno preferiu comecar do zero. */
    ABANDONADO
}
