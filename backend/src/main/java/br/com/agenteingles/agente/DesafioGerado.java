package br.com.agenteingles.agente;

/**
 * Desafio produzido pelo agente gerador.
 *
 * @param enunciado a instrucao que o usuario le
 * @param contextoDaCena a situacao do tema que envolve o desafio
 * @param respostaDeReferencia uma resposta correta, usada como apoio na avaliacao
 * @param criterioDeAvaliacao o que exatamente deve ser verificado na resposta
 */
public record DesafioGerado(
        String enunciado,
        String contextoDaCena,
        String respostaDeReferencia,
        String criterioDeAvaliacao) {
}
