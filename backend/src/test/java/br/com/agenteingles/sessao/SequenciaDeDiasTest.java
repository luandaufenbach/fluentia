package br.com.agenteingles.sessao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A sequencia de dias e cheia de casos de borda que so aparecem na virada do dia.
 * Cada um deles esta aqui.
 */
class SequenciaDeDiasTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 23);

    @Test
    @DisplayName("sem pratica nenhuma a sequencia e zero")
    void semPraticaESequenciaZero() {
        assertThat(SequenciaDeDias.calcular(List.of(), HOJE).atual()).isZero();
        assertThat(SequenciaDeDias.calcular(null, HOJE).atual()).isZero();
    }

    @Test
    @DisplayName("dias seguidos ate hoje contam a sequencia inteira")
    void diasSeguidosAteHoje() {
        SequenciaDeDias sequencia = SequenciaDeDias.calcular(
                List.of(HOJE, HOJE.minusDays(1), HOJE.minusDays(2)), HOJE);

        assertThat(sequencia.atual()).isEqualTo(3);
        assertThat(sequencia.praticouHoje()).isTrue();
    }

    @Test
    @DisplayName("quem praticou ontem e ainda nao praticou hoje mantem a sequencia")
    void ontemMantemASequencia() {
        // O dia ainda nao acabou. Zerar a sequencia as 00h01 puniria o relogio, nao a
        // falta de pratica — e e justamente quem estuda a noite que perderia tudo.
        SequenciaDeDias sequencia = SequenciaDeDias.calcular(
                List.of(HOJE.minusDays(1), HOJE.minusDays(2)), HOJE);

        assertThat(sequencia.atual()).isEqualTo(2);
        assertThat(sequencia.praticouHoje()).isFalse();
    }

    @Test
    @DisplayName("um dia inteiro sem praticar quebra a sequencia")
    void faltarUmDiaQuebra() {
        SequenciaDeDias sequencia = SequenciaDeDias.calcular(
                List.of(HOJE.minusDays(2), HOJE.minusDays(3), HOJE.minusDays(4)), HOJE);

        assertThat(sequencia.atual()).isZero();
        // A quebra nao apaga o que ja foi conquistado.
        assertThat(sequencia.melhor()).isEqualTo(3);
    }

    @Test
    @DisplayName("a melhor sequencia sobrevive a uma quebra no meio")
    void melhorSequenciaSobreviveAQuebra() {
        SequenciaDeDias sequencia = SequenciaDeDias.calcular(
                List.of(HOJE, HOJE.minusDays(1),
                        HOJE.minusDays(5), HOJE.minusDays(6), HOJE.minusDays(7), HOJE.minusDays(8)),
                HOJE);

        assertThat(sequencia.atual()).isEqualTo(2);
        assertThat(sequencia.melhor()).isEqualTo(4);
    }

    @Test
    @DisplayName("praticar so hoje ja e sequencia de um")
    void primeiroDiaContaComoUm() {
        SequenciaDeDias sequencia = SequenciaDeDias.calcular(List.of(HOJE), HOJE);

        assertThat(sequencia.atual()).isEqualTo(1);
        assertThat(sequencia.melhor()).isEqualTo(1);
    }

    @Test
    @DisplayName("a sequencia atravessa a virada do mes")
    void atravessaAViradaDoMes() {
        LocalDate primeiroDeSetembro = LocalDate.of(2026, 9, 1);

        SequenciaDeDias sequencia = SequenciaDeDias.calcular(
                List.of(primeiroDeSetembro, LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 30)),
                primeiroDeSetembro);

        assertThat(sequencia.atual()).isEqualTo(3);
    }
}
