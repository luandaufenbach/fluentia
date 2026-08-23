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

    /**
     * Cenas por tema, para o desafio nunca sair na mesma roupagem duas vezes seguidas.
     *
     * <p>A chave e o nome do tema como ele esta no banco, com acento. Cena de tema
     * desconhecido cai em {@link #CENAS_PADRAO}, entao errar a grafia aqui nao quebra
     * nada — so tira a cor da cena, sem aviso nenhum. Ha teste cobrindo isso.
     */
    private static final Map<String, List<String>> CENAS_POR_TEMA = Map.of(
            "Conversação livre", List.of(
                    "Você acabou de conhecer alguém numa festa.",
                    "Você está contando como foi o seu dia.",
                    "Você está apresentando a sua família para alguém."),
            "Viagem", List.of(
                    "Você está no balcão de check-in do aeroporto.",
                    "Você acabou de chegar na recepção do hotel.",
                    "Você está pedindo informação na rua de uma cidade nova."),
            "Trabalho", List.of(
                    "Você está se apresentando na primeira reunião com o time.",
                    "Você está respondendo um e-mail do seu gerente.",
                    "Você está numa entrevista de emprego."),
            "Cultura e expressões", List.of(
                    "Você está comentando um filme com um amigo.",
                    "Você está explicando um costume do seu país.",
                    "Você está conversando sobre música numa roda de amigos."),
            "Comida e restaurante", List.of(
                    "Você está pedindo o prato num restaurante.",
                    "Você está explicando ao garçom o que não pode comer.",
                    "Você está recomendando um lugar para um amigo."),
            "Compras e serviços", List.of(
                    "Você está trocando uma peça de roupa na loja.",
                    "Você está resolvendo um problema na conta do banco.",
                    "Você está comprando um remédio na farmácia."),
            "Saúde e bem-estar", List.of(
                    "Você está explicando um sintoma na consulta.",
                    "Você está marcando um horário por telefone.",
                    "Você está falando da sua rotina de exercícios."),
            "Vida social", List.of(
                    "Você está combinando de sair no fim de semana.",
                    "Você está contando uma novidade para um amigo.",
                    "Você está recusando um convite com educação."),
            "Casa e rotina", List.of(
                    "Você está falando com o vizinho sobre um barulho.",
                    "Você está dividindo as tarefas da casa.",
                    "Você está explicando um problema para o síndico."));

    private static final List<String> CENAS_PADRAO = List.of(
            "Você está numa conversa do dia a dia.",
            "Você está explicando uma situação para alguém.");

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

    /** Se o tema tem cenas proprias. Existe para o teste pegar erro de grafia na chave. */
    public static boolean temCenasProprias(String nomeDoTema) {
        return CENAS_POR_TEMA.containsKey(nomeDoTema);
    }

    private DesafioGerado gerarUm(PedidoDeGeracao pedido, List<String> jaUsados) {
        List<String> cenas = CENAS_POR_TEMA.getOrDefault(pedido.nomeDoTema(), CENAS_PADRAO);
        String cena = sortear(cenas) + montarReforco(pedido.errosRecentes());

        BancoDeAlvos banco = BancoDeAlvos.doModulo(pedido.codigoDoModulo());
        if (banco == null) {
            // Modulo sem banco proprio: o desafio sai correto, mas sem gabarito — e sem
            // gabarito o avaliador simulado nao tem como dar uma nota que signifique algo.
            return new DesafioGerado(
                    "Escreva uma frase em inglês usando " + pedido.nomeDoModulo().toLowerCase(Locale.ROOT) + ".",
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
        return "Traduza para o inglês: \"" + alvo.emPortugues() + "\"";
    }

    private DesafioGerado montarDesafio(BancoDeAlvos.Alvo alvo, String cena, PedidoDeGeracao pedido) {
        return new DesafioGerado(
                montarEnunciado(alvo),
                cena,
                alvo.emIngles(),
                "Verificar o uso correto de " + pedido.nomeDoModulo()
                        + ", comparando com a resposta de referência.");
    }

    /** Se o usuario vem errando algo especifico, a cena avisa o que sera cobrado. */
    private String montarReforco(List<String> errosRecentes) {
        if (errosRecentes == null || errosRecentes.isEmpty()) {
            return "";
        }
        List<String> distintos = new ArrayList<>(new LinkedHashSet<>(errosRecentes));
        return " Atenção ao ponto que você vem errando: " + distintos.get(0).replace('_', ' ') + ".";
    }

    private <T> T sortear(List<T> opcoes) {
        return opcoes.get(ThreadLocalRandom.current().nextInt(opcoes.size()));
    }
}
