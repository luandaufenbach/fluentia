package br.com.agenteingles.agente.simulado;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.desafio.CatalogoDeTiposDeErro;
import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.modulo.ModuloRepositorio;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * O banco de alvos do modo simulado.
 *
 * <p>O que estes testes protegem e a promessa que o modo simulado faz: nota que significa
 * alguma coisa em <b>todos</b> os modulos. Um modulo sem alvo volta a devolver desafio sem
 * gabarito, e sem gabarito a nota vira um numero fixo.
 */
@SpringBootTest
@Transactional
class BancoDeAlvosTest {

    /** Abaixo disso o banco esgota rapido e o aluno reve os mesmos enunciados. */
    private static final int ALVOS_MINIMOS_POR_MODULO = 6;

    @Autowired
    private ModuloRepositorio moduloRepositorio;

    @Test
    @DisplayName("todo modulo do curriculo tem banco de alvos")
    void todoModuloTemBanco() {
        List<Modulo> modulos = moduloRepositorio.listarTodosComPreRequisitos();

        assertThat(modulos).isNotEmpty();
        assertThat(modulos).allSatisfy(modulo ->
                assertThat(BancoDeAlvos.doModulo(modulo.getCodigo()))
                        .as("banco de alvos do modulo %s", modulo.getCodigo())
                        .isNotNull());
    }

    @Test
    @DisplayName("cada banco tem alvos suficientes para nao repetir logo")
    void bancoTemAlvosSuficientes() {
        for (Modulo modulo : moduloRepositorio.listarTodosComPreRequisitos()) {
            assertThat(BancoDeAlvos.doModulo(modulo.getCodigo()).alvos())
                    .as("alvos do modulo %s", modulo.getCodigo())
                    .hasSizeGreaterThanOrEqualTo(ALVOS_MINIMOS_POR_MODULO);
        }
    }

    @Test
    @DisplayName("todo alvo tem as duas pontas preenchidas")
    void alvoTemAsDuasPontas() {
        // Referencia vazia derruba o avaliador simulado de volta para a nota fixa, que e
        // exatamente o problema que este banco existe para resolver.
        for (Modulo modulo : moduloRepositorio.listarTodosComPreRequisitos()) {
            assertThat(BancoDeAlvos.doModulo(modulo.getCodigo()).alvos())
                    .allSatisfy(alvo -> {
                        assertThat(alvo.emPortugues()).isNotBlank();
                        assertThat(alvo.emIngles()).isNotBlank();
                    });
        }
    }

    @Test
    @DisplayName("nao ha frase em portugues repetida dentro do mesmo modulo")
    void semRepeticaoDentroDoModulo() {
        for (Modulo modulo : moduloRepositorio.listarTodosComPreRequisitos()) {
            List<String> frases = BancoDeAlvos.doModulo(modulo.getCodigo()).alvos().stream()
                    .map(BancoDeAlvos.Alvo::emPortugues)
                    .toList();

            assertThat(frases)
                    .as("frases do modulo %s", modulo.getCodigo())
                    .doesNotHaveDuplicates();
        }
    }

    @Test
    @DisplayName("o tipo de erro tipico vem do catalogo")
    void tipoTipicoVemDoCatalogo() {
        // Tipo fora do catalogo agregaria mal na contagem de repeticao, que e justamente
        // o que o modo simulado precisa conseguir exercitar.
        for (Modulo modulo : moduloRepositorio.listarTodosComPreRequisitos()) {
            String tipo = BancoDeAlvos.doModulo(modulo.getCodigo()).tipoDeErroTipico();

            assertThat(CatalogoDeTiposDeErro.TIPOS)
                    .as("tipo tipico do modulo %s", modulo.getCodigo())
                    .contains(tipo);
        }
    }
}
