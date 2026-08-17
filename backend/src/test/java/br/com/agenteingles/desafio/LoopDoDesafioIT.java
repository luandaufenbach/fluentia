package br.com.agenteingles.desafio;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.modulo.ServicoDeModulo;
import br.com.agenteingles.modulo.SituacaoDoModulo;
import br.com.agenteingles.nota.FaixaDeNota;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loop de ponta a ponta com o banco de verdade: desafio gerado, resposta, avaliacao,
 * nota atualizada e o proximo desafio ja refletindo essa nota.
 *
 * <p>A transacao do teste e revertida ao final, entao rodar a suite nao suja o banco local.
 */
@SpringBootTest
@Transactional
class LoopDoDesafioIT {

    @Autowired
    private ServicoDeDesafio servicoDeDesafio;

    @Autowired
    private ServicoDeUsuario servicoDeUsuario;

    @Autowired
    private ServicoDeModulo servicoDeModulo;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    private Usuario usuario;

    @BeforeEach
    void prepararLinhaDeBase() {
        limpeza.limparHistoricoENotas();
        usuario = servicoDeUsuario.usuarioAtual();
    }

    private SituacaoDoModulo situacaoDe(String codigo) {
        return servicoDeModulo.situacaoDeTodosOsModulos(usuario).stream()
                .filter(situacao -> situacao.modulo().getCodigo().equals(codigo))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("o primeiro desafio cai no verbo to be, que e o unico A1 sem pre-requisito pendente")
    void primeiroDesafioCaiNoVerboToBe() {
        ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario);

        assertThat(desafio.moduloCodigo()).isEqualTo("verbo_to_be");
        assertThat(desafio.status()).isEqualTo(StatusDoDesafio.AGUARDANDO_RESPOSTA);
        assertThat(desafio.enunciado()).isNotBlank();
        assertThat(desafio.motivoDaEscolha()).contains("não foi praticado");
    }

    @Test
    @DisplayName("enquanto o desafio nao e respondido, o proximo devolve o mesmo desafio")
    void naoGeraOutroDesafioComUmEmAberto() {
        ResumoDoDesafio primeiro = servicoDeDesafio.proximoDesafio(usuario);
        ResumoDoDesafio segundo = servicoDeDesafio.proximoDesafio(usuario);

        assertThat(segundo.id()).isEqualTo(primeiro.id());
    }

    @Test
    @DisplayName("resposta errada derruba a nota do modulo e registra o erro especifico")
    void respostaErradaDerrubaANotaERegistraOErro() {
        assertThat(situacaoDe("verbo_to_be").nota()).isNull();

        ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario);
        ResultadoDaResposta resultado = servicoDeDesafio.responder(usuario, desafio.id(), "I are wrong");

        assertThat(resultado.erros()).isNotEmpty();
        assertThat(resultado.erros().get(0).tipo())
                .isEqualTo("concordancia_do_verbo_to_be");
        assertThat(resultado.notaDoModulo()).isLessThan(new java.math.BigDecimal("6"));
        assertThat(resultado.faixaDoModulo()).isEqualTo(FaixaDeNota.VERMELHO);

        SituacaoDoModulo situacao = situacaoDe("verbo_to_be");
        assertThat(situacao.quantidadeDePraticas()).isEqualTo(1);
        assertThat(situacao.faixa()).isEqualTo(FaixaDeNota.VERMELHO);
    }

    @Test
    @DisplayName("com o modulo em vermelho o orquestrador insiste nele em vez de avancar")
    void orquestradorInsisteNoModuloEmVermelho() {
        ResumoDoDesafio primeiro = servicoDeDesafio.proximoDesafio(usuario);
        servicoDeDesafio.responder(usuario, primeiro.id(), "I are wrong");

        ResumoDoDesafio segundo = servicoDeDesafio.proximoDesafio(usuario);

        assertThat(segundo.id()).isNotEqualTo(primeiro.id());
        assertThat(segundo.moduloCodigo()).isEqualTo("verbo_to_be");
        assertThat(segundo.motivoDaEscolha()).contains("vermelho");
        assertThat(segundo.enunciado()).isNotEqualTo(primeiro.enunciado());
    }

    @Test
    @DisplayName("praticar o modulo estudado troca o desafio em aberto pelo do modulo pedido")
    void praticaDirigidaTrocaODesafioEmAberto() {
        // Consolida o to be para liberar "artigos" e ter dois modulos disponiveis.
        for (int rodada = 0; rodada < 4; rodada++) {
            ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario);
            if (!desafio.moduloCodigo().equals("verbo_to_be")) {
                break;
            }
            servicoDeDesafio.responder(usuario, desafio.id(), desafio.respostaDeReferencia());
        }

        ResumoDoDesafio doOrquestrador = servicoDeDesafio.proximoDesafio(usuario);
        assertThat(doOrquestrador.moduloCodigo()).isNotEqualTo("verbo_to_be");

        // O aluno abriu o conteudo do "to be" e pediu para praticar justamente ele.
        ResumoDoDesafio dirigido = servicoDeDesafio.proximoDesafio(usuario, "verbo_to_be");

        assertThat(dirigido.moduloCodigo()).isEqualTo("verbo_to_be");
        assertThat(dirigido.id()).isNotEqualTo(doOrquestrador.id());
        assertThat(dirigido.motivoDaEscolha()).contains("escolheu praticar");

        // O anterior saiu da fila sem entrar no historico: nao havia resposta nele.
        assertThat(servicoDeDesafio.proximoDesafio(usuario).id()).isEqualTo(dirigido.id());
    }

    @Test
    @DisplayName("acertando seguidamente a nota sobe, o modulo fica verde e o curriculo avanca")
    void acertosConsecutivosLiberamOProximoModulo() {
        for (int rodada = 0; rodada < 4; rodada++) {
            ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario);
            if (!desafio.moduloCodigo().equals("verbo_to_be")) {
                break;
            }
            servicoDeDesafio.responder(usuario, desafio.id(), desafio.respostaDeReferencia());
        }

        SituacaoDoModulo verboToBe = situacaoDe("verbo_to_be");
        assertThat(verboToBe.nota()).isEqualByComparingTo("10.00");
        assertThat(verboToBe.faixa()).isEqualTo(FaixaDeNota.VERDE);

        // Com o pre-requisito consolidado, os modulos que dependiam dele saem do bloqueio.
        SituacaoDoModulo artigos = situacaoDe("artigos");
        assertThat(artigos.liberado()).isTrue();
        assertThat(artigos.preRequisitosPendentes()).isEmpty();

        // E o orquestrador passa a mirar o proximo conceito, ja que o anterior esta verde.
        ResumoDoDesafio proximo = servicoDeDesafio.proximoDesafio(usuario);
        assertThat(proximo.moduloCodigo()).isNotEqualTo("verbo_to_be");
        assertThat(proximo.motivoDaEscolha()).contains("não foi praticado");
    }

    @Test
    @DisplayName("modulos de nivel avancado ficam bloqueados enquanto o pre-requisito nao tem nota")
    void modulosAvancadosComecamBloqueados() {
        List<SituacaoDoModulo> situacoes = servicoDeModulo.situacaoDeTodosOsModulos(usuario);

        SituacaoDoModulo passadoSimples = situacaoDe("passado_simples");
        assertThat(passadoSimples.liberado()).isFalse();
        assertThat(passadoSimples.preRequisitosPendentes()).isNotEmpty();

        // Apenas o verbo "to be" nao depende de ninguem, entao e o unico ponto de entrada.
        List<String> liberados = situacoes.stream()
                .filter(SituacaoDoModulo::liberado)
                .map(situacao -> situacao.modulo().getCodigo())
                .toList();
        assertThat(liberados).containsExactly("verbo_to_be");
    }
}
