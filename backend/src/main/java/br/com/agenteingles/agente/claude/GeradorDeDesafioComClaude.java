package br.com.agenteingles.agente.claude;

import br.com.agenteingles.agente.AgenteGeradorDeDesafio;
import br.com.agenteingles.agente.DesafioGerado;
import br.com.agenteingles.agente.LoteDeDesafios;
import br.com.agenteingles.agente.PedidoDeGeracao;
import br.com.agenteingles.agente.PropriedadesDoAgente;
import br.com.agenteingles.custo.TipoDeChamada;
import com.anthropic.models.messages.Model;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Gerador de desafio apoiado na Claude. Usa o modelo de raciocinio porque a qualidade do
 * desafio determina o que sera medido — um enunciado ambiguo contamina a nota do modulo.
 *
 * <p>Gera em lote. Medindo o pedido real, 666 dos tokens de entrada sao fixos e se
 * repetiriam a cada desafio; pedindo varios de uma vez esse custo e dividido pelo tamanho
 * do lote.
 */
@Component
@ConditionalOnProperty(name = "agente-ingles.usar-claude", havingValue = "true")
public class GeradorDeDesafioComClaude implements AgenteGeradorDeDesafio {

    private static final Logger log = LoggerFactory.getLogger(GeradorDeDesafioComClaude.class);

    /** Cabe o lote inteiro com folga: cada desafio ocupa por volta de 250 tokens. */
    private static final int TOKENS_POR_DESAFIO = 400;

    private static final int TOKENS_MINIMOS = 1500;

    private static final String INSTRUCAO_DO_SISTEMA = """
            Voce e o agente gerador de desafios de um curriculo adaptativo de ingles.

            Sua tarefa e criar desafios novos que meçam exatamente o conceito informado,
            ambientados na cena do tema informado.

            Regras:
            - Cada desafio deve medir o conceito indicado, e nada alem dele. O tema so da a roupagem.
            - Os enunciados precisam ser ineditos: nunca repita nenhum dos ja usados, e nao
              repita nenhum enunciado dentro do proprio lote.
            - Varie a construcao entre os desafios do lote: traduzir uma frase, completar uma
              lacuna, corrigir uma frase errada, responder uma pergunta, descrever uma cena.
            - Calibre a dificuldade pelo nivel CEFR e pela nota atual do usuario no modulo.
            - Se houver erros recentes, mire justamente neles.
            - O enunciado e a explicacao vao em portugues; o conteudo a ser produzido pelo aluno,
              em ingles.
            - A resposta de referencia deve ser uma resposta correta e natural.
            - O criterio de avaliacao descreve objetivamente o que verificar na resposta.
            - Escreva o portugues com a ortografia correta, COM acentos e cedilha. Estas
              instrucoes vem sem acento por convencao do codigo-fonte: nao imite esse
              estilo, ele nao vale para o texto que o aluno le.
            """;

    /** A forma do JSON de saida, escrita a mao. Ver {@link LeitorDeRespostaEstruturada}. */
    private static final String FORMA_DA_RESPOSTA = LeitorDeRespostaEstruturada.instrucaoCompacta("""
            {"desafios": [
              {"enunciado": "<o pedido ao aluno>", "contextoDaCena": "<a situacao>",
               "respostaDeReferencia": "<em ingles>", "criterioDeAvaliacao": "<o que verificar>"}]}
            """);

    private final ChatClient clienteDeChat;
    private final PropriedadesDoAgente propriedades;
    private final MedidorDeChamada medidor;
    private final LeitorDeRespostaEstruturada<LoteDeDesafios> leitor =
            new LeitorDeRespostaEstruturada<>(LoteDeDesafios.class);

    public GeradorDeDesafioComClaude(AnthropicChatModel modeloDeChat,
                                     PropriedadesDoAgente propriedades,
                                     MedidorDeChamada medidor) {
        this.clienteDeChat = ChatClient.create(modeloDeChat);
        this.propriedades = propriedades;
        this.medidor = medidor;
    }

    @Override
    public List<DesafioGerado> gerar(PedidoDeGeracao pedido, int quantidade) {
        log.debug("Gerando {} desafio(s) do modulo {} no tema {}",
                quantidade, pedido.codigoDoModulo(), pedido.nomeDoTema());

        var resposta = clienteDeChat.prompt()
                .system(INSTRUCAO_DO_SISTEMA + FORMA_DA_RESPOSTA)
                .user(montarPedido(pedido, quantidade))
                .options(AnthropicChatOptions.builder()
                        .model(Model.of(propriedades.modeloDeGeracao()))
                        .maxTokens(Math.max(TOKENS_MINIMOS, quantidade * TOKENS_POR_DESAFIO)))
                .call()
                .chatResponse();

        List<DesafioGerado> desafios;
        try {
            desafios = leitor.converter(resposta).desafios();
        } catch (RuntimeException respostaIlegivel) {
            // Resposta ilegivel tambem foi cobrada, e sem itens produzidos: registrar
            // zero aqui e o que faz o custo por desafio refletir a chamada perdida.
            medir(resposta, pedido, 0);
            throw respostaIlegivel;
        }

        medir(resposta, pedido, desafios == null ? 0 : desafios.size());

        if (desafios == null || desafios.isEmpty()) {
            throw new IllegalStateException("A Claude devolveu um lote de desafios vazio.");
        }
        return desafios;
    }

    private void medir(ChatResponse resposta, PedidoDeGeracao pedido, int desafiosProduzidos) {
        medidor.medir(resposta, pedido.usuarioId(), TipoDeChamada.GERACAO_DE_DESAFIO,
                propriedades.modeloDeGeracao(), desafiosProduzidos);
    }

    private String montarPedido(PedidoDeGeracao pedido, int quantidade) {
        return """
                Gere %d desafios distintos entre si.

                Conceito a medir: %s (codigo %s)
                Descricao do conceito: %s
                Nivel CEFR: %s
                Tema da cena: %s — %s
                Formato do desafio: %s
                Nota atual do usuario neste modulo: %s
                Erros recentes neste conceito: %s
                Enunciados ja usados (nao repita nenhum): %s
                """.formatted(
                quantidade,
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
