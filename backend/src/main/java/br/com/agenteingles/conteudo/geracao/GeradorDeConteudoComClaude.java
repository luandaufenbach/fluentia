package br.com.agenteingles.conteudo.geracao;

import br.com.agenteingles.agente.PropriedadesDoAgente;
import br.com.agenteingles.agente.claude.LeitorDeRespostaEstruturada;
import br.com.agenteingles.modulo.Modulo;
import com.anthropic.models.messages.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import br.com.agenteingles.agente.claude.MedidorDeChamada;
import br.com.agenteingles.custo.TipoDeChamada;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Escreve o conteudo de ensino de um modulo.
 *
 * <p>Roda uma vez por modulo e o resultado vira migration versionada — nao e chamado
 * em tempo de uso. Por isso fica fora do {@code usar-claude}, que liga os agentes do
 * loop: gerar conteudo e tarefa de manutencao, nao de execucao do produto.
 */
@Component
@Profile("gerar-conteudo")
public class GeradorDeConteudoComClaude {

    private static final Logger log = LoggerFactory.getLogger(GeradorDeConteudoComClaude.class);

    private static final int MAXIMO_DE_TOKENS = 8000;

    /*
     * Escrito com acentuação de propósito, ao contrário do resto dos comentários do
     * projeto: a primeira versão deste prompt vinha sem acento e o modelo espelhou o
     * estilo, devolvendo todo o material didático sem acentuação nenhuma.
     */
    private static final String INSTRUCAO_DO_SISTEMA = """
            Você escreve o material de estudo de um curso de inglês para brasileiros,
            organizado por nível CEFR.

            O aluno lê este material ANTES de fazer os exercícios do conceito, e volta
            a ele quando erra. Então ele precisa bastar sozinho.

            Regras de escrita:
            - Escreva em português do Brasil com ORTOGRAFIA COMPLETA E CORRETA, incluindo
              todos os acentos e a cedilha: é, ê, á, â, ã, ó, ô, õ, í, ú, ç. Texto sem
              acento está errado e não pode ser entregue. Apenas as frases de exemplo em
              inglês ficam em inglês.
            - A explicação vai em parágrafos separados por linha em branco. Não use
              markdown, nem título, nem lista com marcador, nem asterisco.
            - Escreva aspas simples e apóstrofos normalmente quando precisar deles.

            Regras de conteúdo:
            - Explique o conceito do zero, sem supor que o aluno já viu o assunto.
            - Calibre a profundidade pelo nível CEFR informado. Em A1 seja concreto e
              direto; em C1/C2 pode tratar nuance e registro.
            - Dê de 4 a 6 exemplos, do mais simples ao mais completo, cada um com tradução.
              A observação do exemplo explica a escolha da forma, e pode ficar vazia
              quando a frase for óbvia.
            - Dê de 3 a 4 erros comuns de brasileiro aprendendo inglês neste conceito
              específico, sempre no formato errado -> certo, com o porquê.
            - Não mencione que você é uma IA e não fale com o aluno sobre o curso.
            """;

    private final ChatClient clienteDeChat;
    private final PropriedadesDoAgente propriedades;
    private final LeitorDeRespostaEstruturada<ConteudoGerado> leitor =
            new LeitorDeRespostaEstruturada<>(ConteudoGerado.class);

    private final MedidorDeChamada medidor;

    public GeradorDeConteudoComClaude(AnthropicChatModel modeloDeChat,
                                      PropriedadesDoAgente propriedades,
                                      MedidorDeChamada medidor) {
        this.clienteDeChat = ChatClient.create(modeloDeChat);
        this.medidor = medidor;
        this.propriedades = propriedades;
    }

    /**
     * Uma retentativa antes de desistir: em texto longo o modelo as vezes escapa mal
     * uma aspa e invalida o JSON inteiro. Regerar custa menos que perder o modulo, e
     * o defeito nao se repete na segunda tentativa.
     */
    public ConteudoGerado gerar(Modulo modulo) {
        log.info("Gerando conteudo do modulo {} ({})", modulo.getCodigo(), modulo.getNivelCefr());

        try {
            return pedirAClaude(modulo);
        } catch (LeitorDeRespostaEstruturada.RespostaIlegivelDaClaudeException falha) {
            log.warn("JSON invalido no modulo {}. Tentando de novo.", modulo.getCodigo());
            return pedirAClaude(modulo);
        }
    }

    private ConteudoGerado pedirAClaude(Modulo modulo) {
        var resposta = clienteDeChat.prompt()
                .system(INSTRUCAO_DO_SISTEMA)
                .user(montarPedido(modulo) + "\n" + leitor.instrucaoDeFormato())
                .options(AnthropicChatOptions.builder()
                        .model(Model.of(propriedades.modeloDeRaciocinio()))
                        .maxTokens(MAXIMO_DE_TOKENS)
                        .thinkingAdaptive())
                .call()
                .chatResponse();

        // Rotina avulsa, sem dono: o custo entra no total do sistema, nao no de uma conta.
        medidor.medir(resposta, null, TipoDeChamada.GERACAO_DE_CONTEUDO, propriedades.modeloDeRaciocinio(), 1);

        return leitor.converter(resposta);
    }

    private String montarPedido(Modulo modulo) {
        return """
                Conceito: %s
                Codigo: %s
                Nivel CEFR: %s
                Escopo a cobrir: %s
                """.formatted(
                modulo.getNome(),
                modulo.getCodigo(),
                modulo.getNivelCefr(),
                modulo.getDescricao());
    }

}
