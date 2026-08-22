package br.com.agenteingles.agente.simulado;

import br.com.agenteingles.agente.AgenteGeradorDeDesafio;
import br.com.agenteingles.agente.DesafioGerado;
import br.com.agenteingles.agente.PedidoDeGeracao;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Gerador simulado: monta o desafio combinando uma cena do tema com um alvo do conceito,
 * evitando os enunciados ja usados. Serve para validar o loop de ponta a ponta sem custo de API.
 * Quando {@code agente-ingles.usar-claude=true}, o gerador com Claude assume no lugar deste.
 */
@Component
@ConditionalOnProperty(name = "agente-ingles.usar-claude", havingValue = "false", matchIfMissing = true)
public class GeradorDeDesafioSimulado implements AgenteGeradorDeDesafio {

    /** Cenas por tema, para o desafio nunca sair na mesma roupagem duas vezes seguidas. */
    private static final Map<String, List<String>> CENAS_POR_TEMA = Map.of(
            "Viagem", List.of(
                    "Voce esta no balcao de check-in do aeroporto.",
                    "Voce acabou de chegar na recepcao do hotel.",
                    "Voce esta pedindo o cardapio num restaurante em Londres."),
            "Trabalho", List.of(
                    "Voce esta se apresentando na primeira reuniao com o time.",
                    "Voce esta respondendo um e-mail do seu gerente.",
                    "Voce esta numa entrevista de emprego."),
            "Ingles para dev", List.of(
                    "Voce esta escrevendo um comentario num code review.",
                    "Voce esta na daily explicando o status da sua tarefa.",
                    "Voce esta descrevendo um bug num ticket."),
            "Cultura e expressoes", List.of(
                    "Voce esta comentando um filme com um amigo.",
                    "Voce esta explicando um costume do seu pais.",
                    "Voce esta conversando sobre musica numa roda de amigos."),
            "Conversacao livre", List.of(
                    "Voce acabou de conhecer alguem numa festa.",
                    "Voce esta contando como foi o seu dia.",
                    "Voce esta apresentando a sua familia para alguem."));

    private static final List<String> CENAS_PADRAO = List.of(
            "Voce esta numa conversa do dia a dia.",
            "Voce esta explicando uma situacao para alguem.");

    /** Alvos do modulo "verbo to be": frase em portugues e a versao correta em ingles. */
    private static final List<String[]> ALVOS_DO_VERBO_TO_BE = List.of(
            new String[] {"Eu sou brasileiro.", "I am Brazilian."},
            new String[] {"Ela e a minha gerente.", "She is my manager."},
            new String[] {"Nos estamos atrasados.", "We are late."},
            new String[] {"Eles nao estao prontos.", "They are not ready."},
            new String[] {"Voce e o novo desenvolvedor?", "Are you the new developer?"},
            new String[] {"O quarto nao esta limpo.", "The room is not clean."},
            new String[] {"Eu nao estou com fome.", "I am not hungry."},
            new String[] {"Ele esta no aeroporto.", "He is at the airport."},
            new String[] {"Nos somos do Brasil.", "We are from Brazil."},
            new String[] {"O teste esta quebrado.", "The test is broken."},
            new String[] {"Elas sao engenheiras.", "They are engineers."},
            new String[] {"Esta e a minha primeira vez aqui.", "This is my first time here."},
            new String[] {"O deploy nao esta pronto.", "The deploy is not ready."},
            new String[] {"Voces estao na reuniao?", "Are you in the meeting?"},
            new String[] {"Eu estou aprendendo ingles.", "I am learning English."});

    private static final String CODIGO_DO_MODULO_COM_BANCO_PROPRIO = "verbo_to_be";

    @Override
    public List<DesafioGerado> gerar(PedidoDeGeracao pedido, int quantidade) {
        List<DesafioGerado> gerados = new ArrayList<>();
        List<String> jaUsados = new ArrayList<>(
                pedido.enunciadosRecentes() == null ? List.of() : pedido.enunciadosRecentes());

        for (int i = 0; i < quantidade; i++) {
            // Cada desafio ja gerado nesta rodada entra na lista de usados: sem isso o
            // lote sairia com enunciados repetidos entre si.
            DesafioGerado desafio = gerarUm(pedido, jaUsados);
            gerados.add(desafio);
            jaUsados.add(desafio.enunciado());
        }
        return gerados;
    }

    private DesafioGerado gerarUm(PedidoDeGeracao pedido, List<String> jaUsados) {
        List<String> cenas = CENAS_POR_TEMA.getOrDefault(pedido.nomeDoTema(), CENAS_PADRAO);
        String cena = sortear(cenas) + montarReforco(pedido.errosRecentes());

        if (!CODIGO_DO_MODULO_COM_BANCO_PROPRIO.equals(pedido.codigoDoModulo())) {
            // Demais modulos ainda nao tem banco proprio no simulado: o desafio sai generico mas correto.
            return new DesafioGerado(
                    "Escreva uma frase em ingles usando " + pedido.nomeDoModulo().toLowerCase(Locale.ROOT) + ".",
                    cena,
                    null,
                    "Verificar o uso correto de " + pedido.nomeDoModulo() + " conforme: " + pedido.descricaoDoModulo());
        }

        // Filtra os alvos ainda nao usados em vez de sortear as cegas: com sorteio puro o ultimo
        // alvo disponivel pode nunca ser encontrado, e o desafio sairia repetido.
        List<String[]> disponiveis = new ArrayList<>();
        for (String[] alvo : ALVOS_DO_VERBO_TO_BE) {
            if (!jaUsados.contains(montarEnunciado(alvo))) {
                disponiveis.add(alvo);
            }
        }
        if (!disponiveis.isEmpty()) {
            return montarDesafio(sortear(disponiveis), cena);
        }

        // Banco esgotado: marca a rodada para o enunciado continuar inedito.
        DesafioGerado desafio = montarDesafio(sortear(ALVOS_DO_VERBO_TO_BE), cena);
        int rodada = jaUsados.size() / ALVOS_DO_VERBO_TO_BE.size() + 1;
        return new DesafioGerado(
                desafio.enunciado() + " (rodada " + rodada + ")",
                desafio.contextoDaCena(),
                desafio.respostaDeReferencia(),
                desafio.criterioDeAvaliacao());
    }

    private String montarEnunciado(String[] alvo) {
        return "Traduza para o ingles: \"" + alvo[0] + "\"";
    }

    private DesafioGerado montarDesafio(String[] alvo, String cena) {
        return new DesafioGerado(
                montarEnunciado(alvo),
                cena,
                alvo[1],
                "Verificar a forma correta do verbo \"to be\" (am/is/are), a concordancia com o sujeito "
                        + "e a estrutura de negativa ou pergunta quando a frase pedir.");
    }

    /** Se o usuario vem errando algo especifico, a cena avisa o que sera cobrado. */
    private String montarReforco(List<String> errosRecentes) {
        if (errosRecentes == null || errosRecentes.isEmpty()) {
            return "";
        }
        List<String> distintos = new ArrayList<>(new LinkedHashSet<>(errosRecentes));
        return " Atencao ao ponto que voce vem errando: " + distintos.get(0).replace('_', ' ') + ".";
    }

    private <T> T sortear(List<T> opcoes) {
        return opcoes.get(ThreadLocalRandom.current().nextInt(opcoes.size()));
    }
}
