package br.com.agenteingles.custo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Preco em dolar por milhao de tokens.
 *
 * <p>Fica em configuracao, e nao no codigo, porque tabela de preco muda sem aviso e
 * ninguem vai lembrar de recompilar por causa disso.
 *
 * @param entrada preco do token enviado
 * @param saida preco do token gerado — costuma ser cinco vezes o de entrada, e e por
 *              isso que encurtar a resposta economiza mais do que encurtar o pedido
 */
public record PrecoDoModelo(BigDecimal entrada, BigDecimal saida) {

    private static final BigDecimal UM_MILHAO = new BigDecimal("1000000");

    /** Casas suficientes para uma chamada de fracao de centavo nao virar zero. */
    private static final int CASAS = 6;

    public BigDecimal calcular(int tokensDeEntrada, int tokensDeSaida) {
        BigDecimal custoDaEntrada = entrada.multiply(BigDecimal.valueOf(tokensDeEntrada));
        BigDecimal custoDaSaida = saida.multiply(BigDecimal.valueOf(tokensDeSaida));
        return custoDaEntrada.add(custoDaSaida).divide(UM_MILHAO, CASAS, RoundingMode.HALF_UP);
    }
}
