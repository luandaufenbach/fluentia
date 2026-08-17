package br.com.agenteingles.desafio;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Usuario usuario;

    @BeforeEach
    void resolverUsuario() {
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
        Desafio desafio = servicoDeDesafio.proximoDesafio(usuario);

        assertThat(desafio.getModulo().getCodigo()).isEqualTo("verbo_to_be");
        assertThat(desafio.getStatus()).isEqualTo(StatusDoDesafio.AGUARDANDO_RESPOSTA);
        assertThat(desafio.getEnunciado()).isNotBlank();
        assertThat(desafio.getMotivoDaEscolha()).contains("ainda nao praticado");
    }

    @Test
    @DisplayName("enquanto o desafio nao e respondido, o proximo devolve o mesmo desafio")
    void naoGeraOutroDesafioComUmEmAberto() {
        Desafio primeiro = servicoDeDesafio.proximoDesafio(usuario);
        Desafio segundo = servicoDeDesafio.proximoDesafio(usuario);

        assertThat(segundo.getId()).isEqualTo(primeiro.getId());
    }

    @Test
    @DisplayName("resposta errada derruba a nota do modulo e registra o erro especifico")
    void respostaErradaDerrubaANotaERegistraOErro() {
        assertThat(situacaoDe("verbo_to_be").nota()).isNull();

        Desafio desafio = servicoDeDesafio.proximoDesafio(usuario);
        ResultadoDaResposta resultado = servicoDeDesafio.responder(usuario, desafio.getId(), "I are wrong");

        assertThat(resultado.avaliacao().getErrosDetectados()).isNotEmpty();
        assertThat(resultado.avaliacao().getErrosDetectados().get(0).getTipo())
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
        Desafio primeiro = servicoDeDesafio.proximoDesafio(usuario);
        servicoDeDesafio.responder(usuario, primeiro.getId(), "I are wrong");

        Desafio segundo = servicoDeDesafio.proximoDesafio(usuario);

        assertThat(segundo.getId()).isNotEqualTo(primeiro.getId());
        assertThat(segundo.getModulo().getCodigo()).isEqualTo("verbo_to_be");
        assertThat(segundo.getMotivoDaEscolha()).contains("vermelho");
        assertThat(segundo.getEnunciado()).isNotEqualTo(primeiro.getEnunciado());
    }

    @Test
    @DisplayName("acertando seguidamente a nota sobe, o modulo fica verde e o curriculo avanca")
    void acertosConsecutivosLiberamOProximoModulo() {
        for (int rodada = 0; rodada < 4; rodada++) {
            Desafio desafio = servicoDeDesafio.proximoDesafio(usuario);
            if (!desafio.getModulo().getCodigo().equals("verbo_to_be")) {
                break;
            }
            servicoDeDesafio.responder(usuario, desafio.getId(), desafio.getRespostaDeReferencia());
        }

        SituacaoDoModulo verboToBe = situacaoDe("verbo_to_be");
        assertThat(verboToBe.nota()).isEqualByComparingTo("10.00");
        assertThat(verboToBe.faixa()).isEqualTo(FaixaDeNota.VERDE);

        // Com o pre-requisito consolidado, os modulos que dependiam dele saem do bloqueio.
        SituacaoDoModulo artigos = situacaoDe("artigos");
        assertThat(artigos.liberado()).isTrue();
        assertThat(artigos.preRequisitosPendentes()).isEmpty();

        // E o orquestrador passa a mirar o proximo conceito, ja que o anterior esta verde.
        Desafio proximo = servicoDeDesafio.proximoDesafio(usuario);
        assertThat(proximo.getModulo().getCodigo()).isNotEqualTo("verbo_to_be");
        assertThat(proximo.getMotivoDaEscolha()).contains("ainda nao praticado");
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
