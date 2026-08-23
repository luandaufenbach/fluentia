package br.com.agenteingles.agente.claude;

import br.com.agenteingles.custo.RegistroDeConsumo;
import br.com.agenteingles.custo.TipoDeChamada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

/**
 * Le o consumo da resposta e manda gravar.
 *
 * <p>Duas responsabilidades que so fazem sentido juntas: tirar os tokens de dentro da
 * resposta do Spring AI e impedir que uma falha na contabilidade derrube o que o aluno
 * esta esperando. O desafio ja foi gerado e a correcao ja foi feita — perder o registro
 * do gasto e menos grave do que perder o trabalho que ja foi pago.
 *
 * <p>Fica fora do {@link RegistroDeConsumo} porque a gravacao roda em transacao propria,
 * e transacao propria nao vale quando o metodo e chamado de dentro da mesma classe.
 */
@Component
public class MedidorDeChamada {

    private static final Logger log = LoggerFactory.getLogger(MedidorDeChamada.class);

    private final RegistroDeConsumo registro;

    public MedidorDeChamada(RegistroDeConsumo registro) {
        this.registro = registro;
    }

    /**
     * @param modeloPedido modelo que foi solicitado, usado se a resposta nao disser qual respondeu
     * @param itensProduzidos quantos desafios sairam da chamada, para chegar ao custo unitario
     */
    public void medir(ChatResponse resposta,
                      Long usuarioId,
                      TipoDeChamada tipo,
                      String modeloPedido,
                      int itensProduzidos) {
        try {
            if (resposta == null || resposta.getMetadata() == null) {
                return;
            }

            Usage consumo = resposta.getMetadata().getUsage();
            if (consumo == null) {
                log.warn("A resposta de {} veio sem contagem de tokens", tipo.descricao());
                return;
            }

            String modelo = resposta.getMetadata().getModel();
            registro.registrar(
                    usuarioId,
                    tipo,
                    modelo == null || modelo.isBlank() ? modeloPedido : modelo,
                    valorOuZero(consumo.getPromptTokens()),
                    valorOuZero(consumo.getCompletionTokens()),
                    itensProduzidos);
        } catch (RuntimeException falha) {
            log.warn("Nao foi possivel registrar o consumo de {}: {}", tipo.descricao(), falha.getMessage());
        }
    }

    private int valorOuZero(Integer tokens) {
        return tokens == null ? 0 : tokens;
    }
}
