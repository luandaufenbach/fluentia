package br.com.agenteingles.agente;

/**
 * Erro especifico encontrado na resposta.
 *
 * @param tipo categoria curta do erro (ex.: "concordancia_do_verbo_to_be")
 * @param trechoErrado o pedaco exato da resposta que esta errado
 * @param correcao como o trecho deveria ficar
 * @param explicacao por que esta errado, em portugues
 */
public record ErroApontado(
        String tipo,
        String trechoErrado,
        String correcao,
        String explicacao) {
}
