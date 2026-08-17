package br.com.agenteingles.agente;

import br.com.agenteingles.modulo.NivelCefr;

/** Tudo que o avaliador precisa para julgar uma resposta e apontar o erro especifico. */
public record PedidoDeAvaliacao(
        String codigoDoModulo,
        String nomeDoModulo,
        String descricaoDoModulo,
        NivelCefr nivel,
        String enunciado,
        String contextoDaCena,
        String respostaDeReferencia,
        String criterioDeAvaliacao,
        String respostaDoUsuario) {
}
