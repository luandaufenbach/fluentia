package br.com.agenteingles.agente.claude;

import br.com.agenteingles.agente.PedidoDeAvaliacao;
import br.com.agenteingles.agente.PropriedadesDoAgente;
import br.com.agenteingles.agente.ResultadoDaAvaliacao;
import br.com.agenteingles.modulo.NivelCefr;
import br.com.agenteingles.usuario.TipoDeCorrecao;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Compara a correcao COM e SEM raciocinio estendido, contra a API de verdade.
 *
 * <p>Existe porque o raciocinio e o item mais caro do app e a decisao de desliga-lo nao
 * pode ser tomada no palpite: ele nao aparece na resposta, mas e cobrado como saida, que
 * custa cinco vezes a entrada. A pergunta que este teste responde e uma so — a correcao
 * piora sem ele?
 *
 * <p><b>Nao roda na suite normal: gasta dinheiro de verdade.</b> O gatilho e a variavel
 * COMPARAR_MODELOS, e nao a presenca da chave — quem trabalha neste projeto costuma ter
 * ANTHROPIC_API_KEY no ambiente, e gatear pela chave faria a suite inteira gastar sem
 * ninguem pedir. Rodar de proposito precisa ser um ato deliberado:
 *
 * <pre>COMPARAR_MODELOS=1 ./mvnw -Dtest=ComparacaoDeRaciocinioIT test</pre>
 */
@SpringBootTest
@TestPropertySource(properties = "agente-ingles.usar-claude=true")
@EnabledIfEnvironmentVariable(named = "COMPARAR_MODELOS", matches = ".+")
class ComparacaoDeRaciocinioIT {

    /** Casos escolhidos para cobrir os tres vereditos que importam. */
    private record Caso(String descricao, String respostaDoAluno, String esperado) {
    }

    private static final List<Caso> CASOS = List.of(
            new Caso("resposta certa", "I am a developer and I am from Brazil.",
                    "nota alta, sem erros"),
            new Caso("erro classico de concordancia", "I are a developer and she are from Brazil.",
                    "nota baixa, erro de concordancia do verbo to be"),
            new Caso("certa mas em outra forma", "I'm a developer. Brazil is where I'm from.",
                    "nota alta: parafrase correta nao pode ser punida"),
            new Caso("deslize pequeno", "I am a developer and I from Brazil.",
                    "nota media: falta o verbo na segunda oracao"));

    @Autowired
    private AnthropicChatModel modeloDeChat;

    @Autowired
    private MedidorDeChamada medidor;

    @Autowired
    private PropriedadesDoAgente propriedadesReais;

    private PropriedadesDoAgente com(String modelo, boolean raciocinio) {
        return new PropriedadesDoAgente(true, propriedadesReais.modeloDeRaciocinio(),
                propriedadesReais.modeloDeGeracao(), propriedadesReais.modeloSimples(),
                propriedadesReais.desafiosPorLote(), propriedadesReais.desafiosPorLoteInicial(),
                Map.of(), propriedadesReais.emailDoUsuarioPadrao(), raciocinio, modelo);
    }

    private PedidoDeAvaliacao pedido(Caso caso) {
        return new PedidoDeAvaliacao(
                null,
                "verbo_to_be",
                "Verbo \"to be\"",
                "Formas am/is/are no presente, afirmativa, negativa e interrogativa.",
                NivelCefr.A1,
                "Diga seu nome, de onde voce e e o que voce faz.",
                "Voce se apresenta a um colega novo no primeiro dia de trabalho.",
                "I am a developer and I am from Brazil.",
                "Verificar o uso correto de am/is/are com cada sujeito.",
                caso.respostaDoAluno(),
                TipoDeCorrecao.RESUMIDA);
    }

    @Test
    void compararSonnetComHaiku() {
        var sonnet = new AvaliadorComClaude(modeloDeChat, com("claude-sonnet-5", true), medidor);
        var haiku = new AvaliadorComClaude(modeloDeChat, com("claude-haiku-4-5", false), medidor);

        System.out.println("\n============ SONNET x HAIKU ============");
        for (Caso caso : CASOS) {
            System.out.printf("%n--- %s ---%n    aluno: \"%s\"%n    esperado: %s%n",
                    caso.descricao(), caso.respostaDoAluno(), caso.esperado());

            ResultadoDaAvaliacao a = sonnet.avaliar(pedido(caso));
            ResultadoDaAvaliacao b = haiku.avaliar(pedido(caso));

            imprimir("SONNET", a);
            imprimir("HAIKU ", b);
            System.out.printf("    diferenca de nota: %s%n",
                    a.notaObtida().subtract(b.notaObtida()).abs());
        }
        System.out.println("\n========================================\n");
    }

    private void imprimir(String rotulo, ResultadoDaAvaliacao r) {
        String tipos = r.erros() == null || r.erros().isEmpty()
                ? "(nenhum)"
                : String.join(", ", r.erros().stream().map(e -> e.tipo()).toList());
        System.out.printf("    %s nota %-5s erros: %-45s feedback: %s%n",
                rotulo, r.notaObtida(), tipos, r.feedback());
    }
}
