package br.com.agenteingles.agente.claude;

import br.com.agenteingles.agente.AgenteGeradorDeDesafio;
import br.com.agenteingles.agente.DesafioGerado;
import br.com.agenteingles.agente.PedidoDeGeracao;
import br.com.agenteingles.agente.PropriedadesDoAgente;
import com.anthropic.models.messages.Model;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Gerador de desafio apoiado na Claude. Usa o modelo de raciocinio porque a qualidade do
 * desafio determina o que sera medido — um enunciado ambiguo contamina a nota do modulo.
 */
@Component
@ConditionalOnProperty(name = "agente-ingles.usar-claude", havingValue = "true")
public class GeradorDeDesafioComClaude implements AgenteGeradorDeDesafio {

    private static final Logger log = LoggerFactory.getLogger(GeradorDeDesafioComClaude.class);

    private static final int MAXIMO_DE_TOKENS = 2000;

    private static final String INSTRUCAO_DO_SISTEMA = """
            Voce e o agente gerador de desafios de um curriculo adaptativo de ingles.

            Sua tarefa e criar UM desafio novo que meça exatamente o conceito informado,
            ambientado na cena do tema informado.

            Regras:
            - O desafio deve medir o conceito indicado, e nada alem dele. O tema so da a roupagem.
            - O enunciado precisa ser inedito: nunca repita nenhum dos enunciados ja usados.
            - Calibre a dificuldade pelo nivel CEFR e pela nota atual do usuario no modulo.
            - Se houver erros recentes, mire justamente neles.
            - O enunciado e a explicacao vao em portugues; o conteudo a ser produzido pelo aluno, em ingles.
            - A resposta de referencia deve ser uma resposta correta e natural.
            - O criterio de avaliacao descreve objetivamente o que verificar na resposta.
            """;

    private final ChatClient clienteDeChat;
    private final PropriedadesDoAgente propriedades;
    private final LeitorDeRespostaEstruturada<DesafioGerado> leitor =
            new LeitorDeRespostaEstruturada<>(DesafioGerado.class);

    public GeradorDeDesafioComClaude(AnthropicChatModel modeloDeChat, PropriedadesDoAgente propriedades) {
        this.clienteDeChat = ChatClient.create(modeloDeChat);
        this.propriedades = propriedades;
    }

    @Override
    public DesafioGerado gerar(PedidoDeGeracao pedido) {
        log.debug("Gerando desafio do modulo {} no tema {}", pedido.codigoDoModulo(), pedido.nomeDoTema());

        var resposta = clienteDeChat.prompt()
                .system(INSTRUCAO_DO_SISTEMA)
                .user(montarPedido(pedido) + "\n" + leitor.instrucaoDeFormato())
                .options(AnthropicChatOptions.builder()
                        .model(Model.of(propriedades.modeloDeRaciocinio()))
                        .maxTokens(MAXIMO_DE_TOKENS))
                .call()
                .chatResponse();

        return leitor.converter(resposta);
    }

    private String montarPedido(PedidoDeGeracao pedido) {
        return """
                Conceito a medir: %s (codigo %s)
                Descricao do conceito: %s
                Nivel CEFR: %s
                Tema da cena: %s — %s
                Formato do desafio: %s
                Nota atual do usuario neste modulo: %s
                Erros recentes neste conceito: %s
                Enunciados ja usados (nao repita nenhum): %s
                """.formatted(
                pedido.nomeDoModulo(),
                pedido.codigoDoModulo(),
                pedido.descricaoDoModulo(),
                pedido.nivel(),
                pedido.nomeDoTema(),
                pedido.descricaoDoTema(),
                pedido.formato(),
                pedido.notaAtual() == null ? "ainda sem nota (modulo novo)" : pedido.notaAtual().toPlainString(),
                formatarLista(pedido.errosRecentes(), "nenhum erro registrado ainda"),
                formatarLista(pedido.enunciadosRecentes(), "nenhum desafio anterior"));
    }

    private String formatarLista(List<String> itens, String textoQuandoVazio) {
        if (itens == null || itens.isEmpty()) {
            return textoQuandoVazio;
        }
        return String.join(" | ", itens);
    }
}
