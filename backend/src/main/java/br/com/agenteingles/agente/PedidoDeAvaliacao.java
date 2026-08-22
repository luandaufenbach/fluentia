package br.com.agenteingles.agente;

import br.com.agenteingles.modulo.NivelCefr;
import br.com.agenteingles.usuario.TipoDeCorrecao;

/**
 * Tudo que o avaliador precisa para julgar uma resposta e apontar o erro especifico.
 *
 * @param tipoDeCorrecao preferencia do aluno. Alem de respeitar a escolha dele, e o
 *                       maior controle de custo do avaliador: a saida e onde o token
 *                       custa caro, e correcao resumida devolve bem menos texto
 */
public record PedidoDeAvaliacao(
        String codigoDoModulo,
        String nomeDoModulo,
        String descricaoDoModulo,
        NivelCefr nivel,
        String enunciado,
        String contextoDaCena,
        String respostaDeReferencia,
        String criterioDeAvaliacao,
        String respostaDoUsuario,
        TipoDeCorrecao tipoDeCorrecao) {
}
