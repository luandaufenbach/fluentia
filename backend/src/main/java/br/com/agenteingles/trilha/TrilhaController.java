package br.com.agenteingles.trilha;

import br.com.agenteingles.modulo.ModuloController.ModuloNaListaResposta;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A trilha inteira: fases, o que cada uma destrava e onde o aluno esta nelas. */
@RestController
@RequestMapping("/api/trilha")
public class TrilhaController {

    private final ServicoDeTrilha servicoDeTrilha;
    private final ServicoDeUsuario servicoDeUsuario;

    public TrilhaController(ServicoDeTrilha servicoDeTrilha, ServicoDeUsuario servicoDeUsuario) {
        this.servicoDeTrilha = servicoDeTrilha;
        this.servicoDeUsuario = servicoDeUsuario;
    }

    /**
     * @param situacaoDoMarco alcancado exige pratica de verdade em todos os conceitos da
     *                        fase; presumido e a fase que so esta fora do vermelho por
     *                        estimativa do nivelamento
     * @param emAndamento a fase contem o conceito que o orquestrador escolheria agora —
     *                    e onde a interface posiciona "voce esta aqui". Sai da mesma fonte
     *                    do cartao de proximo passo, para os dois nunca discordarem
     * @param modulosConsolidados fora do vermelho e provados numa resposta
     * @param modulosPresumidos fora do vermelho apenas por estimativa, sem pratica
     */
    public record FaseNaTrilhaResposta(
            String codigo,
            String nome,
            String promessa,
            String marco,
            SituacaoDoMarco situacaoDoMarco,
            boolean emAndamento,
            int modulosConsolidados,
            int modulosPresumidos,
            int totalDeModulos,
            List<ModuloNaListaResposta> modulos) {
    }

    public record TrilhaResposta(
            List<FaseNaTrilhaResposta> fases,
            int modulosConsolidados,
            int modulosPresumidos,
            int totalDeModulos) {
    }

    @GetMapping
    public TrilhaResposta ver() {
        return servicoDeTrilha.montar(servicoDeUsuario.usuarioAtual());
    }
}
