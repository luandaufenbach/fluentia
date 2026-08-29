package br.com.agenteingles.agente.claude;

import br.com.agenteingles.agente.AgenteAvaliador;
import br.com.agenteingles.agente.PedidoDeAvaliacao;
import br.com.agenteingles.agente.PropriedadesDoAgente;
import br.com.agenteingles.agente.ResultadoDaAvaliacao;
import br.com.agenteingles.custo.TipoDeChamada;
import br.com.agenteingles.desafio.CatalogoDeTiposDeErro;
import br.com.agenteingles.usuario.TipoDeCorrecao;
import com.anthropic.models.messages.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Avaliador apoiado na Claude. Usa o modelo de raciocinio com raciocinio adaptativo:
 * a nota que sai daqui alimenta a media do modulo e a decisao do orquestrador, entao
 * um erro de correcao se propaga por todo o curriculo do usuario.
 */
@Component
@ConditionalOnProperty(name = "agente-ingles.usar-claude", havingValue = "true")
public class AvaliadorComClaude implements AgenteAvaliador {

    private static final Logger log = LoggerFactory.getLogger(AvaliadorComClaude.class);

    private static final int MAXIMO_DE_TOKENS = 4000;

    private static final String INSTRUCAO_DO_SISTEMA = """
            Voce e o agente avaliador de um curriculo adaptativo de ingles.

            Sua tarefa e julgar a resposta do aluno ao desafio e apontar o erro especifico.

            Regras:
            - Avalie apenas o conceito que o desafio estava medindo. Nao desconte por questoes
              de estilo ou por escolhas de vocabulario que tambem estariam corretas.
            - A nota vai de 0 a 10 e se refere somente a esta resposta.
              10 = correta; 7 a 9 = correta com deslize menor; 4 a 6 = conceito parcialmente
              aplicado; 1 a 3 = conceito aplicado errado; 0 = sem resposta ou fora do pedido.
            - Para cada erro, informe o trecho exato que esta errado, a correcao e a explicacao.
            - O tipo do erro precisa ser UM dos valores desta lista, exatamente como escrito:
              %s
              Use "outro" quando nenhum servir. Nao invente nomes novos: o tipo e a chave que
              conta quantas vezes o aluno repetiu o mesmo erro, e um nome diferente para o
              mesmo problema quebra essa contagem.
            - Se a resposta estiver correta, devolva a lista de erros vazia.
            - O feedback e as explicacoes vao em portugues; os trechos e correcoes, em ingles.
            - Escreva o portugues com a ortografia correta, COM acentos e cedilha
              ("Voce" esta errado, "Você" esta certo). Este texto e lido pelo aluno.
              Estas instrucoes vem sem acento por convencao do codigo-fonte: nao imite
              esse estilo, ele nao vale para o que voce escreve.
            """.formatted(CatalogoDeTiposDeErro.paraOPrompt());

    /**
     * Instrucao de tamanho conforme a preferencia do aluno. Alem de respeitar a escolha,
     * e o maior controle de custo aqui: o token de saida custa cinco vezes o de entrada,
     * entao encurtar a correcao pesa mais do que encurtar o pedido.
     */
    private static final String CORRECAO_RESUMIDA = """

            Formato desta correcao: RESUMIDA.
            - Feedback em no maximo uma frase.
            - Explicacao de cada erro em no maximo uma frase curta.
            - Nao repita a regra geral do conceito: aponte so o que esta errado nesta resposta.
            """;

    private static final String CORRECAO_DETALHADA = """

            Formato desta correcao: DETALHADA.
            - Feedback em ate tres frases, dizendo o que ficou bom antes do que precisa mudar.
            - Explicacao de cada erro dizendo o porque da regra, nao so qual e a forma certa.
            """;

    /**
     * A forma do JSON de saida, escrita a mao.
     *
     * <p>Fica junto da instrucao do sistema, e nao no fim do pedido, porque e instrucao
     * fixa e nao dado da requisicao — o lugar dela e onde estao as outras regras.
     */
    private static final String FORMA_DA_RESPOSTA = LeitorDeRespostaEstruturada.instrucaoCompacta("""
            {"notaObtida": <numero de 0 a 10>, "feedback": "<texto>", "erros": [
              {"tipo": "<um da lista>", "trechoErrado": "<em ingles>",
               "correcao": "<em ingles>", "explicacao": "<em portugues>"}]}
            Sem erros, "erros" vem [].
            """);

    private final ChatClient clienteDeChat;
    private final PropriedadesDoAgente propriedades;
    private final MedidorDeChamada medidor;
    private final LeitorDeRespostaEstruturada<ResultadoDaAvaliacao> leitor =
            new LeitorDeRespostaEstruturada<>(ResultadoDaAvaliacao.class);

    public AvaliadorComClaude(AnthropicChatModel modeloDeChat,
                              PropriedadesDoAgente propriedades,
                              MedidorDeChamada medidor) {
        this.clienteDeChat = ChatClient.create(modeloDeChat);
        this.propriedades = propriedades;
        this.medidor = medidor;
    }

    @Override
    public ResultadoDaAvaliacao avaliar(PedidoDeAvaliacao pedido) {
        log.debug("Avaliando resposta do modulo {}", pedido.codigoDoModulo());

        var resposta = clienteDeChat.prompt()
                .system(INSTRUCAO_DO_SISTEMA + formatoDaCorrecao(pedido) + FORMA_DA_RESPOSTA)
                .user(montarPedido(pedido))
                .options(opcoes())
                .call()
                .chatResponse();

        // Uma chamada, uma correcao — mesmo quando a resposta vem ilegivel e a
        // avaliacao se perde, a chamada ja foi cobrada e precisa aparecer no total.
        medidor.medir(resposta, pedido.usuarioId(), TipoDeChamada.AVALIACAO_DE_RESPOSTA,
                propriedades.modeloDeAvaliacao(), 1);

        return leitor.converter(resposta);
    }

    /**
     * O raciocinio estendido e o item mais caro desta chamada, e por isso e configuravel.
     *
     * <p>Ele nao aparece na resposta, mas <b>e cobrado como saida</b> — e saida custa
     * cinco vezes a entrada. Medindo em producao, a avaliacao gastava 342 tokens de
     * saida para um JSON que ocupa cerca de 120: o resto era raciocinio.
     *
     * <p>Fica ligado por padrao. Desligar economiza de verdade, mas a nota que sai daqui
     * alimenta a media do modulo e a decisao do orquestrador — um erro de correcao se
     * propaga pelo curriculo inteiro. Entao a troca e de qualidade por dinheiro, e quem
     * decide isso e o dono do app, com numero na mao, nao um padrao escondido no codigo.
     */
    private AnthropicChatOptions.Builder opcoes() {
        AnthropicChatOptions.Builder construtor = AnthropicChatOptions.builder();
        construtor.model(Model.of(propriedades.modeloDeAvaliacao()))
                .maxTokens(MAXIMO_DE_TOKENS);

        if (propriedades.raciocinioNaAvaliacao()) {
            construtor.thinkingAdaptive();
        }
        return construtor;
    }

    private String formatoDaCorrecao(PedidoDeAvaliacao pedido) {
        return pedido.tipoDeCorrecao() == TipoDeCorrecao.RESUMIDA
                ? CORRECAO_RESUMIDA
                : CORRECAO_DETALHADA;
    }

    private String montarPedido(PedidoDeAvaliacao pedido) {
        return """
                Conceito medido: %s (codigo %s)
                Descricao do conceito: %s
                Nivel CEFR: %s
                Cena do desafio: %s
                Enunciado apresentado ao aluno: %s
                Resposta de referencia: %s
                Criterio de avaliacao: %s

                Resposta do aluno:
                %s
                """.formatted(
                pedido.nomeDoModulo(),
                pedido.codigoDoModulo(),
                pedido.descricaoDoModulo(),
                pedido.nivel(),
                textoOuAusente(pedido.contextoDaCena()),
                pedido.enunciado(),
                textoOuAusente(pedido.respostaDeReferencia()),
                textoOuAusente(pedido.criterioDeAvaliacao()),
                pedido.respostaDoUsuario());
    }

    private String textoOuAusente(String texto) {
        return texto == null || texto.isBlank() ? "(nao informado)" : texto;
    }
}
