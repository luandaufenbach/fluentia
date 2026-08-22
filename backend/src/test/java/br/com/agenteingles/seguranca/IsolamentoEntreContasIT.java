package br.com.agenteingles.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

/**
 * Uma conta nao alcanca os dados da outra.
 *
 * <p>E a falha mais comum e mais barata de explorar em API com dado por usuario: o
 * endpoint confere que ha alguem logado, mas nao que aquele registro e de quem esta
 * logado. Quem descobre isso troca o numero na URL e le o que quiser.
 *
 * <p>A defesa aqui e estrutural, e nao uma checagem espalhada: nenhum endpoint aceita
 * identificador de usuario do cliente, e toda consulta parte do usuario resolvido da
 * sessao. Estes testes existem para que essa propriedade nao se perca sem ninguem ver.
 */
@SpringBootTest
@AutoConfigureMockMvc
class IsolamentoEntreContasIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContaDeTeste conta;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    private final ObjectMapper conversorJson = new ObjectMapper();

    @BeforeEach
    @AfterEach
    void linhaDeBase() {
        conta.garantirQueExiste();
        conta.garantirQueOOutroExiste();
        limpeza.limparHistoricoENotas();
    }

    private JsonNode chamar(RequestBuilder requisicao) throws Exception {
        return conversorJson.readTree(
                mockMvc.perform(requisicao).andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("responder o desafio de outra conta devolve 404, nao a correcao")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void naoRespondeDesafioDeOutraConta() throws Exception {
        long desafioDoPrimeiro = chamar(get("/api/desafios/proximo")).get("id").asLong();

        // Mesma requisicao, mesma sessao valida, so que da outra conta.
        var resultado = mockMvc.perform(post("/api/desafios/" + desafioDoPrimeiro + "/resposta")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors
                                .user(ContaDeTeste.EMAIL_DO_OUTRO).roles("ALUNO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resposta":"I am here"}"""))
                .andReturn();

        // 404 e nao 403 de proposito: confirmar que o recurso existe ja seria uma
        // informacao a mais do que quem nao e dono precisa ter.
        assertThat(resultado.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("o desafio em aberto de uma conta nao aparece para a outra")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void desafioEmAbertoNaoVazaParaOutraConta() throws Exception {
        long doPrimeiro = chamar(get("/api/desafios/proximo")).get("id").asLong();

        long doSegundo = chamar(get("/api/desafios/proximo")
                .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors
                        .user(ContaDeTeste.EMAIL_DO_OUTRO).roles("ALUNO")))
                .get("id").asLong();

        assertThat(doSegundo).isNotEqualTo(doPrimeiro);
    }

    @Test
    @DisplayName("a nota de uma conta nao aparece na trilha da outra")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void notaNaoVazaParaOutraConta() throws Exception {
        long desafio = chamar(get("/api/desafios/proximo")).get("id").asLong();
        chamar(post("/api/desafios/" + desafio + "/resposta")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"resposta":"I are wrong"}"""));

        JsonNode trilhaDoPrimeiro = chamar(get("/api/trilha"));
        assertThat(trilhaDoPrimeiro.get("modulosConsolidados").asInt()).isZero();

        JsonNode trilhaDoSegundo = chamar(get("/api/trilha")
                .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors
                        .user(ContaDeTeste.EMAIL_DO_OUTRO).roles("ALUNO")));

        // Todo modulo da segunda conta continua sem nota: nada praticado por ela.
        JsonNode primeiroModulo = trilhaDoSegundo.get("fases").get(0).get("modulos").get(0);
        assertThat(primeiroModulo.get("nota").isNull()).isTrue();
        assertThat(primeiroModulo.get("quantidadeDePraticas").asInt()).isZero();
    }

    @Test
    @DisplayName("preferencia alterada numa conta nao altera a outra")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void preferenciaNaoVazaParaOutraConta() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/usuario/preferencias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"objetivo":"VIAGEM","minutosPorDia":45}"""))
                .andExpect(status().isOk());

        JsonNode outro = chamar(get("/api/usuario")
                .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors
                        .user(ContaDeTeste.EMAIL_DO_OUTRO).roles("ALUNO")));

        assertThat(outro.get("objetivo").asText()).isEqualTo("CONVERSACAO_GERAL");
        assertThat(outro.get("minutosPorDia").asInt()).isEqualTo(15);
        assertThat(outro.get("email").asText()).isEqualTo(ContaDeTeste.EMAIL_DO_OUTRO);
    }

    @Test
    @DisplayName("o historico de uma conta nao inclui desafio da outra")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void historicoNaoMisturaContas() throws Exception {
        chamar(get("/api/desafios/proximo"));

        JsonNode historicoDoSegundo = chamar(get("/api/desafios/historico")
                .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors
                        .user(ContaDeTeste.EMAIL_DO_OUTRO).roles("ALUNO")));

        assertThat(historicoDoSegundo.isArray()).isTrue();
        assertThat(historicoDoSegundo).isEmpty();
    }
}
