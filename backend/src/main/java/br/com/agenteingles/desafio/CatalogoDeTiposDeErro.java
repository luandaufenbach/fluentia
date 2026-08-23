package br.com.agenteingles.desafio;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * O vocabulario fechado de tipos de erro.
 *
 * <p>Existe porque contar depende de o nome bater. O tipo era texto livre saido do
 * modelo, e "concordancia_do_verbo_to_be" numa resposta e "concordancia_verbal" na
 * seguinte sao, para qualquer contagem, dois erros diferentes — o aviso de repeticao
 * nunca chegaria a tres, e o mesmo vale para o reforco dirigido do gerador.
 *
 * <p>A lista vai no pedido ao avaliador. O que volta ainda passa por
 * {@link #normalizar(String)}, porque instrucao nao e garantia: acento, maiuscula e
 * espaco no lugar de sublinhado aparecem de vez em quando.
 *
 * <p>Tipo fora do catalogo <b>nao</b> e descartado nem trocado por "outro". Ele fica
 * como veio e vira aviso no log: perder o que o avaliador viu seria pior do que ter uma
 * chave que agrega mal, e o log mostra quando a lista precisa crescer.
 */
public final class CatalogoDeTiposDeErro {

    private static final Logger log = LoggerFactory.getLogger(CatalogoDeTiposDeErro.class);

    /** Cobre os 16 modulos mais os deslizes que aparecem em qualquer um deles. */
    public static final List<String> TIPOS = List.of(
            "concordancia_do_verbo_to_be",
            "terceira_pessoa_do_singular",
            "uso_de_artigo",
            "pronome_errado",
            "passado_simples",
            "verbo_irregular",
            "comparativo_ou_superlativo",
            "there_is_there_are",
            "presente_perfeito",
            "condicional",
            "phrasal_verb",
            "passado_perfeito",
            "voz_passiva",
            "subjuntivo",
            "inversao",
            "expressao_idiomatica",
            "ordem_das_palavras",
            "preposicao",
            "quantificador",
            "tempo_verbal_trocado",
            "vocabulario",
            "ortografia",
            "resposta_fora_do_pedido",
            "resposta_em_branco",
            "outro");

    private static final Set<String> CONHECIDOS = Set.copyOf(TIPOS);

    private CatalogoDeTiposDeErro() {
    }

    /** A lista formatada para entrar no pedido enviado ao avaliador. */
    public static String paraOPrompt() {
        return String.join(", ", TIPOS);
    }

    /**
     * Deixa o tipo na forma que o catalogo usa: minusculas, sem acento e com sublinhado.
     *
     * @return o tipo normalizado, ou {@code "outro"} se vier vazio
     */
    public static String normalizar(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "outro";
        }

        String semAcento = Normalizer.normalize(tipo.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String normalizado = semAcento.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalizado.isEmpty()) {
            return "outro";
        }
        if (!CONHECIDOS.contains(normalizado)) {
            // Nao vira "outro": o tipo especifico ainda diz mais do que um balde generico.
            // O aviso existe para a lista poder crescer com base no que aparece de verdade.
            log.warn("Tipo de erro fora do catalogo: {}", normalizado);
        }
        return normalizado;
    }
}
