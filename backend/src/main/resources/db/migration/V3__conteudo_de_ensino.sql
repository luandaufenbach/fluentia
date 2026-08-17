-- Conteudo de ensino do modulo: o app ensina antes de cobrar.
-- Ate aqui o produto so gerava desafios; sem uma explicacao para consultar, errar
-- nao ensinava nada. O conteudo e por modulo, nao por desafio, porque ele e estavel:
-- o desafio muda toda vez, a explicacao do conceito nao.

CREATE TABLE conteudo_do_modulo (
    id            BIGSERIAL PRIMARY KEY,
    modulo_id     BIGINT NOT NULL UNIQUE REFERENCES modulo (id) ON DELETE CASCADE,
    resumo        TEXT   NOT NULL,
    explicacao    TEXT   NOT NULL,
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN conteudo_do_modulo.resumo IS 'Uma frase: o que o aluno vai aprender aqui.';
COMMENT ON COLUMN conteudo_do_modulo.explicacao IS 'Explicacao didatica em portugues, em paragrafos.';

-- Exemplo em tabela propria, e nao num campo de texto, para que a interface possa
-- alinhar ingles e portugues lado a lado e para o gerador de desafio poder consultar
-- o que ja foi ensinado.
CREATE TABLE exemplo_do_conteudo (
    id           BIGSERIAL PRIMARY KEY,
    conteudo_id  BIGINT       NOT NULL REFERENCES conteudo_do_modulo (id) ON DELETE CASCADE,
    ordem        INTEGER      NOT NULL,
    em_ingles    TEXT         NOT NULL,
    em_portugues TEXT         NOT NULL,
    observacao   TEXT
);

CREATE INDEX idx_exemplo_do_conteudo ON exemplo_do_conteudo (conteudo_id, ordem);

-- Erros comuns fecham o ciclo com o avaliador: o aluno le antes o erro que
-- provavelmente vai cometer, no mesmo formato (errado -> certo) da correcao.
CREATE TABLE erro_comum_do_conteudo (
    id          BIGSERIAL PRIMARY KEY,
    conteudo_id BIGINT  NOT NULL REFERENCES conteudo_do_modulo (id) ON DELETE CASCADE,
    ordem       INTEGER NOT NULL,
    errado      TEXT    NOT NULL,
    certo       TEXT    NOT NULL,
    explicacao  TEXT    NOT NULL
);

CREATE INDEX idx_erro_comum_do_conteudo ON erro_comum_do_conteudo (conteudo_id, ordem);
