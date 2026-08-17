package br.com.agenteingles.agente.claude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.agenteingles.agente.ResultadoDaAvaliacao;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

/**
 * Cobre os formatos de resposta que a Claude devolveu de verdade e que quebravam o
 * {@code .entity()} do Spring AI. Cada caso aqui e um erro 500 que ja aconteceu em execucao.
 */
class LeitorDeRespostaEstruturadaTest {

    private final LeitorDeRespostaEstruturada<ResultadoDaAvaliacao> leitor =
            new LeitorDeRespostaEstruturada<>(ResultadoDaAvaliacao.class);

    private static final String JSON = """
            {"notaObtida":5,"feedback":"Falta concordancia.","erros":[
              {"tipo":"concordancia_do_verbo_to_be","trechoErrado":"She are","correcao":"She is",
               "explicacao":"Terceira pessoa do singular usa is."}]}
            """;

    private ChatResponse respostaCom(String... blocos) {
        List<Generation> geracoes = Arrays.stream(blocos)
                .map(texto -> new Generation(new AssistantMessage(texto)))
                .toList();
        return new ChatResponse(geracoes);
    }

    @Test
    @DisplayName("le o JSON puro")
    void leJsonPuro() {
        var resultado = leitor.converter(respostaCom(JSON));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("5");
        assertThat(resultado.erros()).hasSize(1);
    }

    @Test
    @DisplayName("le mesmo quando o modelo abre a cerca de codigo e nao fecha")
    void leCercaDeCodigoAberta() {
        var resultado = leitor.converter(respostaCom("```" + JSON));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("le mesmo com cerca fechada e linguagem declarada")
    void leCercaCompleta() {
        var resultado = leitor.converter(respostaCom("```json\n" + JSON + "\n```"));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("le mesmo com frase antes e depois do JSON")
    void leComTextoEmVolta() {
        var resultado = leitor.converter(respostaCom("Segue a avaliacao:\n" + JSON + "\nEspero ter ajudado."));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("junta os blocos quando o raciocinio adaptativo devolve um bloco vazio antes")
    void juntaBlocoDeRaciocinioVazio() {
        var resultado = leitor.converter(respostaCom("", JSON));

        assertThat(resultado.notaObtida()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("falha com o texto recebido na mensagem quando nao ha JSON")
    void falhaComTextoNaMensagem() {
        assertThatThrownBy(() -> leitor.converter(respostaCom("Nao consigo avaliar isso.")))
                .isInstanceOf(LeitorDeRespostaEstruturada.RespostaIlegivelDaClaudeException.class)
                .hasMessageContaining("Nao consigo avaliar isso.");
    }

    @Test
    @DisplayName("falha explicitamente quando nenhum bloco tem texto")
    void falhaQuandoTudoVazio() {
        assertThatThrownBy(() -> leitor.converter(respostaCom("", "  ")))
                .isInstanceOf(LeitorDeRespostaEstruturada.RespostaIlegivelDaClaudeException.class)
                .hasMessageContaining("nenhum com texto");
    }
}
