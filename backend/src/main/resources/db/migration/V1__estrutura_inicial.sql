-- Estrutura inicial do curriculo adaptativo.
-- Dois eixos independentes: modulo (o conceito avaliado) e tema (a cena do desafio).

CREATE TABLE usuario (
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(120)  NOT NULL,
    email             VARCHAR(180)  NOT NULL UNIQUE,
    objetivo          VARCHAR(30)   NOT NULL DEFAULT 'CONVERSACAO_GERAL',
    minutos_por_dia   INTEGER       NOT NULL DEFAULT 15,
    tipo_de_correcao  VARCHAR(20)   NOT NULL DEFAULT 'DETALHADA',
    nivel_estimado    VARCHAR(2),
    criado_em         TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Conceito: a unidade que recebe nota. Organizado por nivel CEFR.
CREATE TABLE modulo (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(60)  NOT NULL UNIQUE,
    nome        VARCHAR(120) NOT NULL,
    nivel_cefr  VARCHAR(2)   NOT NULL,
    descricao   TEXT         NOT NULL,
    ordem       INTEGER      NOT NULL
);

-- Um modulo so e liberado quando seus pre-requisitos tem nota razoavel.
CREATE TABLE pre_requisito_modulo (
    modulo_id         BIGINT NOT NULL REFERENCES modulo (id) ON DELETE CASCADE,
    pre_requisito_id  BIGINT NOT NULL REFERENCES modulo (id) ON DELETE CASCADE,
    PRIMARY KEY (modulo_id, pre_requisito_id),
    CONSTRAINT modulo_nao_e_pre_requisito_de_si_mesmo CHECK (modulo_id <> pre_requisito_id)
);

-- Tema: a roupagem/cena do desafio. Nao recebe nota.
CREATE TABLE tema (
    id         BIGSERIAL PRIMARY KEY,
    codigo     VARCHAR(60)  NOT NULL UNIQUE,
    nome       VARCHAR(120) NOT NULL,
    descricao  TEXT         NOT NULL
);

-- Nota de dominio do usuario naquele modulo. E o dado central do produto.
CREATE TABLE nota_do_modulo (
    id                       BIGSERIAL PRIMARY KEY,
    usuario_id               BIGINT       NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    modulo_id                BIGINT       NOT NULL REFERENCES modulo (id) ON DELETE CASCADE,
    nota                     NUMERIC(4,2) NOT NULL,
    quantidade_de_praticas   INTEGER      NOT NULL DEFAULT 0,
    data_da_ultima_pratica   TIMESTAMP,
    atualizado_em            TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT nota_unica_por_usuario_e_modulo UNIQUE (usuario_id, modulo_id),
    CONSTRAINT nota_dentro_da_faixa CHECK (nota >= 0 AND nota <= 10)
);

-- Historico de desafios: alimenta o calculo da nota e a decisao do orquestrador.
CREATE TABLE desafio (
    id                       BIGSERIAL PRIMARY KEY,
    usuario_id               BIGINT       NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    modulo_id                BIGINT       NOT NULL REFERENCES modulo (id),
    tema_id                  BIGINT       NOT NULL REFERENCES tema (id),
    formato                  VARCHAR(20)  NOT NULL DEFAULT 'TEXTO',
    status                   VARCHAR(30)  NOT NULL DEFAULT 'AGUARDANDO_RESPOSTA',
    enunciado                TEXT         NOT NULL,
    contexto_da_cena         TEXT,
    resposta_de_referencia   TEXT,
    criterio_de_avaliacao    TEXT,
    motivo_da_escolha        TEXT         NOT NULL,
    criado_em                TIMESTAMP    NOT NULL DEFAULT NOW(),
    respondido_em            TIMESTAMP
);

CREATE INDEX idx_desafio_usuario_modulo ON desafio (usuario_id, modulo_id, criado_em DESC);

CREATE TABLE avaliacao_do_desafio (
    id                   BIGSERIAL PRIMARY KEY,
    desafio_id           BIGINT       NOT NULL UNIQUE REFERENCES desafio (id) ON DELETE CASCADE,
    resposta_do_usuario  TEXT         NOT NULL,
    nota_obtida          NUMERIC(4,2) NOT NULL,
    feedback             TEXT         NOT NULL,
    avaliado_em          TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT nota_obtida_dentro_da_faixa CHECK (nota_obtida >= 0 AND nota_obtida <= 10)
);

-- Erro especifico detectado pelo avaliador. E o que permite o reforco dirigido.
CREATE TABLE erro_detectado (
    id             BIGSERIAL PRIMARY KEY,
    avaliacao_id   BIGINT       NOT NULL REFERENCES avaliacao_do_desafio (id) ON DELETE CASCADE,
    tipo           VARCHAR(60)  NOT NULL,
    trecho_errado  TEXT,
    correcao       TEXT,
    explicacao     TEXT         NOT NULL
);

CREATE INDEX idx_erro_detectado_avaliacao ON erro_detectado (avaliacao_id);
