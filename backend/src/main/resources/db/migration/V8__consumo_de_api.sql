-- Consumo da API por chamada.
--
-- O gasto so era visivel no painel da Anthropic, fora do produto. Cada chamada
-- passa a gravar tokens, modelo e custo, ligados a conta que a provocou.
--
-- custo_usd aceita nulo de proposito: se o modelo nao tem preco configurado, os
-- tokens continuam registrados e o custo fica explicitamente desconhecido, em vez
-- de virar zero e mentir no total.

CREATE TABLE consumo_de_api (
    id                 BIGSERIAL PRIMARY KEY,
    usuario_id         BIGINT REFERENCES usuario (id) ON DELETE SET NULL,
    tipo_de_chamada    VARCHAR(40)   NOT NULL,
    modelo             VARCHAR(60)   NOT NULL,
    tokens_de_entrada  INTEGER       NOT NULL,
    tokens_de_saida    INTEGER       NOT NULL,
    itens_produzidos   INTEGER       NOT NULL DEFAULT 1,
    custo_usd          NUMERIC(12,6),
    ocorrido_em        TIMESTAMP     NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN consumo_de_api.usuario_id IS 'Nulo para rotina sem dono, como a geracao de conteudo.';
COMMENT ON COLUMN consumo_de_api.itens_produzidos IS 'Quantos desafios o lote rendeu, para o custo por desafio.';
COMMENT ON COLUMN consumo_de_api.custo_usd IS 'Nulo quando o modelo nao tem preco configurado.';

CREATE INDEX idx_consumo_por_usuario ON consumo_de_api (usuario_id, ocorrido_em DESC);
