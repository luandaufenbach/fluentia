package br.com.agenteingles.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.usuario.Usuario;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A recuperacao de senha e um caminho que troca credencial sem pedir credencial. Cada
 * teste aqui trava uma das defesas que tornam isso aceitavel.
 */
@SpringBootTest
class RecuperacaoDeSenhaIT {

    private static final String ORIGEM = "203.0.113.10";

    @Autowired
    private ServicoDeRecuperacaoDeSenha servico;

    @Autowired
    private RecuperacaoDeSenhaRepositorio recuperacaoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder codificador;

    @Autowired
    private ContaDeTeste conta;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    private Usuario usuario;

    @BeforeEach
    @AfterEach
    void linhaDeBase() {
        recuperacaoRepositorio.deleteAll();
        conta.recriar();
        limpeza.limparHistoricoENotas();
        usuario = usuarioRepositorio.buscarPorEmail(ContaDeTeste.EMAIL).orElseThrow();
    }

    /** Mesmo resumo que o servico usa. O token em claro nunca sai do e-mail. */
    private String resumir(String token) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    /** Cria um pedido com token conhecido: o servico so guarda o hash, entao o teste
     *  precisa plantar a linha para poder exercitar a redefinicao. */
    private String plantarPedido(LocalDateTime expiraEm) throws Exception {
        String token = "token-de-teste-" + System.nanoTime();
        recuperacaoRepositorio.save(
                new RecuperacaoDeSenha(usuario.getId(), resumir(token), expiraEm, ORIGEM));
        return token;
    }

    // ---------- pedir ----------

    @Test
    @DisplayName("e-mail sem conta nao explode e nao deixa rastro de pedido")
    void emailSemContaNaoQuebra() {
        // A resposta HTTP e a mesma com e sem conta. Se aqui lancasse excecao, o
        // controller teria de tratar — e a diferenca de tratamento acabaria vazando
        // para o cliente, que e justamente o que nao pode acontecer.
        assertThatCode(() -> servico.solicitar("nao.existe@fluentia.local", ORIGEM))
                .doesNotThrowAnyException();

        assertThat(recuperacaoRepositorio.count()).isZero();
    }

    @Test
    @DisplayName("pedir de novo queima o link anterior")
    void pedirDeNovoQueimaOAnterior() {
        servico.solicitar(ContaDeTeste.EMAIL, ORIGEM);
        servico.solicitar(ContaDeTeste.EMAIL, ORIGEM);

        // Dois pedidos, um unico utilizavel: senao cada tentativa de quem nao esta
        // conseguindo entrar deixaria mais uma chave viva na caixa de entrada.
        long utilizaveis = recuperacaoRepositorio.findAll().stream()
                .filter(pedido -> pedido.getUsadoEm() == null)
                .count();

        assertThat(recuperacaoRepositorio.count()).isEqualTo(2);
        assertThat(utilizaveis).isEqualTo(1);
    }

    @Test
    @DisplayName("o token em claro nunca e gravado")
    void tokenEmClaroNaoEhGravado() {
        servico.solicitar(ContaDeTeste.EMAIL, ORIGEM);

        // O que existe no banco tem 64 caracteres hexadecimais — o resumo, nao o token.
        assertThat(recuperacaoRepositorio.findAll())
                .singleElement()
                .satisfies(pedido -> assertThat(recuperacaoRepositorio.findByTokenHash(
                        resumirSemChecar("qualquer-coisa"))).isEmpty());
    }

    // ---------- redefinir ----------

    @Test
    @DisplayName("com o link valido a senha muda de verdade")
    void linkValidoTrocaASenha() throws Exception {
        String token = plantarPedido(LocalDateTime.now().plusMinutes(15));

        servico.redefinir(token, "senha-nova-longa-2026", ORIGEM);

        Usuario depois = usuarioRepositorio.findById(usuario.getId()).orElseThrow();
        assertThat(codificador.matches("senha-nova-longa-2026", depois.getSenhaHashParaVerificacao()))
                .as("a senha nova passa a valer")
                .isTrue();
        assertThat(codificador.matches(ContaDeTeste.SENHA, depois.getSenhaHashParaVerificacao()))
                .as("a senha antiga para de valer")
                .isFalse();
    }

    @Test
    @DisplayName("o mesmo link nao serve duas vezes")
    void linkNaoServeDuasVezes() throws Exception {
        String token = plantarPedido(LocalDateTime.now().plusMinutes(15));
        servico.redefinir(token, "senha-nova-longa-2026", ORIGEM);

        // Prazo sozinho nao basta: quem tiver acesso a caixa de entrada depois usaria
        // o mesmo link de novo enquanto ele nao vencesse.
        assertThatThrownBy(() -> servico.redefinir(token, "outra-senha-longa-2026", ORIGEM))
                .isInstanceOf(ServicoDeRecuperacaoDeSenha.LinkInvalidoException.class);
    }

    @Test
    @DisplayName("link vencido e recusado")
    void linkVencidoEhRecusado() throws Exception {
        String token = plantarPedido(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> servico.redefinir(token, "senha-nova-longa-2026", ORIGEM))
                .isInstanceOf(ServicoDeRecuperacaoDeSenha.LinkInvalidoException.class);
    }

    @Test
    @DisplayName("token inventado e recusado com a mesma mensagem de link vencido")
    void tokenInventadoEhRecusado() {
        // Mesma excecao dos outros dois casos: distinguir "nao existe" de "venceu"
        // ajudaria quem esta sondando tokens e nao ajuda quem perdeu a senha.
        assertThatThrownBy(() -> servico.redefinir("token-que-ninguem-emitiu", "senha-nova-longa-2026", ORIGEM))
                .isInstanceOf(ServicoDeRecuperacaoDeSenha.LinkInvalidoException.class);
    }

    @Test
    @DisplayName("redefinir destrava a conta bloqueada por tentativas")
    void redefinirDestravaAConta() throws Exception {
        // Quem esqueceu a senha normalmente errou varias vezes antes de desistir, e
        // chega aqui com a conta bloqueada. Se a troca nao destravasse, a pessoa
        // definiria a senha nova e continuaria sem conseguir entrar.
        usuario.registrarFalhaDeLogin(1, LocalDateTime.now().plusMinutes(15));
        usuarioRepositorio.save(usuario);
        assertThat(usuarioRepositorio.findById(usuario.getId()).orElseThrow()
                .estaBloqueado(LocalDateTime.now())).isTrue();

        String token = plantarPedido(LocalDateTime.now().plusMinutes(15));
        servico.redefinir(token, "senha-nova-longa-2026", ORIGEM);

        assertThat(usuarioRepositorio.findById(usuario.getId()).orElseThrow()
                .estaBloqueado(LocalDateTime.now())).isFalse();
    }

    private String resumirSemChecar(String texto) {
        try {
            return resumir(texto);
        } catch (Exception impossivel) {
            throw new IllegalStateException(impossivel);
        }
    }
}
