package br.com.agenteingles.nivelamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.comum.RegraDeNegocioException;
import br.com.agenteingles.desafio.ResumoDoDesafio;
import br.com.agenteingles.desafio.ServicoDeDesafio;
import br.com.agenteingles.modulo.NivelCefr;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * O nivelamento de entrada contra o banco de verdade.
 *
 * <p>O que estes testes protegem nao e o texto do veredito, e sim o efeito dele: um nivel
 * estimado que nao move o ponto de partida da trilha nao serve para nada.
 */
@SpringBootTest
@Transactional
class NivelamentoIT {

    /** Respostas com substancia suficiente para o agente simulado contar como tentativa. */
    private static final String RESPOSTA_A1 = "My name is Luan, I am from Brazil and I work as a developer.";
    private static final String RESPOSTA_A2 = "Last weekend I visited my family and we cooked lunch together.";
    private static final String RESPOSTA_B1 = "I would change the meetings, because they take the time I need to focus.";

    @Autowired
    private ServicoDeNivelamento servicoDeNivelamento;

    @Autowired
    private ServicoDeDesafio servicoDeDesafio;

    @Autowired
    private ServicoDeUsuario servicoDeUsuario;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @Autowired
    private ContaDeTeste conta;

    private Usuario usuario;

    @BeforeEach
    void prepararLinhaDeBase() {
        limpeza.limparHistoricoENotas();
        conta.autenticar();
        usuario = servicoDeUsuario.usuarioAtual();
        usuario.setNivelEstimado(null);
    }

    @AfterEach
    void encerrarSessao() {
        conta.limparContexto();
    }

    @Test
    @DisplayName("a conversa comeca na pergunta mais simples e anuncia quantas sao")
    void comecaNaPerguntaMaisSimples() {
        EtapaDoNivelamento etapa = servicoDeNivelamento.iniciar(usuario);

        assertThat(etapa.ordem()).isEqualTo(1);
        assertThat(etapa.total()).isEqualTo(PerguntaDoNivelamento.quantidade());
        assertThat(etapa.perguntaAtual().nivelAlvo()).isEqualTo(NivelCefr.A1);
        assertThat(etapa.resultado()).isNull();
    }

    @Test
    @DisplayName("iniciar de novo retoma a conversa aberta em vez de comecar outra")
    void retomaAConversaAberta() {
        EtapaDoNivelamento primeira = servicoDeNivelamento.iniciar(usuario);
        servicoDeNivelamento.responder(usuario, primeira.id(), 1, RESPOSTA_A1);

        EtapaDoNivelamento retomada = servicoDeNivelamento.iniciar(usuario);

        assertThat(retomada.id()).isEqualTo(primeira.id());
        assertThat(retomada.ordem()).isEqualTo(2);
    }

    @Test
    @DisplayName("responder fora de ordem e recusado")
    void recusaRespostaForaDeOrdem() {
        EtapaDoNivelamento etapa = servicoDeNivelamento.iniciar(usuario);

        assertThatThrownBy(() -> servicoDeNivelamento.responder(usuario, etapa.id(), 3, RESPOSTA_B1))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("pergunta 1");
    }

    @Test
    @DisplayName("pular e sinal de teto: a conversa segue e o nivel para onde o aluno parou")
    void pularSegueEDefineOTeto() {
        EtapaDoNivelamento resultado = conversar(RESPOSTA_A1, RESPOSTA_A2, RESPOSTA_B1, "", "");

        assertThat(resultado.perguntaAtual()).isNull();
        assertThat(resultado.resultado().nivel()).isEqualTo("B1");
    }

    @Test
    @DisplayName("o nivel estimado muda o ponto de partida da trilha, nao so o perfil")
    void nivelEstimadoMudaOPontoDePartida() {
        conversar(RESPOSTA_A1, RESPOSTA_A2, RESPOSTA_B1, "", "");

        ResumoDoDesafio primeiroDesafio = servicoDeDesafio.proximoDesafio(usuario);

        // Sem nivelamento este desafio cairia em verbo_to_be, o primeiro A1 da trilha.
        assertThat(primeiroDesafio.moduloCodigo()).isEqualTo("presente_perfeito");
    }

    @Test
    @DisplayName("os modulos abaixo do nivel ficam presumidos, e nao aprovados")
    void modulosAbaixoFicamPresumidos() {
        EtapaDoNivelamento resultado = conversar(RESPOSTA_A1, RESPOSTA_A2, RESPOSTA_B1, "", "");

        // Sete A1 e A2 estao abaixo de B1: quatro de A1 e tres de A2.
        assertThat(resultado.resultado().modulosLiberados()).isEqualTo(7);

        // Presumido e amarelo, nao verde: a pessoa ainda nao demonstrou nada aqui.
        assertThat(servicoDeUsuario.usuarioAtual().getNivelEstimado()).isEqualTo(NivelCefr.B1);
    }

    @Test
    @DisplayName("quem nao responde nada comeca do inicio, sem ser mandado para um nivel alto")
    void semRespostaComecaDoInicio() {
        EtapaDoNivelamento resultado = conversar("", "", "", "", "");

        assertThat(resultado.resultado().nivel()).isEqualTo("A1");
        assertThat(resultado.resultado().modulosLiberados()).isZero();
        assertThat(servicoDeDesafio.proximoDesafio(usuario).moduloCodigo()).isEqualTo("verbo_to_be");
    }

    @Test
    @DisplayName("abandonar conta como decisao: o app nao oferece o nivelamento de novo")
    void abandonarContaComoDecisao() {
        // Apareceu ao usar a tela: quem clicava em "prefiro comecar do inicio" caia na
        // trilha, atualizava a pagina e estava de volta na pergunta 1. A saida virava laco.
        EtapaDoNivelamento etapa = servicoDeNivelamento.iniciar(usuario);
        servicoDeNivelamento.abandonar(usuario, etapa.id());

        assertThat(servicoDeNivelamento.jaFezNivelamento(usuario)).isTrue();
    }

    @Test
    @DisplayName("depois de abandonar da para recomecar do zero")
    void depoisDeAbandonarDaParaRecomecar() {
        // Contrapartida do teste acima: se abandonar vale para sempre, precisa ter volta.
        EtapaDoNivelamento primeira = servicoDeNivelamento.iniciar(usuario);
        servicoDeNivelamento.abandonar(usuario, primeira.id());

        EtapaDoNivelamento nova = servicoDeNivelamento.iniciar(usuario);

        assertThat(nova.id()).isNotEqualTo(primeira.id());
        assertThat(nova.ordem()).isEqualTo(1);
    }

    @Test
    @DisplayName("dois inicios simultaneos nao criam duas conversas")
    void doisIniciosSimultaneosNaoCriamDuasConversas() {
        // Aconteceu de verdade na primeira execucao pelo navegador: o modo estrito do
        // React chama o efeito duas vezes, as duas requisicoes passaram juntas pelo
        // "ja existe?" e a segunda morreu no indice unico, com 500 na tela.
        EtapaDoNivelamento primeira = servicoDeNivelamento.iniciar(usuario);
        EtapaDoNivelamento segunda = servicoDeNivelamento.iniciar(usuario);

        assertThat(segunda.id()).isEqualTo(primeira.id());
    }

    @Test
    @DisplayName("o nivelamento de outra conta nao e alcancavel trocando o id")
    void nivelamentoDeOutraContaNaoEAlcancavel() {
        Usuario outro = conta.garantirQueOOutroExiste();
        EtapaDoNivelamento doOutro = servicoDeNivelamento.iniciar(outro);

        assertThatThrownBy(() -> servicoDeNivelamento.responder(usuario, doOutro.id(), 1, RESPOSTA_A1))
                .hasMessageContaining("não encontrado");
    }

    /** Percorre a escada inteira e devolve a etapa final. */
    private EtapaDoNivelamento conversar(String... respostas) {
        EtapaDoNivelamento etapa = servicoDeNivelamento.iniciar(usuario);
        for (int ordem = 1; ordem <= respostas.length; ordem++) {
            etapa = servicoDeNivelamento.responder(usuario, etapa.id(), ordem, respostas[ordem - 1]);
        }
        return etapa;
    }
}
