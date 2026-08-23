package br.com.agenteingles.agente;

/**
 * O veredito do nivelamento.
 *
 * @param nivelCefr nivel estimado, de A1 a C2
 * @param resumo em portugues, dirigido ao aluno: o que ele ja sustenta e o que trava
 * @param pontoForte o que apareceu de mais solido, para a mensagem nao ser so o que falta
 * @param pontoAFortalecer o que mais atrapalhou, ja no formato de um conceito da trilha
 */
public record ResultadoDoNivelamento(String nivelCefr,
                                     String resumo,
                                     String pontoForte,
                                     String pontoAFortalecer) {
}
