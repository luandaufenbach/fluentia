package br.com.agenteingles.trilha;

/**
 * Em que pe esta o marco da fase.
 *
 * <p>Existe porque o marco era booleano e uma fase inteira presumida pelo nivelamento
 * fechava com o mesmo tique de quem praticou. O marco nao e um numero, e uma frase — "se
 * apresentar e falar da sua rotina sem travar" — e dar isso por cumprido a partir de uma
 * estimativa contradiz o que o produto promete: <b>o estado vem da nota real, nao de um
 * clique</b>. Uma presuncao e ainda menos que um clique: nem o aluno afirmou.
 *
 * <p>Rebaixar tudo que e presumido para pendente resolveria a honestidade e criaria outro
 * problema: quem foi nivelado em B1 veria as duas primeiras fases como trabalho a fazer, e
 * a trilha voltaria a empurra-lo para tras — que e exatamente o que o nivelamento existe
 * para evitar. Por isso sao tres estados, e nao dois.
 */
public enum SituacaoDoMarco {

    /** Todos os conceitos da fase foram praticados e sairam do vermelho. */
    ALCANCADO,

    /**
     * Todos estao fora do vermelho, mas pelo menos um so por estimativa do nivelamento.
     *
     * <p>A fase nao trava o caminho — a nota presumida continua liberando o modulo
     * seguinte — mas o marco nao e anunciado como conquistado.
     */
    PRESUMIDO,

    /** Ainda ha conceito em vermelho ou nunca tocado. */
    PENDENTE;

    public boolean fechada() {
        return this == ALCANCADO;
    }
}
