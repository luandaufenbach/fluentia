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
     * @param marcoAlcancado a fase inteira saiu do vermelho, entao a habilidade do marco
     *                       conta como destravada
     * @param emAndamento ha pelo menos um modulo praticado e a fase ainda nao fechou —
     *                    e onde a interface posiciona "voce esta aqui"
     */
    public record FaseNaTrilhaResposta(
            String codigo,
            String nome,
            String promessa,
            String marco,
            boolean marcoAlcancado,
            boolean emAndamento,
            int modulosConsolidados,
            int totalDeModulos,
            List<ModuloNaListaResposta> modulos) {
    }

    public record TrilhaResposta(
            List<FaseNaTrilhaResposta> fases,
            int modulosConsolidados,
            int totalDeModulos) {
    }

    @GetMapping
    public TrilhaResposta ver() {
        return servicoDeTrilha.montar(servicoDeUsuario.usuarioAtual());
    }
}
