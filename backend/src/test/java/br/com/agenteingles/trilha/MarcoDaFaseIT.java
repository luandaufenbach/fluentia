package br.com.agenteingles.trilha;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.desafio.ResumoDoDesafio;
import br.com.agenteingles.desafio.ServicoDeDesafio;
import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.modulo.ModuloRepositorio;
import br.com.agenteingles.nota.NotaDoModulo;
import br.com.agenteingles.nota.NotaDoModuloRepositorio;
import br.com.agenteingles.orquestrador.Orquestrador;
import br.com.agenteingles.trilha.TrilhaController.FaseNaTrilhaResposta;
import br.com.agenteingles.trilha.TrilhaController.TrilhaResposta;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * O marco da fase separa o que foi demonstrado do que foi presumido.
 *
 * <p>O marco e uma frase — "se apresentar e falar da sua rotina sem travar" — e da-la por
 * cumprida a partir de uma estimativa do nivelamento contradiz o que o produto promete: o
 * estado vem da nota real, nao de um clique.
 */
@SpringBootTest
@Transactional
class MarcoDaFaseIT {

    /** Os quatro conceitos de A1, que formam a primeira fase. */
    private static final List<String> MODULOS_DA_PRIMEIRA_FASE =
            List.of("verbo_to_be", "artigos", "pronomes_pessoais", "presente_simples");

    private static final BigDecimal NOTA_PRESUMIDA = new BigDecimal("7.00");

    @Autowired
    private ServicoDeTrilha servicoDeTrilha;

    @Autowired
    private ServicoDeDesafio servicoDeDesafio;

    @Autowired
    private ServicoDeUsuario servicoDeUsuario;

    @Autowired
    private NotaDoModuloRepositorio notaRepositorio;

    @Autowired
    private ModuloRepositorio moduloRepositorio;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @Autowired
    private Orquestrador orquestrador;

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
    @DisplayName("fase sem nenhuma nota fica pendente")
    void faseSemNotaFicaPendente() {
        FaseNaTrilhaResposta primeira = primeiraFase();

        assertThat(primeira.situacaoDoMarco()).isEqualTo(SituacaoDoMarco.PENDENTE);
        assertThat(primeira.modulosConsolidados()).isZero();
        assertThat(primeira.modulosPresumidos()).isZero();
    }

    @Test
    @DisplayName("fase inteira presumida pelo nivelamento nao anuncia marco alcancado")
    void fasePresumidaNaoAnunciaMarco() {
        // Era este o caso que fechava a fase com o mesmo tique de quem praticou.
        presumirAPrimeiraFase();

        FaseNaTrilhaResposta primeira = primeiraFase();

        assertThat(primeira.situacaoDoMarco()).isEqualTo(SituacaoDoMarco.PRESUMIDO);
        assertThat(primeira.modulosPresumidos()).isEqualTo(MODULOS_DA_PRIMEIRA_FASE.size());
        assertThat(primeira.modulosConsolidados()).isZero();
    }

    @Test
    @DisplayName("fase presumida nao aparece como em andamento")
    void fasePresumidaNaoEstaEmAndamento() {
        // "Voce esta aqui" precisa apontar para onde o aluno esta de fato. Uma fase que
        // ele nunca abriu nao pode reivindicar essa marca so por ter nota estimada.
        presumirAPrimeiraFase();

        assertThat(primeiraFase().emAndamento()).isFalse();
    }

    @Test
    @DisplayName("o \"voce esta aqui\" cai na mesma fase do proximo passo")
    void marcadorDePosicaoConcordaComOProximoPasso() {
        // Antes o marcador era "fase ja encostada e nao fechada", e a tela conseguia
        // mostrar "voce esta aqui" numa fase com o "proximo passo" apontando outra.
        presumirAPrimeiraFase();
        praticarAteSairDoVermelho("verbo_to_be");

        String faseComMarcador = servicoDeTrilha.montar(usuario).fases().stream()
                .filter(FaseNaTrilhaResposta::emAndamento)
                .map(FaseNaTrilhaResposta::codigo)
                .findFirst()
                .orElseThrow();

        String faseDoProximoPasso = orquestrador.decidirProximaPratica(usuario)
                .situacaoDoModulo().modulo().getFase().getCodigo();

        assertThat(faseComMarcador).isEqualTo(faseDoProximoPasso);
    }

    @Test
    @DisplayName("uma fase de cada vez carrega o marcador")
    void apenasUmaFaseCarregaOMarcador() {
        presumirAPrimeiraFase();
        praticarAteSairDoVermelho("verbo_to_be");

        assertThat(servicoDeTrilha.montar(usuario).fases())
                .filteredOn(FaseNaTrilhaResposta::emAndamento)
                .hasSize(1);
    }

    @Test
    @DisplayName("praticar todos os conceitos da fase alcanca o marco")
    void praticarTodosAlcancaOMarco() {
        for (String codigo : MODULOS_DA_PRIMEIRA_FASE) {
            praticarAteSairDoVermelho(codigo);
        }

        FaseNaTrilhaResposta primeira = primeiraFase();

        assertThat(primeira.situacaoDoMarco()).isEqualTo(SituacaoDoMarco.ALCANCADO);
        assertThat(primeira.modulosConsolidados()).isEqualTo(MODULOS_DA_PRIMEIRA_FASE.size());
        assertThat(primeira.modulosPresumidos()).isZero();
    }

    @Test
    @DisplayName("um conceito ainda presumido segura o marco em presumido")
    void umPresumidoSeguraOMarco() {
        presumirAPrimeiraFase();
        // Tres dos quatro passam a ser praticados; o quarto continua so estimado.
        MODULOS_DA_PRIMEIRA_FASE.stream().limit(3).forEach(this::praticarAteSairDoVermelho);

        FaseNaTrilhaResposta primeira = primeiraFase();

        assertThat(primeira.situacaoDoMarco()).isEqualTo(SituacaoDoMarco.PRESUMIDO);
        assertThat(primeira.modulosConsolidados()).isEqualTo(3);
        assertThat(primeira.modulosPresumidos()).isEqualTo(1);
    }

    @Test
    @DisplayName("o total da trilha separa demonstrado de presumido")
    void totalSeparaDemonstradoDePresumido() {
        presumirAPrimeiraFase();
        praticarAteSairDoVermelho("verbo_to_be");

        TrilhaResposta trilha = servicoDeTrilha.montar(usuario);

        assertThat(trilha.modulosConsolidados()).isEqualTo(1);
        assertThat(trilha.modulosPresumidos()).isEqualTo(MODULOS_DA_PRIMEIRA_FASE.size() - 1);
        assertThat(trilha.totalDeModulos()).isEqualTo(16);
    }

    @Test
    @DisplayName("nota presumida continua liberando o modulo seguinte")
    void presumidoContinuaLiberandoOCaminho() {
        // A contrapartida obrigatoria: rebaixar a presuncao a pendente resolveria a
        // honestidade e devolveria quem foi nivelado em B1 para o comeco da trilha.
        presumirAPrimeiraFase();

        assertThat(primeiraFase().modulos())
                .allSatisfy(modulo -> assertThat(modulo.liberado()).isTrue());
    }

    private FaseNaTrilhaResposta primeiraFase() {
        return servicoDeTrilha.montar(usuario).fases().get(0);
    }

    private void presumirAPrimeiraFase() {
        for (String codigo : MODULOS_DA_PRIMEIRA_FASE) {
            notaRepositorio.save(new NotaDoModulo(usuario, moduloDe(codigo), NOTA_PRESUMIDA));
        }
    }

    /**
     * Pratica de verdade e depois fixa a nota.
     *
     * <p>A pratica cria a linha com a contagem de praticas; a nota e fixada depois porque
     * o que esta em teste e o marco, e nao o veredito do avaliador simulado.
     */
    private void praticarAteSairDoVermelho(String codigo) {
        ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario, codigo);
        servicoDeDesafio.responder(usuario, desafio.id(), "I am Brazilian.");

        NotaDoModulo nota = notaRepositorio
                .buscarPorUsuarioEModulo(usuario.getId(), moduloDe(codigo).getId())
                .orElseThrow();
        nota.registrarPratica(new BigDecimal("8.00"), java.time.LocalDateTime.now());
    }

    private Modulo moduloDe(String codigo) {
        return moduloRepositorio.buscarPorCodigo(codigo).orElseThrow();
    }
}
