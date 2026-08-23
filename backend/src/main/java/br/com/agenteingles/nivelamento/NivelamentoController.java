package br.com.agenteingles.nivelamento;

import br.com.agenteingles.usuario.ServicoDeUsuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * O nivelamento de entrada.
 *
 * <p>Nao recebe id de usuario em lugar nenhum: a conta vem do contexto de seguranca, e o
 * nivelamento e sempre buscado com o dono junto — trocar o id na URL alcanca 404, nao a
 * conversa de outra pessoa.
 */
@RestController
@RequestMapping("/api/nivelamento")
public class NivelamentoController {

    /**
     * Teto do texto aceito por resposta. Existe para nao mandar um texto gigante ao
     * modelo: o token de entrada e cobrado, e ninguem responde uma pergunta de conversa
     * com dois mil caracteres.
     */
    private static final int TAMANHO_MAXIMO_DA_RESPOSTA = 2_000;

    private final ServicoDeNivelamento servicoDeNivelamento;
    private final ServicoDeUsuario servicoDeUsuario;

    public NivelamentoController(ServicoDeNivelamento servicoDeNivelamento,
                                 ServicoDeUsuario servicoDeUsuario) {
        this.servicoDeNivelamento = servicoDeNivelamento;
        this.servicoDeUsuario = servicoDeUsuario;
    }

    /** @param resposta em branco significa pular, que e um sinal legitimo de teto */
    public record RespostaRequisicao(
            @Min(value = 1, message = "Ordem inválida.")
            int ordem,

            @Size(max = TAMANHO_MAXIMO_DA_RESPOSTA, message = "Resposta muito longa.")
            String resposta) {
    }

    public record SituacaoDoNivelamento(boolean jaFez) {
    }

    /** Se a tela de entrada deve oferecer o nivelamento. */
    @GetMapping
    public SituacaoDoNivelamento situacao() {
        return new SituacaoDoNivelamento(
                servicoDeNivelamento.jaFezNivelamento(servicoDeUsuario.usuarioAtual()));
    }

    @PostMapping
    public EtapaDoNivelamento iniciar() {
        return servicoDeNivelamento.iniciar(servicoDeUsuario.usuarioAtual());
    }

    @PostMapping("/{nivelamentoId}/resposta")
    public EtapaDoNivelamento responder(@PathVariable Long nivelamentoId,
                                        @Valid @RequestBody RespostaRequisicao requisicao) {
        return servicoDeNivelamento.responder(
                servicoDeUsuario.usuarioAtual(),
                nivelamentoId,
                requisicao.ordem(),
                requisicao.resposta());
    }

    @DeleteMapping("/{nivelamentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abandonar(@PathVariable Long nivelamentoId) {
        servicoDeNivelamento.abandonar(servicoDeUsuario.usuarioAtual(), nivelamentoId);
    }
}
