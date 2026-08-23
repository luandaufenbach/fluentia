package br.com.agenteingles.sessao;

import java.util.List;

/**
 * O dia do aluno: quanto ele se propos a fazer, quanto ja fez e o que esta caindo.
 *
 * @param meta desafios de hoje, derivados do ritmo escolhido em Configuracoes
 * @param concluidos desafios ja respondidos hoje
 * @param metaAlcancada quando a sessao do dia tem um fim, e nao um fluxo infinito
 * @param revisoes conceitos que o tempo esta derrubando, do que mais caiu para o que menos caiu
 */
public record ResumoDoDia(int meta,
                          long concluidos,
                          boolean metaAlcancada,
                          SequenciaDeDias sequencia,
                          List<RevisaoPendente> revisoes) {

    /** Quantos faltam para fechar o dia. Nunca negativo: passar da meta nao e divida. */
    public long restantes() {
        return Math.max(0, meta - concluidos);
    }
}
