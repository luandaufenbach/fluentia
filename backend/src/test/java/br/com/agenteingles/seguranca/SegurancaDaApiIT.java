package br.com.agenteingles.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
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

/**
 * Prova que a politica de acesso vale de verdade.
 *
 * <p>Configuracao de seguranca e o tipo de codigo que parece certo lendo e esta errado
 * rodando: uma regra na ordem trocada, um filtro que nao entrou na cadeia, e a API
 * segue respondendo — so que para qualquer um. Cada teste aqui e uma afirmacao que
 * falharia silenciosamente sem ele.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaDaApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContaDeTeste conta;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @Autowired
    private EventoDeAutenticacaoRepositorio eventoRepositorio;

    @BeforeEach
    @AfterEach
    void linhaDeBase() {
        // Recria a conta do zero: um bloqueio deixado por um teste travaria o proximo.
        conta.recriar();
        limpeza.limparHistoricoENotas();
    }

    // ---------- negar por padrao ----------

    @Test
    @DisplayName("sem sessao, nenhum endpoint de dados responde")
    void semSessaoTudoERecusado() throws Exception {
        // A lista cobre um endpoint de cada dominio: se um controller novo nascer
        // desprotegido, o padrao "negar por ultimo" da configuracao o pega, mas este
        // teste documenta a expectativa.
        for (String rota : new String[] {
                "/api/trilha", "/api/modulos", "/api/desafios/proximo", "/api/desafios/historico",
                "/api/progresso", "/api/dashboard/sugestao", "/api/temas", "/api/usuario",
                "/api/modulos/verbo_to_be/conteudo", "/api/consumo", "/api/nivelamento"}) {
            mockMvc.perform(get(rota))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("a recusa vem em JSON, nao em redirecionamento para tela de login")
    void recusaVemEmJson() throws Exception {
        String corpo = mockMvc.perform(get("/api/trilha"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).contains("mensagem");
        assertThat(corpo).doesNotContain("<html");
    }

    @Test
    @DisplayName("diagnostico expoe configuracao, entao exige papel de administrador")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void diagnosticoEhRestritoAoAdministrador() throws Exception {
        mockMvc.perform(get("/api/diagnostico"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("actuator alem da sonda de vida nao responde para aluno")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void actuatorEhRestrito() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/env")).andExpect(status().isForbidden());
    }

    // ---------- CSRF ----------

    @Test
    @DisplayName("POST sem token CSRF e recusado mesmo com sessao valida")
    @WithMockUser(username = ContaDeTeste.EMAIL, roles = "ALUNO")
    void postSemCsrfERecusado() throws Exception {
        mockMvc.perform(post("/api/desafios/1/resposta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resposta\":\"I am here\"}"))
                .andExpect(status().isForbidden());
    }

    // ---------- cabecalhos de protecao ----------

    @Test
    @DisplayName("os cabecalhos de protecao acompanham toda resposta")
    void cabecalhosDeProtecaoPresentes() throws Exception {
        mockMvc.perform(get("/api/trilha"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "same-origin"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    // ---------- credencial ----------

    @Test
    @DisplayName("conta inexistente e senha errada devolvem a mesma resposta")
    void naoRevelaQuaisContasExistem() throws Exception {
        var inexistente = mockMvc.perform(post("/api/autenticacao/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ninguem@fluentia.local","senha":"senha-qualquer-longa"}"""))
                .andReturn().getResponse();

        var senhaErrada = mockMvc.perform(post("/api/autenticacao/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"senha-errada-mas-longa"}""".formatted(ContaDeTeste.EMAIL)))
                .andReturn().getResponse();

        // Status e corpo identicos: a diferenca entre os casos nao pode vazar. O
        // instante e removido dos dois lados por ser naturalmente diferente.
        assertThat(inexistente.getStatus()).isEqualTo(401);
        assertThat(senhaErrada.getStatus()).isEqualTo(401);
        assertThat(semInstante(inexistente.getContentAsString()))
                .isEqualTo(semInstante(senhaErrada.getContentAsString()));
    }

    private String semInstante(String corpo) {
        return corpo.replaceAll("\"momento\":\"[^\"]+\"", "");
    }

    /**
     * Este teste existe porque a primeira versao do bloqueio nao funcionava, e passava
     * em todos os outros: a recusa lancava excecao, a excecao desfazia a transacao, e
     * a transacao desfeita levava junto o incremento do contador de falhas. A conta
     * ficava eternamente na "primeira" tentativa. Nenhuma leitura de codigo pegou —
     * so tentar entrar seis vezes seguidas.
     */
    @Test
    @DisplayName("apos o limite de tentativas a conta bloqueia, e ate a senha certa e recusada")
    void contaBloqueiaAposTentativasSeguidas() throws Exception {
        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            mockMvc.perform(post("/api/autenticacao/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"%s","senha":"chute-errado-numero-%d"}"""
                                    .formatted(ContaDeTeste.EMAIL, tentativa)))
                    .andExpect(status().is(tentativa < 5 ? 401 : 429));
        }

        // A senha correta tambem e recusada: e isto que impede o atacante de continuar
        // tentando e o que prova que o contador sobreviveu as recusas anteriores.
        mockMvc.perform(post("/api/autenticacao/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}"""
                                .formatted(ContaDeTeste.EMAIL, ContaDeTeste.SENHA)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("a tentativa recusada fica na trilha de auditoria")
    void tentativaRecusadaEhAuditada() throws Exception {
        mockMvc.perform(post("/api/autenticacao/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"errada-mas-longa"}""".formatted(ContaDeTeste.EMAIL)))
                .andExpect(status().isUnauthorized());

        // Mesmo motivo do teste acima: a auditoria da falha era desfeita junto com a
        // transacao da falha, e a trilha so guardava os acessos bem-sucedidos.
        var eventos = eventoRepositorio.listarPorEmail(ContaDeTeste.EMAIL,
                org.springframework.data.domain.Limit.of(5));

        assertThat(eventos).isNotEmpty();
        assertThat(eventos.get(0).getTipo()).isEqualTo(TipoDeEventoDeAutenticacao.LOGIN_RECUSADO);
    }

    /**
     * Tambem nasceu de um controle que nao funcionava. {@code maximumSessions(1)}
     * estava configurado e nao tinha efeito nenhum — a sessao antiga seguia
     * respondendo depois de um novo login. Duas tentativas depois, a invalidacao
     * passou a ser direta; este teste e o que garante que continue sendo.
     */
    @Test
    @DisplayName("entrar de novo derruba a sessao anterior da mesma conta")
    void novoLoginDerrubaSessaoAnterior() throws Exception {
        var primeiraSessao = new org.springframework.mock.web.MockHttpSession();
        var segundaSessao = new org.springframework.mock.web.MockHttpSession();

        String credencial = """
                {"email":"%s","senha":"%s"}""".formatted(ContaDeTeste.EMAIL, ContaDeTeste.SENHA);

        mockMvc.perform(post("/api/autenticacao/login").session(primeiraSessao)
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(credencial))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/autenticacao/login").session(segundaSessao)
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(credencial))
                .andExpect(status().isOk());

        assertThat(primeiraSessao.isInvalid())
                .as("a sessao aberta antes precisa ter sido derrubada pelo login novo")
                .isTrue();
    }

    @Test
    @DisplayName("senha curta demais e recusada no cadastro")
    void senhaCurtaERecusada() throws Exception {
        mockMvc.perform(post("/api/autenticacao/cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Alguem","email":"curta@fluentia.local","senha":"12345"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("e-mail invalido e recusado no cadastro")
    void emailInvalidoERecusado() throws Exception {
        mockMvc.perform(post("/api/autenticacao/cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Alguem","email":"nao-e-email","senha":"senha-longa-o-bastante"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a resposta do login nunca traz hash, papel nem estado de bloqueio")
    void respostaDeLoginNaoVazaCampoInterno() throws Exception {
        String corpo = mockMvc.perform(post("/api/autenticacao/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}"""
                                .formatted(ContaDeTeste.EMAIL, ContaDeTeste.SENHA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).contains("\"email\"", "\"nome\"", "\"id\"");
        assertThat(corpo).doesNotContain("senha", "Hash", "papel", "bloqueado", "tentativas");
    }

    @Test
    @DisplayName("a conta semeada nas migrations nao autentica: nao ha credencial conhecida no repositorio")
    void contaSemeadaNaoAutentica() throws Exception {
        for (String tentativa : new String[] {"dev", "admin", "senha", "dev@agenteingles.local", ""}) {
            mockMvc.perform(post("/api/autenticacao/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"dev@agenteingles.local","senha":"%s"}""".formatted(tentativa)))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isIn(400, 401));
        }
    }
}
