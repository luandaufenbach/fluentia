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
        assertThat(resultado.erros().get(0).tipo()).isEqualTo("detalhe_de_forma");
    }

    @Test
    @DisplayName("resposta totalmente fora do pedido recebe nota baixa")
    void respostaForaDoPedidoRecebeNotaBaixa() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("The weather looks nice today"));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("2.00");
        assertThat(resultado.erros().get(0).tipo()).isEqualTo("estrutura_da_frase");
    }

    @Test
    @DisplayName("resposta em branco zera a nota")
    void respostaEmBrancoZeraANota() {
        ResultadoDaAvaliacao resultado = avaliador.avaliar(pedidoComResposta("   "));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("0.00");
        assertThat(resultado.erros().get(0).tipo()).isEqualTo("resposta_em_branco");
    }
}
