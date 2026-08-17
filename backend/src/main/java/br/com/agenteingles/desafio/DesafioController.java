package br.com.agenteingles.desafio;

import br.com.agenteingles.nota.FaixaDeNota;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints do loop: pegar o proximo desafio e responder o desafio atual. */
@RestController
@RequestMapping("/api/desafios")
public class DesafioController {

    private final ServicoDeDesafio servicoDeDesafio;
    private final ServicoDeUsuario servicoDeUsuario;

    public DesafioController(ServicoDeDesafio servicoDeDesafio, ServicoDeUsuario servicoDeUsuario) {
        this.servicoDeDesafio = servicoDeDesafio;
        this.servicoDeUsuario = servicoDeUsuario;
    }

    public record DesafioResposta(
            Long id,
            String enunciado,
            String contextoDaCena,
            FormatoDoDesafio formato,
            StatusDoDesafio status,
            String moduloCodigo,
            String moduloNome,
            String temaNome,
            String motivoDaEscolha,
            LocalDateTime criadoEm) {
    }

    public record RespostaDoUsuarioRequisicao(
            @NotBlank(message = "a resposta nao pode estar em branco")
            @Size(max = 4000, message = "a resposta e longa demais")
            String resposta) {
    }

    public record ErroResposta(String tipo, String trechoErrado, String correcao, String explicacao) {
    }

    /**
     * A correcao so chega aqui, ao final da sessao — nunca durante a escrita da resposta.
     *
     * @param notaDoModulo nota do modulo ja recalculada com esta resposta
     */
    public record CorrecaoResposta(
            Long desafioId,
            BigDecimal notaDaResposta,
            String feedback,
            List<ErroResposta> erros,
            BigDecimal notaDoModulo,
            FaixaDeNota faixaDoModulo,
            String moduloNome) {
    }

    /** Desafio da vez: devolve o que esta em aberto ou gera um novo. */
    @GetMapping("/proximo")
    public DesafioResposta proximo() {
        Usuario usuario = servicoDeUsuario.usuarioAtual();
        return converter(servicoDeDesafio.proximoDesafio(usuario));
    }

    @PostMapping("/{desafioId}/resposta")
    public CorrecaoResposta responder(@PathVariable Long desafioId,
                                      @Valid @RequestBody RespostaDoUsuarioRequisicao requisicao) {
        Usuario usuario = servicoDeUsuario.usuarioAtual();
        ResultadoDaResposta resultado = servicoDeDesafio.responder(usuario, desafioId, requisicao.resposta());

        List<ErroResposta> erros = resultado.erros().stream()
                .map(erro -> new ErroResposta(
                        erro.tipo(), erro.trechoErrado(), erro.correcao(), erro.explicacao()))
                .toList();

        return new CorrecaoResposta(
                resultado.desafioId(),
                resultado.notaDaResposta(),
                resultado.feedback(),
                erros,
                resultado.notaDoModulo(),
                resultado.faixaDoModulo(),
                resultado.moduloNome());
    }

    @GetMapping("/historico")
    public List<DesafioResposta> historico(@RequestParam(defaultValue = "20") int quantidade) {
        Usuario usuario = servicoDeUsuario.usuarioAtual();
        return servicoDeDesafio.historico(usuario, quantidade).stream().map(this::converter).toList();
    }

    /** A resposta de referencia fica de fora de proposito: seria entregar o gabarito. */
    private DesafioResposta converter(ResumoDoDesafio desafio) {
        return new DesafioResposta(
                desafio.id(),
                desafio.enunciado(),
                desafio.contextoDaCena(),
                desafio.formato(),
                desafio.status(),
                desafio.moduloCodigo(),
                desafio.moduloNome(),
                desafio.temaNome(),
                desafio.motivoDaEscolha(),
                desafio.criadoEm());
    }
}
