package br.com.agenteingles.desafio;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
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
 * O aviso de erro repetido.
 *
 * <p>Usa o avaliador simulado, que aponta {@code concordancia_do_verbo_to_be} de forma
 * deterministica para uma frase com "I are" — o teste precisa do mesmo tipo de erro
 * saindo tres vezes, e nao de um veredito que pode variar.
 */
@SpringBootTest
@Transactional
class ReforcoDeErroIT {

    /** Concordancia errada de propriedade: o simulado sempre aponta o mesmo tipo aqui. */
    private static final String RESPOSTA_COM_ERRO_DE_CONCORDANCIA = "I are Brazilian.";

    private static final String TIPO_ESPERADO = "concordancia_do_verbo_to_be";

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
    }

    @AfterEach
    void encerrarSessao() {
        conta.limparContexto();
    }

    @Test
    @DisplayName("errar uma vez nao dispara aviso nenhum")
    void primeiroErroNaoAvisa() {
        assertThat(errarUmaVez().reforco()).isNull();
    }

    @Test
    @DisplayName("errar duas vezes ainda nao avisa: cabe em distracao")
    void segundoErroAindaNaoAvisa() {
        errarUmaVez();

        assertThat(errarUmaVez().reforco()).isNull();
    }

    @Test
    @DisplayName("na terceira vez o app para e mostra o padrao")
    void terceiroErroAvisa() {
        errarUmaVez();
        errarUmaVez();

        ReforcoDeErro reforco = errarUmaVez().reforco();

        assertThat(reforco).isNotNull();
        assertThat(reforco.tipo()).isEqualTo(TIPO_ESPERADO);
        assertThat(reforco.vezes()).isEqualTo(3);
    }

    @Test
    @DisplayName("o aviso mostra as tentativas anteriores, sem repetir a de agora")
    void avisoMostraAsTentativasAnteriores() {
        errarUmaVez();
        errarUmaVez();

        ReforcoDeErro reforco = errarUmaVez().reforco();

        // Duas anteriores, nao tres: repetir na tela o erro que o aluno acabou de ver
        // logo acima nao acrescenta nada.
        assertThat(reforco.anteriores()).hasSize(2);
        assertThat(reforco.anteriores())
                .allSatisfy(anterior -> assertThat(anterior.trechoErrado()).isNotBlank());
    }

    @Test
    @DisplayName("o aviso aponta o modulo onde o conceito e ensinado")
    void avisoApontaOModuloDoConceito() {
        errarUmaVez();
        errarUmaVez();

        ReforcoDeErro reforco = errarUmaVez().reforco();

        assertThat(reforco.moduloDoConceito()).isEqualTo("verbo_to_be");
        assertThat(reforco.moduloDoConceitoNome()).isNotBlank();
    }

    @Test
    @DisplayName("o tipo vira rotulo legivel em vez de chave crua")
    void tipoViraRotuloLegivel() {
        assertThat(ReforcoDeErro.rotuloDe(TIPO_ESPERADO))
                .isEqualTo("Concordancia do verbo to be");
        assertThat(ReforcoDeErro.rotuloDe(null)).isEqualTo("Erro recorrente");
    }

    @Test
    @DisplayName("a contagem e por conta: o erro de um nao aciona o aviso do outro")
    void contagemNaoVazaEntreContas() {
        errarUmaVez();
        errarUmaVez();

        Usuario outro = conta.garantirQueOOutroExiste();
        ResumoDoDesafio desafioDoOutro = servicoDeDesafio.proximoDesafio(outro);
        ResultadoDaResposta doOutro = servicoDeDesafio.responder(
                outro, desafioDoOutro.id(), RESPOSTA_COM_ERRO_DE_CONCORDANCIA);

        // Seria o terceiro erro no total, mas o primeiro desta conta.
        assertThat(doOutro.reforco()).isNull();
    }

    private ResultadoDaResposta errarUmaVez() {
        ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario, "verbo_to_be");
        return servicoDeDesafio.responder(usuario, desafio.id(), RESPOSTA_COM_ERRO_DE_CONCORDANCIA);
    }
}
