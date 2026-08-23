package br.com.agenteingles.agente.simulado;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.agente.ErroApontado;
import br.com.agenteingles.agente.PedidoDeAvaliacao;
import br.com.agenteingles.agente.ResultadoDaAvaliacao;
import br.com.agenteingles.modulo.NivelCefr;
import org.junit.jupiter.api.DisplayName;
import br.com.agenteingles.usuario.TipoDeCorrecao;
import org.junit.jupiter.api.Test;

class AvaliadorSimuladoTest {

    private final AvaliadorSimulado avaliador = new AvaliadorSimulado();

    private PedidoDeAvaliacao pedidoComResposta(String resposta) {
        return new PedidoDeAvaliacao(
                1L,
                "verbo_to_be",
                "Verbo \"to be\"",
                "Formas am/is/are no presente.",
                NivelCefr.A1,
                "Traduza para o ingles: \"Eu sou brasileiro.\"",
                "Voce acabou de conhecer alguem numa festa.",
                "I am Brazilian.",
                "Verificar a forma correta do verbo \"to be\".",
                resposta,
                TipoDeCorrecao.DETALHADA);
    }

    @Test
    @DisplayName("resposta igual a referencia recebe nota maxima e nenhum erro")
    void respostaCorretaRecebeNotaMaxima() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("I am Brazilian."));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("10.00");
        assertThat(resultado.erros()).isEmpty();
    }

    @Test
    @DisplayName("pontuacao e caixa nao mudam o veredito")
    void pontuacaoECaixaNaoMudamOVeredito() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("  i AM brazilian  "));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("concordancia errada do verbo to be vira erro especifico com correcao")
    void concordanciaErradaViraErroEspecifico() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("I are Brazilian."));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("3.00");
        assertThat(resultado.erros()).hasSize(1);

        ErroApontado erro = resultado.erros().get(0);
        assertThat(erro.tipo()).isEqualTo("concordancia_do_verbo_to_be");
        assertThat(erro.trechoErrado()).isEqualTo("i are");
        assertThat(erro.correcao()).isEqualTo("i am");
        assertThat(erro.explicacao()).contains("am");
    }

    @Test
    @DisplayName("resposta proxima da referencia fica na faixa intermediaria")
    void respostaProximaFicaNaFaixaIntermediaria() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("I am from Brazilian"));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("7.00");
        assertThat(resultado.erros()).hasSize(1);
        assertThat(resultado.erros().get(0).tipo()).isEqualTo("vocabulario");
    }

    @Test
    @DisplayName("resposta totalmente fora do pedido recebe nota baixa")
    void respostaForaDoPedidoRecebeNotaBaixa() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("The weather looks nice today"));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("2.00");
        // Fora do pedido nao e erro do conceito: rotular como tal acusaria o aluno de
        // insistir num erro que ele nem cometeu, e o tipo e a chave que conta a repeticao.
        assertThat(resultado.erros().get(0).tipo()).isEqualTo("resposta_fora_do_pedido");
    }

    @Test
    @DisplayName("resposta em branco zera a nota")
    void respostaEmBrancoZeraANota() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("   "));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("0.00");
        assertThat(resultado.erros().get(0).tipo()).isEqualTo("resposta_em_branco");
    }

    /** Pedido de outro modulo, com a referencia que o banco de alvos daquele modulo daria. */
    private PedidoDeAvaliacao pedidoDeOutroModulo(String resposta) {
        return new PedidoDeAvaliacao(
                1L,
                "passado_simples",
                "Passado simples",
                "Verbos regulares e irregulares no passado.",
                NivelCefr.A2,
                "Traduza para o ingles: \"Eu comprei um livro ontem.\"",
                "Voce esta contando como foi o seu dia.",
                "I bought a book yesterday.",
                "Verificar o uso correto de Passado simples.",
                resposta,
                TipoDeCorrecao.DETALHADA);
    }

    @Test
    @DisplayName("modulo fora do verbo to be tambem recebe nota de verdade")
    void outroModuloRecebeNotaDeVerdade() {
        // Antes do banco de alvos, os outros quinze modulos vinham sem gabarito e a nota
        // saia fixa em 7 — a trilha inteira ficava amarela independentemente da resposta.
        ResultadoDaAvaliacao certa = avaliador.avaliar(
                pedidoDeOutroModulo("I bought a book yesterday."));
        ResultadoDaAvaliacao errada = avaliador.avaliar(
                pedidoDeOutroModulo("I buy a book tomorrow morning."));

        assertThat(certa.notaObtida()).isEqualByComparingTo("10.00");
        assertThat(errada.notaObtida()).isLessThan(certa.notaObtida());
    }

    @Test
    @DisplayName("o erro aponta o tipo tipico do modulo, nao um rotulo generico")
    void erroApontaOTipoTipicoDoModulo() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(
                pedidoDeOutroModulo("I buy a book tomorrow morning."));

        assertThat(resultado.erros())
                .extracting(ErroApontado::tipo)
                .containsExactly("verbo_irregular");
    }
}
