package br.com.agenteingles.agente;

import java.util.List;

/**
 * A conversa inteira do nivelamento, para o agente estimar o nivel de uma vez so.
 *
 * <p>Uma chamada, e nao uma por pergunta: julgar cada resposta isolada custaria cinco
 * chamadas e ainda daria um veredito pior, porque o nivel aparece no conjunto — quem
 * acerta a pergunta de A1 e trava na de B1 esta dizendo mais do que qualquer resposta
 * isolada diria.
 *
 * @param usuarioId conta que provocou a chamada, para o consumo ser atribuido a ela
 * @param turnos pergunta e resposta na ordem em que foram apresentadas
 */
public record PedidoDeNivelamento(Long usuarioId, List<TurnoDoNivelamento> turnos) {

    /**
     * @param resposta nulo quando o aluno pulou — o agente precisa saber que pulou, e nao
     *                 receber a pergunta como se ela nunca tivesse sido feita
     */
    public record TurnoDoNivelamento(String nivelAlvo, String pergunta, String resposta) {
    }
}
