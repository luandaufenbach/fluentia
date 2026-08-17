package br.com.agenteingles.nota;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Calculo da nota de dominio de um modulo.
 *
 * <p>Sao duas etapas separadas de proposito:
 * <ol>
 *   <li>{@link #calcularNotaDaPratica} produz a media ponderada das ultimas avaliacoes,
 *       com peso maior para as mais recentes. E esse valor que fica gravado no banco.</li>
 *   <li>{@link #aplicarDecaimento} desconta o esquecimento em funcao do tempo sem praticar.
 *       E aplicado na leitura, entao a nota exibida cai sozinha com o passar dos dias,
 *       sem precisar de rotina agendada reescrevendo o banco.</li>
 * </ol>
 */
@Service
public class ServicoDeNota {

    /** Quantas avaliacoes entram na media. Alem disso o peso ja e desprezivel. */
    public static final int QUANTIDADE_DE_AVALIACOES_CONSIDERADAS = 8;

    /** Peso de cada avaliacao anterior em relacao a seguinte: a mais recente domina. */
    private static final double FATOR_DE_RECENCIA = 0.65;

    /** Dias de folga antes de o esquecimento comecar a contar. */
    private static final int DIAS_DE_TOLERANCIA = 3;

    /** Passado esse periodo sem praticar, a nota cai pela metade. */
    private static final double MEIA_VIDA_EM_DIAS = 30.0;

    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = BigDecimal.TEN;

    /**
     * Media ponderada das avaliacoes recebidas, da mais recente para a mais antiga.
     *
     * @param notasRecentes notas obtidas, ordenadas da mais recente para a mais antiga
     * @return nota de 0 a 10, ou {@code null} se o modulo ainda nao foi praticado
     */
    public BigDecimal calcularNotaDaPratica(List<BigDecimal> notasRecentes) {
        if (notasRecentes == null || notasRecentes.isEmpty()) {
            return null;
        }

        double somaPonderada = 0;
        double somaDosPesos = 0;
        int consideradas = Math.min(notasRecentes.size(), QUANTIDADE_DE_AVALIACOES_CONSIDERADAS);

        for (int posicao = 0; posicao < consideradas; posicao++) {
            BigDecimal nota = notasRecentes.get(posicao);
            if (nota == null) {
                continue;
            }
            double peso = Math.pow(FATOR_DE_RECENCIA, posicao);
            somaPonderada += nota.doubleValue() * peso;
            somaDosPesos += peso;
        }

        if (somaDosPesos == 0) {
            return null;
        }
        return arredondar(somaPonderada / somaDosPesos);
    }

    /**
     * Desconta o esquecimento: apos {@value #DIAS_DE_TOLERANCIA} dias sem praticar,
     * a nota decai exponencialmente com meia-vida de {@value #MEIA_VIDA_EM_DIAS} dias.
     *
     * @return a nota ja descontada, ou {@code null} se ainda nao ha nota
     */
    public BigDecimal aplicarDecaimento(BigDecimal notaArmazenada,
                                        LocalDateTime dataDaUltimaPratica,
                                        LocalDateTime agora) {
        if (notaArmazenada == null) {
            return null;
        }
        if (dataDaUltimaPratica == null || agora == null || !agora.isAfter(dataDaUltimaPratica)) {
            return arredondar(notaArmazenada.doubleValue());
        }

        double diasParados = Duration.between(dataDaUltimaPratica, agora).toMinutes() / (60.0 * 24.0);
        double diasQueContam = diasParados - DIAS_DE_TOLERANCIA;
        if (diasQueContam <= 0) {
            return arredondar(notaArmazenada.doubleValue());
        }

        double retencao = Math.pow(0.5, diasQueContam / MEIA_VIDA_EM_DIAS);
        return arredondar(notaArmazenada.doubleValue() * retencao);
    }

    /** Faixa de cor correspondente a nota ja descontada. */
    public FaixaDeNota faixaDa(BigDecimal nota) {
        return FaixaDeNota.daNota(nota);
    }

    private BigDecimal arredondar(double valor) {
        BigDecimal resultado = BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
        if (resultado.compareTo(NOTA_MINIMA) < 0) {
            return NOTA_MINIMA.setScale(2, RoundingMode.HALF_UP);
        }
        if (resultado.compareTo(NOTA_MAXIMA) > 0) {
            return NOTA_MAXIMA.setScale(2, RoundingMode.HALF_UP);
        }
        return resultado;
    }
}
