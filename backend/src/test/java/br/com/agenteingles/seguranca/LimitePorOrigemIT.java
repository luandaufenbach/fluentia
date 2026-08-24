package br.com.agenteingles.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Limit;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prova os dois limites que o contador por conta nao alcanca.
 *
 * <p>O contador por conta trava tentativa repetida contra <i>uma</i> conta. Ele tem dois
 * pontos cegos, e os dois so aparecem quando o endereco vira publico:
 *
 * <ul>
 *   <li>quem espalha uma tentativa por conta nunca estoura o contador de nenhuma;</li>
 *   <li>quem cria contas nao erra nenhuma vez — nao ha contador de falha para estourar,
 *       e cada conta nova e um caminho novo para gastar a chave da API.</li>
 * </ul>
 *
 * <p>Os limites vem baixos aqui de proposito: com os valores de producao o teste levaria
 * dezenas de requisicoes para provar a mesma coisa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "agente-ingles.seguranca.cadastros-por-origem-por-hora=2",
        "agente-ingles.seguranca.recusas-por-origem-por-hora=3"
})
class LimitePorOrigemIT {

    private static final int LIMITE_DE_CADASTROS = 2;
    private static final int LIMITE_DE_RECUSAS = 3;

    /**
     * Enderecos criados por esta classe, listados um a um.
     *
     * <p>A limpeza apaga exatamente estes e nada mais. Ja apaguei conta de verdade por
     * filtrar com {@code LIKE} num dominio inteiro em vez de conferir cada endereco;
     * lista explicita nao tem como pegar o que nao esta nela.
     */
    private static final List<String> EMAILS_CRIADOS = List.of(
            "limite.origem.1@fluentia.local",
            "limite.origem.2@fluentia.local",
            "limite.origem.3@fluentia.local");

    private static final String SENHA = "senha-longa-de-teste-2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContaDeTeste conta;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private EventoDeAutenticacaoRepositorio eventoRepositorio;

    @BeforeEach
    @AfterEach
    void linhaDeBase() {
        conta.recriar();
        // Zera os eventos primeiro: sao eles que alimentam os dois contadores, e um
        // teste que herdasse a contagem do anterior passaria ou falharia pela ordem.
        limpeza.limparHistoricoENotas();
        EMAILS_CRIADOS.forEach(email ->
                usuarioRepositorio.buscarPorEmail(email).ifPresent(usuarioRepositorio::delete));
    }

    @Test
    @DisplayName("acima do teto, a origem para de conseguir criar conta")
    void cadastroAcimaDoTetoEhRecusado() throws Exception {
        for (int indice = 0; indice < LIMITE_DE_CADASTROS; indice++) {
            cadastrar(EMAILS_CRIADOS.get(indice)).andExpect(status().isCreated());
        }

        cadastrar(EMAILS_CRIADOS.get(LIMITE_DE_CADASTROS)).andExpect(status().isTooManyRequests());

        // E o terceiro nao entrou: o 429 barrou de verdade em vez de so mudar a resposta.
        assertThat(usuarioRepositorio.buscarPorEmail(EMAILS_CRIADOS.get(LIMITE_DE_CADASTROS)))
                .isEmpty();
    }

    @Test
    @DisplayName("a origem barrada nao consegue descobrir quais e-mails ja existem")
    void origemBarradaNaoEnumeraEmails() throws Exception {
        for (int indice = 0; indice < LIMITE_DE_CADASTROS; indice++) {
            cadastrar(EMAILS_CRIADOS.get(indice)).andExpect(status().isCreated());
        }

        // Um e-mail que existe de verdade. Se o limite fosse conferido depois da checagem
        // de duplicado, este caso responderia 409 e o de baixo 429 — e a diferenca entre
        // as duas respostas entregaria, de graca, a lista de quem tem conta.
        cadastrar(ContaDeTeste.EMAIL).andExpect(status().isTooManyRequests());
        cadastrar("nunca.existiu@fluentia.local").andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("recusas espalhadas por varias contas travam a origem, nao a conta")
    void recusasEspalhadasTravamAOrigem() throws Exception {
        // Uma tentativa por e-mail: nenhuma conta chega perto do proprio limite de cinco.
        // Sem o limite por origem, isto seguiria para sempre.
        for (int tentativa = 1; tentativa <= LIMITE_DE_RECUSAS; tentativa++) {
            entrar("desconhecido-%d@fluentia.local".formatted(tentativa))
                    .andExpect(status().isUnauthorized());
        }

        entrar("desconhecido-4@fluentia.local").andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("a recusa por limite fica na auditoria, e nao conta como recusa de login")
    void recusaPorLimiteEhAuditadaEmTipoProprio() throws Exception {
        for (int indice = 0; indice < LIMITE_DE_CADASTROS; indice++) {
            cadastrar(EMAILS_CRIADOS.get(indice)).andExpect(status().isCreated());
        }
        String barrado = EMAILS_CRIADOS.get(LIMITE_DE_CADASTROS);
        cadastrar(barrado).andExpect(status().isTooManyRequests());

        var eventos = eventoRepositorio.listarPorEmail(barrado, Limit.of(5));

        // Tipo proprio, e nao LOGIN_RECUSADO: se a recusa por limite contasse como
        // recusa, ela alimentaria o contador que a causou e o bloqueio se renovaria
        // sozinho sem fim.
        assertThat(eventos)
                .extracting(EventoDeAutenticacao::getTipo)
                .containsExactly(TipoDeEventoDeAutenticacao.LIMITE_DE_ORIGEM);
    }

    private org.springframework.test.web.servlet.ResultActions cadastrar(String email) throws Exception {
        return mockMvc.perform(post("/api/autenticacao/cadastro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Aluno do Limite","email":"%s","senha":"%s"}"""
                        .formatted(email, SENHA)));
    }

    private org.springframework.test.web.servlet.ResultActions entrar(String email) throws Exception {
        return mockMvc.perform(post("/api/autenticacao/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","senha":"chute-errado-mas-longo"}""".formatted(email)));
    }
}
