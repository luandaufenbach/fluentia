-- Nivelamento: a conversa curta que estima o nivel de quem chega.
--
-- Sem isto todo mundo comecava em A1, e quem ja sabe ingles abandona na primeira
-- tela ao ser mandado traduzir "eu sou brasileiro".
--
-- As perguntas ficam gravadas junto com as respostas porque a escada de perguntas
-- vai mudar com o tempo: sem guardar o que foi perguntado, um nivelamento antigo
-- deixaria de poder ser reavaliado.

CREATE TABLE nivelamento (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL,
    nivel_estimado  VARCHAR(2),
    resumo          TEXT,
    iniciado_em     TIMESTAMP   NOT NULL DEFAULT NOW(),
    concluido_em    TIMESTAMP
);

-- Um nivelamento em andamento por conta: comecar outro pela metade deixaria dois
-- abertos e nenhum dos dois seria concluido.
CREATE UNIQUE INDEX idx_nivelamento_em_andamento
    ON nivelamento (usuario_id)
    WHERE status = 'EM_ANDAMENTO';

CREATE TABLE resposta_do_nivelamento (
    id               BIGSERIAL PRIMARY KEY,
    nivelamento_id   BIGINT      NOT NULL REFERENCES nivelamento (id) ON DELETE CASCADE,
    ordem            INTEGER     NOT NULL,
    nivel_alvo       VARCHAR(2)  NOT NULL,
    pergunta         TEXT        NOT NULL,
    resposta         TEXT,
    respondido_em    TIMESTAMP,
    CONSTRAINT ordem_unica_no_nivelamento UNIQUE (nivelamento_id, ordem)
);

COMMENT ON COLUMN resposta_do_nivelamento.resposta IS 'Nulo quando o aluno pulou. Pular e sinal de teto, nao falta de dado.';

CREATE INDEX idx_resposta_do_nivelamento ON resposta_do_nivelamento (nivelamento_id, ordem);
