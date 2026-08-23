package br.com.agenteingles.custo;

import java.math.BigDecimal;

/** Soma de um periodo. */
public record TotalDeConsumo(long chamadas,
                             long tokensDeEntrada,
                             long tokensDeSaida,
                             BigDecimal custoUsd) {
}
