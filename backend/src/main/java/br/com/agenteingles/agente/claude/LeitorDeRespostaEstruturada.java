package br.com.agenteingles.agente.claude;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;

/**
 * Converte a resposta da Claude em objeto tipado.
 *
 * <p>Existe porque o {@code .entity()} do ChatClient nao da conta de dois detalhes reais
 * da resposta, ambos observados em producao contra a API:
 *
 * <ol>
 *   <li>Com raciocinio adaptativo a resposta pode vir em mais de um bloco — o de
 *       raciocinio, cujo texto vem vazio, antes do bloco com o JSON. O {@code .entity()}
 *       le apenas o primeiro e quebra com "No content to map due to end-of-input".</li>
 *   <li>O modelo as vezes abre uma cerca de codigo (```) e nao a fecha. O conversor do
 *       Spring AI tenta remover a cerca procurando o par e acaba devolvendo texto vazio,
 *       com a mesma mensagem enganosa de fim de entrada.</li>
 * </ol>
 *
 * <p>A instrucao de formato tambem passa a ser responsabilidade de quem chama, porque
 * ela so e anexada automaticamente pelo {@code .entity()}.
 */
class LeitorDeRespostaEstruturada<T> {

    private static final Logger log = LoggerFactory.getLogger(LeitorDeRespostaEstruturada.class);

    private static final int TAMANHO_NO_ERRO = 800;

    private final BeanOutputConverter<T> conversor;

    LeitorDeRespostaEstruturada(Class<T> tipo) {
        this.conversor = new BeanOutputConverter<>(tipo);
    }

    /** Descricao do JSON esperado, para anexar ao pedido enviado ao modelo. */
    String instrucaoDeFormato() {
        return conversor.getFormat();
    }

    T converter(ChatResponse resposta) {
        if (resposta == null || resposta.getResults() == null || resposta.getResults().isEmpty()) {
            throw new RespostaIlegivelDaClaudeException("A Claude nao devolveu nenhum bloco.", null);
        }

        String texto = resposta.getResults().stream()
                .map(geracao -> geracao.getOutput().getText())
                .filter(bloco -> bloco != null && !bloco.isBlank())
                .collect(Collectors.joining());

        log.debug("Blocos recebidos: {} | texto util: {} caracteres",
                resposta.getResults().size(), texto.length());

        if (texto.isBlank()) {
            // Distinto de "veio texto sem JSON": aqui o modelo nao produziu saida nenhuma,
            // tipicamente por estouro de max_tokens no raciocinio.
            throw new RespostaIlegivelDaClaudeException(
                    "A Claude devolveu %d bloco(s), nenhum com texto."
                            .formatted(resposta.getResults().size()), null);
        }

        String json = extrairJson(texto);
        try {
            return conversor.convert(json);
        } catch (RuntimeException falha) {
            // Sem o texto na mensagem o sintoma seria so "end-of-input", sem pista do porque.
            throw new RespostaIlegivelDaClaudeException(
                    "Nao foi possivel ler o JSON da Claude. Texto recebido: <<%s>>"
                            .formatted(resumir(texto)), falha);
        }
    }

    /**
     * Recorta o objeto JSON de dentro do texto, do primeiro {@code &#123;} ao ultimo
     * {@code &#125;}. Resolve cerca de codigo aberta, cerca com linguagem declarada e
     * qualquer frase que o modelo escreva antes ou depois do JSON, sem depender de o
     * modelo obedecer a instrucao de nao usar markdown.
     */
    private String extrairJson(String texto) {
        int inicio = texto.indexOf('{');
        int fim = texto.lastIndexOf('}');

        if (inicio < 0 || fim <= inicio) {
            throw new RespostaIlegivelDaClaudeException(
                    "A resposta da Claude nao contem um objeto JSON. Texto recebido: <<%s>>"
                            .formatted(resumir(texto)), null);
        }

        return texto.substring(inicio, fim + 1);
    }

    private String resumir(String texto) {
        return texto.length() <= TAMANHO_NO_ERRO
                ? texto
                : texto.substring(0, TAMANHO_NO_ERRO) + "... (+" + (texto.length() - TAMANHO_NO_ERRO) + ")";
    }

    /** Falha explicita: sem isto o sintoma seria um erro de desserializacao sem contexto. */
    static class RespostaIlegivelDaClaudeException extends RuntimeException {
        RespostaIlegivelDaClaudeException(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }
}
