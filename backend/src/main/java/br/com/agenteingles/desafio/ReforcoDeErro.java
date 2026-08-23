package br.com.agenteingles.desafio;

import java.util.List;
import java.util.Locale;

/**
 * O aviso que aparece quando o mesmo erro insiste.
 *
 * <p>A correcao resolve a resposta da vez; ela nao resolve o padrao. Errar a mesma coisa
 * pela terceira vez nao e distracao, e um conceito que nao entrou — e ai o que ajuda nao
 * e corrigir de novo, e mostrar que ha um padrao e onde ele e ensinado.
 *
 * @param vezes quantas vezes este erro ja aconteceu, contando a de agora
 * @param anteriores as vezes passadas, com o trecho errado e a correcao de cada uma
 * @param moduloDoConceito modulo onde este erro mais aparece — para onde mandar o aluno
 */
public record ReforcoDeErro(String tipo,
                            String rotulo,
                            long vezes,
                            List<OcorrenciaAnterior> anteriores,
                            String moduloDoConceito,
                            String moduloDoConceitoNome) {

    /** @param trechoErrado o que o aluno escreveu naquela vez */
    public record OcorrenciaAnterior(String trechoErrado, String correcao) {
    }

    /**
     * Transforma {@code concordancia_do_verbo_to_be} em "Concordancia do verbo to be".
     *
     * <p>O tipo vem em snake_case porque e chave: serve para contar e agrupar. Mostrar a
     * chave crua na tela entregaria ao aluno um detalhe interno que nao diz nada a ele.
     */
    static String rotuloDe(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "Erro recorrente";
        }
        String comEspacos = tipo.replace('_', ' ').trim();
        return comEspacos.substring(0, 1).toUpperCase(Locale.ROOT) + comEspacos.substring(1);
    }
}
