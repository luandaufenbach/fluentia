package br.com.agenteingles.orquestrador;

import br.com.agenteingles.modulo.SituacaoDoModulo;
import br.com.agenteingles.tema.Tema;

/**
 * O que praticar agora e por que.
 *
 * @param motivo explicacao em portugues da decisao, gravada junto do desafio para
 *               que a escolha do agente seja auditavel depois
 */
public record DecisaoDoOrquestrador(
        SituacaoDoModulo situacaoDoModulo,
        Tema tema,
        String motivo) {
}
