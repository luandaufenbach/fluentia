package br.com.agenteingles.sessao;

import java.time.LocalDate;
import java.util.List;

/**
 * Dias seguidos de pratica.
 *
 * <p>A regra e honesta de proposito: <b>um desafio respondido conta o dia</b>. Exigir a
 * sessao inteira transformaria a sequencia num segundo cobrador, e quem tem quinze
 * minutos ruins no dia perderia semanas de constancia — que e exatamente o habito que a
 * sequencia deveria proteger.
 *
 * @param atual dias seguidos ate hoje
 * @param melhor a maior sequencia que a conta ja teve
 * @param praticouHoje se hoje ja conta
 */
public record SequenciaDeDias(int atual, int melhor, boolean praticouHoje) {

    static final SequenciaDeDias VAZIA = new SequenciaDeDias(0, 0, false);

    /**
     * Calcula a sequencia a partir dos dias praticados, do mais recente para o mais antigo.
     *
     * <p>O dia de hoje ainda nao acabou: quem praticou ontem e ainda nao praticou hoje
     * continua com a sequencia viva. Quebrar a sequencia as 00h01 de quem estuda a noite
     * seria punir o relogio, nao a falta de pratica.
     *
     * @param diasPraticados sem repeticao, em ordem decrescente
     */
    static SequenciaDeDias calcular(List<LocalDate> diasPraticados, LocalDate hoje) {
        if (diasPraticados == null || diasPraticados.isEmpty()) {
            return VAZIA;
        }

        boolean praticouHoje = diasPraticados.get(0).isEqual(hoje);
        LocalDate ontem = hoje.minusDays(1);
        boolean sequenciaViva = praticouHoje || diasPraticados.get(0).isEqual(ontem);

        int atual = sequenciaViva ? contarSeguidosApartirDe(diasPraticados, 0) : 0;
        return new SequenciaDeDias(atual, calcularMelhor(diasPraticados), praticouHoje);
    }

    private static int contarSeguidosApartirDe(List<LocalDate> dias, int inicio) {
        int seguidos = 1;
        for (int i = inicio; i < dias.size() - 1; i++) {
            if (!dias.get(i).minusDays(1).isEqual(dias.get(i + 1))) {
                break;
            }
            seguidos++;
        }
        return seguidos;
    }

    private static int calcularMelhor(List<LocalDate> dias) {
        int melhor = 1;
        int corrente = 1;
        for (int i = 0; i < dias.size() - 1; i++) {
            if (dias.get(i).minusDays(1).isEqual(dias.get(i + 1))) {
                corrente++;
            } else {
                corrente = 1;
            }
            melhor = Math.max(melhor, corrente);
        }
        return melhor;
    }
}
