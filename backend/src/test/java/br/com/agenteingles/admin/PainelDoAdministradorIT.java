package br.com.agenteingles.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.custo.RegistroDeConsumo;
import br.com.agenteingles.custo.TipoDeChamada;
import br.com.agenteingles.usuario.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * O painel expoe dados de TODAS as contas: e-mail, ultimo acesso e quanto cada uma
 * gastou. Quem nao e administrador nao pode chegar perto — e e isso que a maior parte
 * destes testes trava.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PainelDoAdministradorIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicoDoPainel servicoDoPainel;

    @Autowired
    private RegistroDeConsumo registroDeConsumo;

    @Autowired
    private ContaDeTeste conta;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @BeforeEach
    @AfterEach
    void linhaDeBase() {
        conta.recriar();
        limpeza.limparHistoricoENotas();
        conta.limparContexto();
    }

    // ---------- quem pode entrar ----------

    @Test
    @DisplayName("sem sessao o painel nao responde")
    void semSessaoNaoResponde() throws Exception {
        mockMvc.perform(get("/api/admin/painel")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("aluno logado recebe 403, nao os dados das outras contas")
    void alunoNaoVeOPainel() throws Exception {
        // O caso que importa: nao e um estranho, e alguem com sessao valida. Se a regra
        // de papel falhar, e por aqui que os dados de todo mundo vazam.
        mockMvc.perform(get("/api/admin/painel").with(usuarioComum()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("administrador enxerga o painel")
    void administradorEnxerga() throws Exception {
        mockMvc.perform(get("/api/admin/painel").with(administrador()))
                .andExpect(status().isOk());
    }

    // ---------- o que o painel mostra ----------

    @Test
    @DisplayName("conta sem consumo aparece com custo zero, e nao some da lista")
    void contaSemConsumoApareceComZero() {
        Usuario usuario = conta.garantirQueExiste();

        PainelDoAdministrador painel = servicoDoPainel.montar();

        // Some da lista seria pior que aparecer zerada: quem nunca gastou ainda e uma
        // conta que existe, e o painel tambem serve para monitorar quem se cadastrou.
        assertThat(painel.contas())
                .extracting(LinhaDoPainel::usuarioId)
                .contains(usuario.getId());
        assertThat(painel.contas()).filteredOn(linha -> linha.usuarioId().equals(usuario.getId()))
                .singleElement()
                .satisfies(linha -> {
                    assertThat(linha.custoUsd()).isEqualByComparingTo("0");
                    assertThat(linha.chamadas()).isZero();
                });
    }

    @Test
    @DisplayName("o gasto da conta aparece somado na linha dela")
    void gastoApareceNaLinha() {
        Usuario usuario = conta.garantirQueExiste();
        registroDeConsumo.registrar(usuario.getId(), TipoDeChamada.GERACAO_DE_DESAFIO,
                "claude-sonnet-5", 1_000, 500, 5);
        registroDeConsumo.registrar(usuario.getId(), TipoDeChamada.AVALIACAO_DE_RESPOSTA,
                "claude-sonnet-5", 1_000, 500, 1);

        PainelDoAdministrador painel = servicoDoPainel.montar();

        assertThat(painel.contas()).filteredOn(linha -> linha.usuarioId().equals(usuario.getId()))
                .singleElement()
                .satisfies(linha -> {
                    assertThat(linha.chamadas()).isEqualTo(2);
                    assertThat(linha.tokensDeEntrada()).isEqualTo(2_000);
                    // 2 x (1000 entrada + 500 saida) a 3.00 / 15.00 por milhao
                    assertThat(linha.custoUsd()).isEqualByComparingTo("0.021000");
                });
        assertThat(painel.total().chamadas()).isEqualTo(2);
    }

    @Test
    @DisplayName("modelo sem preco deixa o custo em aberto, e nao zerado")
    void modeloSemPrecoNaoViraZero() {
        Usuario usuario = conta.garantirQueExiste();
        registroDeConsumo.registrar(usuario.getId(), TipoDeChamada.AVALIACAO_DE_RESPOSTA,
                "modelo-que-ninguem-configurou", 800, 400, 1);

        PainelDoAdministrador painel = servicoDoPainel.montar();

        // Nulo, e nao zero: total que soma desconhecido como zero mente para baixo, e
        // essa e a direcao perigosa numa conta que alguem paga.
        assertThat(painel.contas()).filteredOn(linha -> linha.usuarioId().equals(usuario.getId()))
                .singleElement()
                .satisfies(linha -> assertThat(linha.custoUsd()).isNull());

        // E a tela precisa poder avisar que o total esta incompleto.
        assertThat(painel.modelosSemPreco()).contains("modelo-que-ninguem-configurou");
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor usuarioComum() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .user(ContaDeTeste.EMAIL).roles("ALUNO");
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor administrador() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .user(ContaDeTeste.EMAIL).roles("ADMINISTRADOR");
    }
}
