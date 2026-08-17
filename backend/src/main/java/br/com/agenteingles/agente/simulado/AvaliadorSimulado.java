package br.com.agenteingles.agente.simulado;

import br.com.agenteingles.agente.AgenteAvaliador;
import br.com.agenteingles.agente.ErroApontado;
import br.com.agenteingles.agente.PedidoDeAvaliacao;
import br.com.agenteingles.agente.ResultadoDaAvaliacao;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Avaliador simulado: compara a resposta com a referencia e aplica heuristicas de concordancia
 * do verbo "to be". Nao substitui o avaliador real, mas produz nota e erro especifico de verdade,
 * o suficiente para o loop de reforco funcionar sem consumir a API.
 */
@Component
@ConditionalOnProperty(name = "agente-ingles.usar-claude", havingValue = "false", matchIfMissing = true)
public class AvaliadorSimulado implements AgenteAvaliador {

    /** Forma correta do verbo "to be" para cada sujeito. */
    private static final Map<String, String> FORMA_CORRETA_POR_SUJEITO = Map.of(
            "i", "am",
            "he", "is",
            "she", "is",
            "it", "is",
            "this", "is",
            "that", "is",
            "we", "are",
            "you", "are",
            "they", "are");

    private static final Set<String> FORMAS_DO_VERBO_TO_BE = Set.of("am", "is", "are");

    private static final BigDecimal NOTA_RESPOSTA_CORRETA = new BigDecimal("10.00");
    private static final BigDecimal NOTA_QUASE_CORRETA = new BigDecimal("7.00");
    private static final BigDecimal NOTA_CONCORDANCIA_ERRADA = new BigDecimal("3.00");
    private static final BigDecimal NOTA_ESTRUTURA_ERRADA = new BigDecimal("2.00");
    private static final BigDecimal NOTA_RESPOSTA_VAZIA = new BigDecimal("0.00");

    @Override
    public ResultadoDaAvaliacao avaliar(PedidoDeAvaliacao pedido) {
        String resposta = pedido.respostaDoUsuario() == null ? "" : pedido.respostaDoUsuario().trim();
        if (resposta.isEmpty()) {
            return new ResultadoDaAvaliacao(
                    NOTA_RESPOSTA_VAZIA,
                    "Nao recebi nenhuma resposta para avaliar.",
                    List.of(new ErroApontado("resposta_em_branco", null, null,
                            "A resposta chegou vazia, entao nao foi possivel avaliar o conceito.")));
        }

        String respostaNormalizada = normalizar(resposta);
        String referenciaNormalizada = normalizar(pedido.respostaDeReferencia());

        if (!referenciaNormalizada.isEmpty() && respostaNormalizada.equals(referenciaNormalizada)) {
            return new ResultadoDaAvaliacao(
                    NOTA_RESPOSTA_CORRETA,
                    "Resposta correta. A forma do verbo \"to be\" e a estrutura da frase estao certas.",
                    List.of());
        }

        List<ErroApontado> erros = procurarErrosDeConcordancia(respostaNormalizada);
        if (!erros.isEmpty()) {
            return new ResultadoDaAvaliacao(
                    NOTA_CONCORDANCIA_ERRADA,
                    "O verbo \"to be\" nao concorda com o sujeito. Reveja a tabela am/is/are"
                            + (referenciaNormalizada.isEmpty() ? "." : ". Uma resposta correta seria: \""
                                    + pedido.respostaDeReferencia() + "\"."),
                    erros);
        }

        if (referenciaNormalizada.isEmpty()) {
            // Sem referencia (modulos ainda sem banco proprio) o simulado nao arrisca um veredito forte.
            return new ResultadoDaAvaliacao(
                    NOTA_QUASE_CORRETA,
                    "Resposta registrada. O avaliador simulado nao tem gabarito para este modulo; "
                            + "ligue o avaliador com Claude para uma correcao completa.",
                    List.of());
        }

        double semelhanca = calcularSemelhanca(respostaNormalizada, referenciaNormalizada);
        if (semelhanca >= 0.7) {
            return new ResultadoDaAvaliacao(
                    NOTA_QUASE_CORRETA,
                    "Quase la. O verbo \"to be\" esta certo, mas ha diferencas em relacao a resposta esperada: \""
                            + pedido.respostaDeReferencia() + "\".",
                    List.of(new ErroApontado("detalhe_de_forma", resposta, pedido.respostaDeReferencia(),
                            "A estrutura principal esta correta, mas algum detalhe de vocabulario ou ordem "
                                    + "esta diferente do esperado.")));
        }

        return new ResultadoDaAvaliacao(
                NOTA_ESTRUTURA_ERRADA,
                "A frase esta distante do esperado. Uma resposta correta seria: \""
                        + pedido.respostaDeReferencia() + "\".",
                List.of(new ErroApontado("estrutura_da_frase", resposta, pedido.respostaDeReferencia(),
                        "A construcao da frase nao corresponde ao que o desafio pedia.")));
    }

    /** Procura pares sujeito + verbo incompativeis, como "I are" ou "he are". */
    private List<ErroApontado> procurarErrosDeConcordancia(String respostaNormalizada) {
        List<ErroApontado> erros = new ArrayList<>();
        String[] palavras = respostaNormalizada.split(" ");

        for (int posicao = 0; posicao < palavras.length - 1; posicao++) {
            String sujeito = palavras[posicao];
            String possivelVerbo = palavras[posicao + 1];
            String formaCorreta = FORMA_CORRETA_POR_SUJEITO.get(sujeito);

            if (formaCorreta == null || !FORMAS_DO_VERBO_TO_BE.contains(possivelVerbo)) {
                continue;
            }
            if (!formaCorreta.equals(possivelVerbo)) {
                erros.add(new ErroApontado(
                        "concordancia_do_verbo_to_be",
                        sujeito + " " + possivelVerbo,
                        sujeito + " " + formaCorreta,
                        "Com o sujeito \"" + sujeito + "\" o verbo \"to be\" e \"" + formaCorreta
                                + "\", nao \"" + possivelVerbo + "\"."));
            }
        }
        return erros;
    }

    /** Proporcao de palavras da referencia que aparecem na resposta. */
    private double calcularSemelhanca(String resposta, String referencia) {
        Set<String> palavrasDaResposta = new LinkedHashSet<>(Arrays.asList(resposta.split(" ")));
        String[] palavrasDaReferencia = referencia.split(" ");
        if (palavrasDaReferencia.length == 0) {
            return 0;
        }
        long encontradas = Arrays.stream(palavrasDaReferencia).filter(palavrasDaResposta::contains).count();
        return (double) encontradas / palavrasDaReferencia.length;
    }

    /** Minusculas, sem pontuacao e sem espacos duplicados, para comparar sem falso negativo. */
    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9' ]", " ")
                .replaceAll("\s+", " ")
                .trim();
    }
}
