package br.com.agenteingles.agente.claude;

import br.com.agenteingles.agente.AgenteDeNivelamento;
import br.com.agenteingles.agente.PedidoDeNivelamento;
import br.com.agenteingles.agente.PropriedadesDoAgente;
import br.com.agenteingles.agente.ResultadoDoNivelamento;
import br.com.agenteingles.custo.TipoDeChamada;
import com.anthropic.models.messages.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Estima o nivel lendo a conversa inteira numa unica chamada.
 *
 * <p>Usa o modelo de raciocinio: o nivel estimado decide por onde a pessoa comeca a
 * trilha, e errar para cima frustra tanto quanto errar para baixo.
 */
@Component
@ConditionalOnProperty(name = "agente-ingles.usar-claude", havingValue = "true")
public class NivelamentoComClaude implements AgenteDeNivelamento {

    private static final Logger log = LoggerFactory.getLogger(NivelamentoComClaude.class);

    private static final int MAXIMO_DE_TOKENS = 3000;

    private static final String INSTRUCAO_DO_SISTEMA = """
            Voce e o agente de nivelamento de um curriculo adaptativo de ingles.

            Le a conversa de entrada e estima o nivel CEFR de quem respondeu.

            Regras:
            - Julgue o que a pessoa PRODUZIU, nao o que ela disse sobre si mesma.
            - Pergunta pulada e sinal de teto: o nivel fica abaixo do alvo daquela pergunta.
            - Erro de digitacao ou de pontuacao nao derruba nivel. Estrutura, tempo verbal
              e capacidade de sustentar um argumento derrubam.
            - Na duvida entre dois niveis, escolha o MENOR. Comecar abaixo do proprio nivel
              custa alguns minutos; comecar acima faz a pessoa desistir.
            - Resposta em uma unica palavra para o nivel: A1, A2, B1, B2, C1 ou C2.
            - O resumo fala com o aluno, em portugues, em ate tres frases, sem jargao de
              linguistica e sem elogio vazio.
            - O ponto a fortalecer deve ser um conceito de gramatica concreto, do tipo
              "passado simples" ou "artigos", nao "praticar mais".
            """;

    private final ChatClient clienteDeChat;
    private final PropriedadesDoAgente propriedades;
    private final MedidorDeChamada medidor;
    private final LeitorDeRespostaEstruturada<ResultadoDoNivelamento> leitor =
            new LeitorDeRespostaEstruturada<>(ResultadoDoNivelamento.class);

    public NivelamentoComClaude(AnthropicChatModel modeloDeChat,
                                PropriedadesDoAgente propriedades,
                                MedidorDeChamada medidor) {
        this.clienteDeChat = ChatClient.create(modeloDeChat);
        this.propriedades = propriedades;
        this.medidor = medidor;
    }

    @Override
    public ResultadoDoNivelamento estimar(PedidoDeNivelamento pedido) {
        log.debug("Estimando nivel a partir de {} turno(s)", pedido.turnos().size());

        var resposta = clienteDeChat.prompt()
                .system(INSTRUCAO_DO_SISTEMA)
                .user(montarPedido(pedido) + "\n" + leitor.instrucaoDeFormato())
                .options(AnthropicChatOptions.builder()
                        .model(Model.of(propriedades.modeloDeRaciocinio()))
                        .maxTokens(MAXIMO_DE_TOKENS)
                        .thinkingAdaptive())
                .call()
                .chatResponse();

        medidor.medir(resposta, pedido.usuarioId(), TipoDeChamada.NIVELAMENTO,
                propriedades.modeloDeRaciocinio(), 1);

        return leitor.converter(resposta);
    }

    private String montarPedido(PedidoDeNivelamento pedido) {
        StringBuilder conversa = new StringBuilder("Conversa de entrada:\n\n");
        for (PedidoDeNivelamento.TurnoDoNivelamento turno : pedido.turnos()) {
            conversa.append("Pergunta (alvo ").append(turno.nivelAlvo()).append("): ")
                    .append(turno.pergunta()).append('\n')
                    .append("Resposta: ")
                    .append(turno.resposta() == null ? "(o aluno pulou esta pergunta)" : turno.resposta())
                    .append("\n\n");
        }
        return conversa.toString();
    }
}
