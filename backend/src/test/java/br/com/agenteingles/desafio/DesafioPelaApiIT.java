package br.com.agenteingles.desafio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Exercita o loop pela camada HTTP, sem transacao de teste em volta.
 *
 * <p>E o que diferencia deste do {@link LoopDoDesafioIT}: com uma transacao aberta pelo teste,
 * associacoes lazy continuam carregando e um vazamento de entidade JPA para fora do servico
 * passa despercebido. Aqui a sessao ja fechou quando o controller serializa a resposta,
 * que foi exatamente onde o bug de LazyInitializationException apareceu.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DesafioPelaApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @BeforeEach
    @AfterEach
    void deixarOBancoNaLinhaDeBase() {
        // Este teste comita de verdade: limpa nos dois lados para nao vazar estado.
        limpeza.limparHistoricoENotas();
    }

    // O Boot 4 nao publica um ObjectMapper como bean por padrao, entao o teste cria o seu.
    private final ObjectMapper conversorJson = new ObjectMapper();

    private JsonNode chamar(org.springframework.test.web.servlet.RequestBuilder requisicao) throws Exception {
        String corpo = mockMvc.perform(requisicao)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return conversorJson.readTree(corpo);
    }

    @Test
    @DisplayName("buscar o proximo desafio duas vezes devolve o mesmo, sem estourar fora da transacao")
    void proximoDesafioFuncionaComDesafioJaEmAberto() throws Exception {
        JsonNode primeira = chamar(get("/api/desafios/proximo"));

        assertThat(primeira.get("moduloCodigo").asText()).isNotBlank();
        assertThat(primeira.get("temaNome").asText()).isNotBlank();

        // A segunda chamada le um desafio ja gravado: e aqui que a entidade lazy quebrava.
        JsonNode segunda = chamar(get("/api/desafios/proximo"));

        assertThat(segunda.get("id").asLong()).isEqualTo(primeira.get("id").asLong());
        assertThat(segunda.get("moduloNome").asText()).isNotBlank();
        assertThat(segunda.get("temaNome").asText()).isNotBlank();
    }

    @Test
    @DisplayName("pedir o mesmo modulo que ja esta em aberto reaproveita o desafio")
    void praticaDirigidaDoMesmoModuloNaoGeraOutro() throws Exception {
        JsonNode emAberto = chamar(get("/api/desafios/proximo"));

        JsonNode dirigido = chamar(get("/api/desafios/proximo")
                .param("modulo", emAberto.get("moduloCodigo").asText()));

        assertThat(dirigido.get("id").asLong()).isEqualTo(emAberto.get("id").asLong());
    }

    @Test
    @DisplayName("praticar modulo bloqueado por pre-requisito e recusado")
    void praticaDirigidaRespeitaPreRequisito() throws Exception {
        mockMvc.perform(get("/api/desafios/proximo").param("modulo", "expressoes_idiomaticas"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("praticar modulo inexistente devolve 404")
    void praticaDirigidaDeModuloInexistente() throws Exception {
        mockMvc.perform(get("/api/desafios/proximo").param("modulo", "nao_existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("o desafio devolvido pela API nao expoe a resposta de referencia")
    void apiNaoExpoeARespostaDeReferencia() throws Exception {
        JsonNode desafio = chamar(get("/api/desafios/proximo"));

        assertThat(desafio.has("respostaDeReferencia")).isFalse();
        assertThat(desafio.has("criterioDeAvaliacao")).isFalse();
    }

    @Test
    @DisplayName("responder pela API devolve a correcao com o erro e a nota do modulo recalculada")
    void responderPelaApiDevolveACorrecao() throws Exception {
        JsonNode desafio = chamar(get("/api/desafios/proximo"));
        long desafioId = desafio.get("id").asLong();

        JsonNode correcao = chamar(post("/api/desafios/" + desafioId + "/resposta")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resposta\":\"I are wrong\"}"));

        assertThat(correcao.get("desafioId").asLong()).isEqualTo(desafioId);
        assertThat(correcao.get("moduloNome").asText()).isNotBlank();
        assertThat(correcao.get("notaDoModulo").isNumber()).isTrue();
        assertThat(correcao.get("faixaDoModulo").asText()).isNotBlank();
        assertThat(correcao.get("erros").isArray()).isTrue();
    }

    @Test
    @DisplayName("resposta em branco e recusada com 400 e mensagem de validacao")
    void respostaEmBrancoERecusada() throws Exception {
        JsonNode desafio = chamar(get("/api/desafios/proximo"));

        var resultado = mockMvc.perform(post("/api/desafios/" + desafio.get("id").asLong() + "/resposta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resposta\":\"   \"}"))
                .andReturn();

        assertThat(resultado.getResponse().getStatus()).isEqualTo(400);
        assertThat(resultado.getResponse().getContentAsString()).contains("resposta");
    }

    @Test
    @DisplayName("responder um desafio inexistente devolve 404")
    void desafioInexistenteDevolve404() throws Exception {
        var resultado = mockMvc.perform(post("/api/desafios/999999/resposta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resposta\":\"I am here\"}"))
                .andReturn();

        assertThat(resultado.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("o curriculo vem agrupado por nivel CEFR com faixa de cor por modulo")
    void curriculoVemAgrupadoPorNivel() throws Exception {
        JsonNode niveis = chamar(get("/api/modulos"));

        assertThat(niveis.isArray()).isTrue();
        assertThat(niveis.get(0).get("nivel").asText()).isEqualTo("A1");

        JsonNode primeiroModulo = niveis.get(0).get("modulos").get(0);
        assertThat(primeiroModulo.get("codigo").asText()).isEqualTo("verbo_to_be");
        assertThat(primeiroModulo.get("faixa").asText()).isNotBlank();
        assertThat(primeiroModulo.get("liberado").asBoolean()).isTrue();
    }
}
