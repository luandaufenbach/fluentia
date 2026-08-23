package br.com.agenteingles.nivelamento;

/**
 * O que a tela mostra em cada passo.
 *
 * @param perguntaAtual nulo quando a conversa terminou
 * @param resultado nulo enquanto a conversa nao terminou
 */
public record EtapaDoNivelamento(Long id,
                                 int ordem,
                                 int total,
                                 PerguntaDoNivelamento perguntaAtual,
                                 ResultadoParaOAluno resultado) {

    /**
     * @param modulosLiberados quantos conceitos a estimativa ja abriu na trilha
     */
    public record ResultadoParaOAluno(String nivel,
                                      String resumo,
                                      String pontoForte,
                                      String pontoAFortalecer,
                                      int modulosLiberados,
                                      String primeiroModulo) {
    }

    static EtapaDoNivelamento pergunta(Nivelamento nivelamento, RespostaDoNivelamento proxima) {
        return new EtapaDoNivelamento(
                nivelamento.getId(),
                proxima.getOrdem(),
                PerguntaDoNivelamento.quantidade(),
                PerguntaDoNivelamento.daOrdem(proxima.getOrdem()),
                null);
    }

    static EtapaDoNivelamento concluido(Nivelamento nivelamento, ResultadoParaOAluno resultado) {
        return new EtapaDoNivelamento(
                nivelamento.getId(),
                PerguntaDoNivelamento.quantidade(),
                PerguntaDoNivelamento.quantidade(),
                null,
                resultado);
    }
}
