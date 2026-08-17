package br.com.agenteingles.nota;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServicoDeNotaTest {

    private final ServicoDeNota servicoDeNota = new ServicoDeNota();

    @Test
    @DisplayName("modulo sem pratica nao tem nota e aparece como novo")
    void moduloSemPraticaNaoTemNota() {
        assertThat(servicoDeNota.calcularNotaDaPratica(List.of())).isNull();
        assertThat(servicoDeNota.faixaDa(null)).isEqualTo(FaixaDeNota.NOVO);
    }

    @Test
    @DisplayName("uma unica avaliacao vira a propria nota")
    void umaUnicaAvaliacaoViraNota() {
        BigDecimal nota = servicoDeNota.calcularNotaDaPratica(List.of(new BigDecimal("7.5")));

        assertThat(nota).isEqualByComparingTo("7.5");
    }

    @Test
    @DisplayName("a avaliacao mais recente pesa mais que as antigas")
    void avaliacaoRecentePesaMais() {
        // Mesmas notas em ordem invertida: quem acabou de acertar deve ficar acima de quem acabou de errar.
        BigDecimal melhorouAgora = servicoDeNota.calcularNotaDaPratica(
                List.of(new BigDecimal("10"), new BigDecimal("2")));
        BigDecimal piorouAgora = servicoDeNota.calcularNotaDaPratica(
                List.of(new BigDecimal("2"), new BigDecimal("10")));

        assertThat(melhorouAgora).isGreaterThan(piorouAgora);
        assertThat(melhorouAgora).isGreaterThan(new BigDecimal("6"));
        assertThat(piorouAgora).isLessThan(new BigDecimal("6"));
    }

    @Test
    @DisplayName("so as ultimas avaliacoes entram na media")
    void apenasAsUltimasAvaliacoesEntramNaMedia() {
        // Uma nota zero muito antiga, alem da janela, nao deve derrubar quem so acerta agora.
        List<BigDecimal> historico = new java.util.ArrayList<>(
                java.util.Collections.nCopies(ServicoDeNota.QUANTIDADE_DE_AVALIACOES_CONSIDERADAS, BigDecimal.TEN));
        historico.add(BigDecimal.ZERO);

        assertThat(servicoDeNota.calcularNotaDaPratica(historico)).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("dentro da tolerancia a nota nao decai")
    void dentroDaToleranciaNaoDecai() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 16, 12, 0);
        LocalDateTime ontem = agora.minusDays(1);

        BigDecimal nota = servicoDeNota.aplicarDecaimento(new BigDecimal("9.00"), ontem, agora);

        assertThat(nota).isEqualByComparingTo("9.00");
    }

    @Test
    @DisplayName("depois de uma meia-vida sem praticar a nota cai pela metade")
    void notaCaiPelaMetadeAposUmaMeiaVida() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 16, 12, 0);
        // 3 dias de tolerancia + 30 dias de meia-vida.
        LocalDateTime ultimaPratica = agora.minusDays(33);

        BigDecimal nota = servicoDeNota.aplicarDecaimento(new BigDecimal("10.00"), ultimaPratica, agora);

        assertThat(nota).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("o decaimento eventualmente derruba a nota de verde para vermelho")
    void decaimentoDerrubaAFaixaDeCor() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 16, 12, 0);
        BigDecimal notaGravada = new BigDecimal("9.50");

        assertThat(servicoDeNota.faixaDa(servicoDeNota.aplicarDecaimento(notaGravada, agora.minusDays(1), agora)))
                .isEqualTo(FaixaDeNota.VERDE);
        assertThat(servicoDeNota.faixaDa(servicoDeNota.aplicarDecaimento(notaGravada, agora.minusDays(90), agora)))
                .isEqualTo(FaixaDeNota.VERMELHO);
    }

    @Test
    @DisplayName("as faixas de cor nao tem lacuna entre si")
    void faixasDeCorNaoTemLacuna() {
        assertThat(FaixaDeNota.daNota(new BigDecimal("5.99"))).isEqualTo(FaixaDeNota.VERMELHO);
        assertThat(FaixaDeNota.daNota(new BigDecimal("6.00"))).isEqualTo(FaixaDeNota.AMARELO);
        assertThat(FaixaDeNota.daNota(new BigDecimal("8.99"))).isEqualTo(FaixaDeNota.AMARELO);
        assertThat(FaixaDeNota.daNota(new BigDecimal("9.00"))).isEqualTo(FaixaDeNota.VERDE);
        assertThat(FaixaDeNota.daNota(new BigDecimal("10.00"))).isEqualTo(FaixaDeNota.VERDE);
    }
}
