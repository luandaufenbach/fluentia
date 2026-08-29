package br.com.agenteingles.admin;

import java.math.BigDecimal;

/**
 * Consumo somado de uma conta.
 *
 * @param custoUsd nulo quando algum modelo usado nao tinha preco. Nulo e "nao da para
 *                 saber", nunca zero: total que soma desconhecido como zero mente para
 *                 baixo, e essa e a direcao perigosa numa conta que alguem paga.
 */
public record TotalDoUsuario(Long usuarioId,
                             long chamadas,
                             long tokensDeEntrada,
                             long tokensDeSaida,
                             BigDecimal custoUsd) {
}
