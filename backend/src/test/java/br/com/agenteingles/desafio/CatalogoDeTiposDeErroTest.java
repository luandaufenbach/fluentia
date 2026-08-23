package br.com.agenteingles.desafio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatalogoDeTiposDeErroTest {

    @Test
    @DisplayName("tipo ja no formato do catalogo passa intacto")
    void tipoDoCatalogoPassaIntacto() {
        assertThat(CatalogoDeTiposDeErro.normalizar("concordancia_do_verbo_to_be"))
                .isEqualTo("concordancia_do_verbo_to_be");
    }

    @Test
    @DisplayName("acento, maiuscula e espaco viram a mesma chave")
    void variacoesViramAMesmaChave() {
        // Sem isto, "Concordância do verbo to be" e "concordancia_do_verbo_to_be" seriam
        // dois erros distintos para a contagem — e o aviso de repeticao nunca chegaria a tres.
        assertThat(CatalogoDeTiposDeErro.normalizar("Concordância do verbo to be"))
                .isEqualTo("concordancia_do_verbo_to_be");
        assertThat(CatalogoDeTiposDeErro.normalizar("  USO_DE_ARTIGO  "))
                .isEqualTo("uso_de_artigo");
    }

    @Test
    @DisplayName("tipo vazio ou nulo vira outro")
    void vazioViraOutro() {
        assertThat(CatalogoDeTiposDeErro.normalizar(null)).isEqualTo("outro");
        assertThat(CatalogoDeTiposDeErro.normalizar("   ")).isEqualTo("outro");
        assertThat(CatalogoDeTiposDeErro.normalizar("!!!")).isEqualTo("outro");
    }

    @Test
    @DisplayName("tipo fora do catalogo e mantido, nao descartado")
    void tipoDesconhecidoEMantido() {
        // Trocar por "outro" jogaria fora o que o avaliador viu. Perder o especifico e
        // pior do que ter uma chave que agrega mal — e o log avisa para a lista crescer.
        assertThat(CatalogoDeTiposDeErro.normalizar("uso_de_gerundio"))
                .isEqualTo("uso_de_gerundio");
    }

    @Test
    @DisplayName("a lista do prompt traz todos os tipos")
    void listaDoPromptTrazTodos() {
        String paraOPrompt = CatalogoDeTiposDeErro.paraOPrompt();

        assertThat(CatalogoDeTiposDeErro.TIPOS)
                .allSatisfy(tipo -> assertThat(paraOPrompt).contains(tipo));
    }

    @Test
    @DisplayName("todo tipo do catalogo ja esta normalizado")
    void catalogoEstaNormalizado() {
        // Um item do catalogo escrito fora do formato jamais bateria com o que sai do
        // normalizador, e a lista deixaria de servir para o que ela existe.
        assertThat(CatalogoDeTiposDeErro.TIPOS)
                .allSatisfy(tipo -> assertThat(CatalogoDeTiposDeErro.normalizar(tipo)).isEqualTo(tipo));
    }
}
