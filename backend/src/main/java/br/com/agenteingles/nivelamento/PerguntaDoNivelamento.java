package br.com.agenteingles.nivelamento;

import br.com.agenteingles.modulo.NivelCefr;
import java.util.List;

/**
 * A escada de perguntas do nivelamento.
 *
 * <p>Sao perguntas abertas, em ordem crescente de exigencia — nao prova de multipla
 * escolha. Multipla escolha mede reconhecimento; aqui o que interessa e o que a pessoa
 * consegue produzir, que e o que o app vai cobrar depois.
 *
 * <p>A escada e fixa e o aluno pode pular. Pular nao e falta de dado: e exatamente o
 * sinal de onde esta o teto, e por isso a pergunta pulada continua gravada.
 *
 * @param nivelAlvo o nivel que a pergunta tenta alcancar
 * @param pergunta enunciado em ingles — ler a pergunta ja faz parte da medida
 * @param apoio uma linha em portugues, para quem trava antes de comecar nao desistir
 */
public record PerguntaDoNivelamento(NivelCefr nivelAlvo, String pergunta, String apoio) {

    public static final List<PerguntaDoNivelamento> ESCADA = List.of(
            new PerguntaDoNivelamento(
                    NivelCefr.A1,
                    "Tell me about yourself: your name, where you are from, and what you do.",
                    "Fale de você: nome, de onde é e o que faz. Duas ou três frases bastam."),
            new PerguntaDoNivelamento(
                    NivelCefr.A2,
                    "What did you do last weekend? Give a few details.",
                    "Conte o que você fez no fim de semana passado. Repare que é no passado."),
            new PerguntaDoNivelamento(
                    NivelCefr.B1,
                    "What would you change about your work or your studies, and why?",
                    "O que você mudaria no seu trabalho ou nos seus estudos, e por quê?"),
            new PerguntaDoNivelamento(
                    NivelCefr.B2,
                    "Some people say adults cannot learn a language well. Do you agree? Explain your reasoning.",
                    "Você concorda que adulto não aprende bem um idioma? Defenda o seu ponto."),
            new PerguntaDoNivelamento(
                    NivelCefr.C1,
                    "Describe a time you changed your mind about something important. What convinced you?",
                    "Conte uma vez em que você mudou de opinião sobre algo importante."));

    public static int quantidade() {
        return ESCADA.size();
    }

    public static PerguntaDoNivelamento daOrdem(int ordem) {
        return ESCADA.get(ordem - 1);
    }
}
