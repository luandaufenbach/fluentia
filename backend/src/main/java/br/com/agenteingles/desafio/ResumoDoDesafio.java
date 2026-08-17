package br.com.agenteingles.desafio;

import java.time.LocalDateTime;

/**
 * Desafio ja resolvido, sem entidade JPA atravessando a fronteira da transacao.
 *
 * <p>O servico e o limite transacional: devolver a entidade faria o controller ler
 * associacoes lazy com a sessao ja fechada.
 *
 * @param respostaDeReferencia apoio interno da avaliacao — o controller nao expoe
 *                             este campo ao cliente, senao entregaria o gabarito
 */
public record ResumoDoDesafio(
        Long id,
        String enunciado,
        String contextoDaCena,
        FormatoDoDesafio formato,
        StatusDoDesafio status,
        String moduloCodigo,
        String moduloNome,
        String temaNome,
        String motivoDaEscolha,
        String respostaDeReferencia,
        LocalDateTime criadoEm) {

    static ResumoDoDesafio de(Desafio desafio) {
        return new ResumoDoDesafio(
                desafio.getId(),
                desafio.getEnunciado(),
                desafio.getContextoDaCena(),
                desafio.getFormato(),
                desafio.getStatus(),
                desafio.getModulo().getCodigo(),
                desafio.getModulo().getNome(),
                desafio.getTema().getNome(),
                desafio.getMotivoDaEscolha(),
                desafio.getRespostaDeReferencia(),
                desafio.getCriadoEm());
    }
}
