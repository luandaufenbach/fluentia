package br.com.agenteingles.custo;

import java.math.BigDecimal;

/** Onde o dinheiro foi: geracao, avaliacao ou conteudo. */
public record ConsumoPorTipo(TipoDeChamada tipo,
                             long chamadas,
                             BigDecimal custoUsd,
                             long itensProduzidos) {
}
