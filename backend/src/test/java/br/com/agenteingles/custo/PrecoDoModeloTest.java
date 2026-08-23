package br.com.agenteingles.custo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PrecoDoModeloTest {

    /** Sonnet 5: US$ 3 por milhao na entrada, US$ 15 na saida. */
    private final PrecoDoModelo preco = new PrecoDoModelo(new BigDecimal("3.00"), new BigDecimal("15.00"));

    @Test
    @DisplayName("soma entrada e saida pela tabela do modelo")
    void somaEntradaESaida() {
        // 1.000 de entrada = US$ 0,003; 500 de saida = US$ 0,0075.
        assertThat(preco.calcular(1_000, 500)).isEqualByComparingTo("0.010500");
    }

    @Test
    @DisplayName("chamada de fracao de centavo nao arredonda para zero")
    void naoPerdeChamadaBarata() {
        // 176 tokens de entrada e nenhuma saida: US$ 0,000528. Com menos casas viraria zero,
        // e mil chamadas dessas somariam zero em vez de meio dolar.
        assertThat(preco.calcular(176, 0)).isEqualByComparingTo("0.000528");
    }

    @Test
    @DisplayName("o token de saida pesa cinco vezes o de entrada")
    void saidaPesaMais() {
        BigDecimal mesmaQuantidadeNaEntrada = preco.calcular(1_000, 0);
        BigDecimal mesmaQuantidadeNaSaida = preco.calcular(0, 1_000);

        assertThat(mesmaQuantidadeNaSaida)
                .isEqualByComparingTo(mesmaQuantidadeNaEntrada.multiply(new BigDecimal("5")));
    }
}
