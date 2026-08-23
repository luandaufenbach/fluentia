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

        BancoDeAlvos banco = BancoDeAlvos.doModulo(pedido.codigoDoModulo());
        if (banco == null) {
            // Modulo sem banco proprio: o desafio sai correto, mas sem gabarito — e sem
            // gabarito o avaliador simulado nao tem como dar uma nota que signifique algo.
            return new DesafioGerado(
                    "Escreva uma frase em ingles usando " + pedido.nomeDoModulo().toLowerCase(Locale.ROOT) + ".",
                    cena,
                    null,
                    "Verificar o uso correto de " + pedido.nomeDoModulo() + " conforme: "
                            + pedido.descricaoDoModulo());
        }

        // Filtra os alvos ainda nao usados em vez de sortear as cegas: com sorteio puro o
        // ultimo alvo disponivel pode nunca ser encontrado, e o desafio sairia repetido.
        List<BancoDeAlvos.Alvo> disponiveis = new ArrayList<>();
        for (BancoDeAlvos.Alvo alvo : banco.alvos()) {
            if (!jaUsados.contains(montarEnunciado(alvo))) {
                disponiveis.add(alvo);
            }
        }
        if (!disponiveis.isEmpty()) {
            return montarDesafio(sortear(disponiveis), cena, pedido);
        }

        // Banco esgotado: marca a rodada para o enunciado continuar inedito.
        DesafioGerado desafio = montarDesafio(sortear(banco.alvos()), cena, pedido);
        int rodada = jaUsados.size() / banco.alvos().size() + 1;
        return new DesafioGerado(
                desafio.enunciado() + " (rodada " + rodada + ")",
                desafio.contextoDaCena(),
                desafio.respostaDeReferencia(),
                desafio.criterioDeAvaliacao());
    }

    private String montarEnunciado(BancoDeAlvos.Alvo alvo) {
        return "Traduza para o ingles: \"" + alvo.emPortugues() + "\"";
    }

    private DesafioGerado montarDesafio(BancoDeAlvos.Alvo alvo, String cena, PedidoDeGeracao pedido) {
        return new DesafioGerado(
                montarEnunciado(alvo),
                cena,
                alvo.emIngles(),
                "Verificar o uso correto de " + pedido.nomeDoModulo()
                        + ", comparando com a resposta de referencia.");
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
