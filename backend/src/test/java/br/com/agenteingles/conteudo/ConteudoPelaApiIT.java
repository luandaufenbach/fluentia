package br.com.agenteingles.conteudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Conteudo pela camada HTTP, <b>sem</b> transacao de teste em volta.
 *
 * <p>Como no {@code DesafioPelaApiIT}, a ausencia de transacao e o ponto: as duas
 * colecoes do conteudo sao lazy, e com uma transacao aberta pelo teste elas
 * continuariam carregando mesmo que o servico estivesse errado. Foi assim que
 * passaram despercebidos tanto o vazamento de entidade quanto a busca das duas
 * colecoes numa consulta so, que estoura com MultipleBagFetchException.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConteudoPelaApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("entrega explicacao, exemplos e erros comuns do modulo")
    void entregaOConteudoCompleto() throws Exception {
        mockMvc.perform(get("/api/modulos/verbo_to_be/conteudo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduloCodigo").value("verbo_to_be"))
                .andExpect(jsonPath("$.nivel").value("A1"))
                .andExpect(jsonPath("$.resumo").isNotEmpty())
                .andExpect(jsonPath("$.explicacao").isNotEmpty())
                .andExpect(jsonPath("$.exemplos").isNotEmpty())
                .andExpect(jsonPath("$.exemplos[0].emIngles").isNotEmpty())
                .andExpect(jsonPath("$.exemplos[0].emPortugues").isNotEmpty())
                .andExpect(jsonPath("$.errosComuns").isNotEmpty())
                .andExpect(jsonPath("$.errosComuns[0].errado").isNotEmpty())
                .andExpect(jsonPath("$.errosComuns[0].certo").isNotEmpty());
    }

    @Test
    @DisplayName("todo modulo do curriculo tem conteudo para estudar")
    void todoModuloTemConteudo() throws Exception {
        String corpo = mockMvc.perform(get("/api/modulos"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Sem isto, um modulo sem material so apareceria quando o aluno chegasse nele.
        for (String codigo : codigosNaResposta(corpo)) {
            mockMvc.perform(get("/api/modulos/{codigo}/conteudo", codigo))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("modulo inexistente responde 404, nao erro interno")
    void moduloInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/modulos/modulo_que_nao_existe/conteudo"))
                .andExpect(status().isNotFound());
    }

    private java.util.List<String> codigosNaResposta(String corpo) {
        var codigos = new java.util.ArrayList<String>();
        var padrao = java.util.regex.Pattern.compile("\"codigo\":\"([^\"]+)\"");
        var busca = padrao.matcher(corpo);
        while (busca.find()) {
            codigos.add(busca.group(1));
        }
        assertThat(codigos).hasSize(16);
        return codigos;
    }
}
