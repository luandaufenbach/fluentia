package br.com.agenteingles.nota;

import java.math.BigDecimal;

/**
 * Faixa de cor exibida ao lado da nota. Sem lacuna entre as faixas:
 * vermelho abaixo de 6, amarelo de 6 a 8,9 e verde de 9 a 10.
 */
public enum FaixaDeNota {
    NOVO,
    VERMELHO,
    AMARELO,
    VERDE;

    private static final BigDecimal LIMITE_DO_VERMELHO = new BigDecimal("6");
    private static final BigDecimal LIMITE_DO_AMARELO = new BigDecimal("9");

    /** Um modulo ainda nao praticado nao tem nota e aparece como "novo". */
    public static FaixaDeNota daNota(BigDecimal nota) {
        if (nota == null) {
            return NOVO;
        }
        if (nota.compareTo(LIMITE_DO_VERMELHO) < 0) {
            return VERMELHO;
        }
        if (nota.compareTo(LIMITE_DO_AMARELO) < 0) {
            return AMARELO;
        }
        return VERDE;
    }
}
