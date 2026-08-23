package br.com.agenteingles.sessao;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.desafio.ResumoDoDesafio;
import br.com.agenteingles.desafio.ServicoDeDesafio;
import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.modulo.ModuloRepositorio;
import br.com.agenteingles.nota.NotaDoModulo;
import br.com.agenteingles.nota.FaixaDeNota;
import br.com.agenteingles.nota.NotaDoModuloRepositorio;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** A sessao do dia contra o banco de verdade. */
@SpringBootTest
@Transactional
class SessaoDoDiaIT {

    @Autowired
    private ServicoDaSessao servicoDaSessao;

    @Autowired
    private ServicoDeDesafio servicoDeDesafio;

    @Autowired
    private ServicoDeUsuario servicoDeUsuario;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @Autowired
    private NotaDoModuloRepositorio notaRepositorio;

    @Autowired
    private ModuloRepositorio moduloRepositorio;

    @Autowired
    private ContaDeTeste conta;

    private Usuario usuario;

    @BeforeEach
    void prepararLinhaDeBase() {
        limpeza.limparHistoricoENotas();
        conta.autenticar();
        usuario = servicoDeUsuario.usuarioAtual();
    }

    @AfterEach
    void encerrarSessao() {
        conta.limparContexto();
    }

    @Test
    @DisplayName("a meta do dia vem do ritmo escolhido, nao de um numero fixo")
    void metaVemDoRitmo() {
        usuario.setMinutosPorDia(15);
        assertThat(servicoDaSessao.doUsuario(usuario).meta()).isEqualTo(5);

        usuario.setMinutosPorDia(30);
        assertThat(servicoDaSessao.doUsuario(usuario).meta()).isEqualTo(10);
    }

    @Test
    @DisplayName("ritmo minimo ainda rende uma sessao com mais de um desafio")
    void ritmoMinimoTemPiso() {
        usuario.setMinutosPorDia(5);

        // Um desafio so nao e sessao: o piso existe para a tela de fechamento significar algo.
        assertThat(servicoDaSessao.doUsuario(usuario).meta()).isEqualTo(3);
    }

    @Test
    @DisplayName("o dia comeca zerado e a meta nao esta alcancada")
    void diaComecaZerado() {
        ResumoDoDia resumo = servicoDaSessao.doUsuario(usuario);

        assertThat(resumo.concluidos()).isZero();
        assertThat(resumo.metaAlcancada()).isFalse();
        assertThat(resumo.restantes()).isEqualTo(resumo.meta());
        assertThat(resumo.sequencia().praticouHoje()).isFalse();
        assertThat(resumo.sequencia().atual()).isZero();
    }

    @Test
    @DisplayName("responder um desafio conta o dia e acende a sequencia")
    void responderContaODia() {
        responderUmDesafio();

        ResumoDoDia resumo = servicoDaSessao.doUsuario(usuario);

        assertThat(resumo.concluidos()).isEqualTo(1);
        // Um desafio conta o dia: a regra e essa, e nao a sessao inteira.
        assertThat(resumo.sequencia().atual()).isEqualTo(1);
        assertThat(resumo.sequencia().praticouHoje()).isTrue();
    }

    @Test
    @DisplayName("cumprir a meta fecha o dia")
    void cumprirAMetaFechaODia() {
        usuario.setMinutosPorDia(5);
        int meta = servicoDaSessao.doUsuario(usuario).meta();

        for (int i = 0; i < meta; i++) {
            responderUmDesafio();
        }

        ResumoDoDia resumo = servicoDaSessao.doUsuario(usuario);

        assertThat(resumo.metaAlcancada()).isTrue();
        assertThat(resumo.restantes()).isZero();
    }

    @Test
    @DisplayName("passar da meta nao vira saldo negativo")
    void passarDaMetaNaoViraDivida() {
        usuario.setMinutosPorDia(5);
        int meta = servicoDaSessao.doUsuario(usuario).meta();

        for (int i = 0; i < meta + 2; i++) {
            responderUmDesafio();
        }

        assertThat(servicoDaSessao.doUsuario(usuario).restantes()).isZero();
    }

    @Test
    @DisplayName("conceito recem-praticado nao aparece como revisao pendente")
    void praticaRecenteNaoEntraNaRevisao() {
        responderUmDesafio();

        // O decaimento so comeca depois da tolerancia: avisar hoje sobre o que foi
        // praticado hoje seria ruido puro.
        assertThat(servicoDaSessao.doUsuario(usuario).revisoes()).isEmpty();
    }

    @Test
    @DisplayName("conceito parado ha semanas aparece como revisao, com a queda medida")
    void conceitoParadoViraRevisao() {
        responderUmDesafio();
        // A nota e fixada de proposito, em vez de herdar o que o avaliador simulado
        // pontuou: o teste e sobre o decaimento, e amarrar a asercao ao veredito do
        // simulado faria a mudanca de faixa depender de algo que nao esta em teste.
        envelhecerPraticasCom(new BigDecimal("9.50"), 40);

        List<RevisaoPendente> revisoes = servicoDaSessao.doUsuario(usuario).revisoes();

        assertThat(revisoes).hasSize(1);
        RevisaoPendente revisao = revisoes.get(0);

        // Meia-vida de 30 dias, com 3 de tolerancia: 37 dias contam, e sobra pouco mais
        // de 40% da nota. O ponto do teste e a queda ser real e medida, nao estimada.
        assertThat(revisao.notaQuandoPraticou()).isEqualByComparingTo("9.50");
        assertThat(revisao.notaHoje()).isLessThan(revisao.notaQuandoPraticou());
        assertThat(revisao.queda()).isEqualByComparingTo(
                revisao.notaQuandoPraticou().subtract(revisao.notaHoje()));
        assertThat(revisao.diasSemPraticar()).isEqualTo(40);
        // Saiu do verde: e essa queda que pode fechar o modulo seguinte sem o aluno errar nada.
        assertThat(revisao.mudouDeFaixa()).isTrue();
        assertThat(revisao.faixaHoje()).isNotEqualTo(FaixaDeNota.VERDE);
    }

    @Test
    @DisplayName("nota presumida pelo nivelamento nao vira revisao")
    void notaPresumidaNaoViraRevisao() {
        // Presumida nao tem data de pratica, entao nao decai. Mandar revisar o que nunca
        // foi medido seria inventar um esquecimento que nao aconteceu.
        notaRepositorio.save(new NotaDoModulo(usuario, moduloDe("artigos"), new BigDecimal("7.00")));

        assertThat(servicoDaSessao.doUsuario(usuario).revisoes()).isEmpty();
    }

    @Test
    @DisplayName("o dia de uma conta nao enxerga a pratica da outra")
    void diaNaoVazaEntreContas() {
        responderUmDesafio();
        Usuario outro = conta.garantirQueOOutroExiste();

        ResumoDoDia doOutro = servicoDaSessao.doUsuario(outro);

        assertThat(doOutro.concluidos()).isZero();
        assertThat(doOutro.sequencia().atual()).isZero();
    }

    private void responderUmDesafio() {
        ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario);
        servicoDeDesafio.responder(usuario, desafio.id(), "I am Brazilian.");
    }

    /**
     * Joga a ultima pratica para o passado.
     *
     * <p>O decaimento e calculado na leitura, entao mexer na data e o unico jeito de
     * exercitar semanas de esquecimento sem esperar semanas.
     */
    private void envelhecerPraticasCom(BigDecimal nota, int dias) {
        for (NotaDoModulo notaDoModulo : notaRepositorio.listarPorUsuario(usuario.getId())) {
            notaDoModulo.registrarPratica(nota, LocalDateTime.now().minusDays(dias));
        }
    }

    private Modulo moduloDe(String codigo) {
        return moduloRepositorio.listarTodosComPreRequisitos().stream()
                .filter(modulo -> modulo.getCodigo().equals(codigo))
                .findFirst()
                .orElseThrow();
    }
}
