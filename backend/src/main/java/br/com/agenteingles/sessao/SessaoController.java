package br.com.agenteingles.sessao;

import br.com.agenteingles.usuario.ServicoDeUsuario;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * O dia do aluno.
 *
 * <p>Sempre da conta autenticada: o usuario vem do contexto de seguranca, nunca de
 * parametro, entao nao ha sequencia nem historico de outra pessoa alcancavel por aqui.
 */
@RestController
@RequestMapping("/api/hoje")
public class SessaoController {

    private final ServicoDaSessao servicoDaSessao;
    private final ServicoDeUsuario servicoDeUsuario;

    public SessaoController(ServicoDaSessao servicoDaSessao, ServicoDeUsuario servicoDeUsuario) {
        this.servicoDaSessao = servicoDaSessao;
        this.servicoDeUsuario = servicoDeUsuario;
    }

    /** @param restantes vem calculado para a tela nao repetir a regra de "nunca negativo" */
    public record ResumoDoDiaResposta(int meta,
                                      long concluidos,
                                      long restantes,
                                      boolean metaAlcancada,
                                      SequenciaDeDias sequencia,
                                      List<RevisaoPendente> revisoes) {
    }

    @GetMapping
    public ResumoDoDiaResposta hoje() {
        ResumoDoDia resumo = servicoDaSessao.doUsuario(servicoDeUsuario.usuarioAtual());
        return new ResumoDoDiaResposta(
                resumo.meta(),
                resumo.concluidos(),
                resumo.restantes(),
                resumo.metaAlcancada(),
                resumo.sequencia(),
                resumo.revisoes());
    }
}
