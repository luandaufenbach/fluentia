package br.com.agenteingles.comum;

import br.com.agenteingles.agente.AgenteAvaliador;
import br.com.agenteingles.agente.AgenteGeradorDeDesafio;
import br.com.agenteingles.agente.PropriedadesDoAgente;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Diz qual implementacao de agente esta ativa agora.
 *
 * <p>Existe para responder de forma inequivoca "a chave pegou?": as duas implementacoes
 * ficam atras da mesma interface, entao sem isto a unica pista seria o texto do desafio.
 * Nenhum valor de chave e exposto — so o nome da classe que respondeu.
 */
@RestController
@RequestMapping("/api/diagnostico")
public class DiagnosticoController {

    private final AgenteGeradorDeDesafio agenteGerador;
    private final AgenteAvaliador agenteAvaliador;
    private final PropriedadesDoAgente propriedades;

    public DiagnosticoController(AgenteGeradorDeDesafio agenteGerador,
                                 AgenteAvaliador agenteAvaliador,
                                 PropriedadesDoAgente propriedades) {
        this.agenteGerador = agenteGerador;
        this.agenteAvaliador = agenteAvaliador;
        this.propriedades = propriedades;
    }

    /**
     * @param usandoClaude {@code true} quando os agentes reais estao no ar
     * @param chaveConfigurada apenas se a variavel de ambiente foi lida, nunca o valor dela
     */
    public record DiagnosticoResposta(
            boolean usandoClaude,
            boolean chaveConfigurada,
            String implementacaoDoGerador,
            String implementacaoDoAvaliador,
            String modeloDeRaciocinio,
            String modeloSimples) {
    }

    @GetMapping
    public DiagnosticoResposta diagnosticar() {
        return new DiagnosticoResposta(
                propriedades.usarClaude(),
                System.getenv("ANTHROPIC_API_KEY") != null
                        && !System.getenv("ANTHROPIC_API_KEY").isBlank(),
                agenteGerador.getClass().getSimpleName(),
                agenteAvaliador.getClass().getSimpleName(),
                propriedades.modeloDeRaciocinio(),
                propriedades.modeloSimples());
    }
}
