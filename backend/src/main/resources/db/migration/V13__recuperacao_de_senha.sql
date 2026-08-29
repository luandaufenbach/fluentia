-- Pedidos de recuperacao de senha.
--
-- Guarda o HASH do token, nunca o token. O token so existe em dois lugares: no e-mail
-- que a pessoa recebeu e na memoria durante a requisicao que o gerou. Se este banco
-- vazar, quem levar a tabela nao consegue redefinir a senha de ninguem — pelo mesmo
-- motivo que a coluna de senha guarda hash e nao a senha.
--
-- SHA-256 aqui, e nao BCrypt: o token ja e 256 bits de aleatoriedade vinda de
-- SecureRandom, entao nao ha o que adivinhar por forca bruta e o custo alto do BCrypt
-- so atrasaria a validacao sem acrescentar seguranca. BCrypt existe para senha humana,
-- que e curta e previsivel.
CREATE TABLE recuperacao_de_senha (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL,
    criado_em   TIMESTAMP   NOT NULL DEFAULT NOW(),
    expira_em   TIMESTAMP   NOT NULL,
    usado_em    TIMESTAMP,
    origem      VARCHAR(45)
);

-- A busca da redefinicao e sempre por hash: e o unico caminho de leitura que existe.
CREATE UNIQUE INDEX idx_recuperacao_por_token ON recuperacao_de_senha (token_hash);

-- Sustenta duas leituras: invalidar os pedidos anteriores de uma conta quando um novo
-- chega, e contar pedidos recentes para o limite por origem.
CREATE INDEX idx_recuperacao_por_usuario ON recuperacao_de_senha (usuario_id, criado_em DESC);

COMMENT ON COLUMN recuperacao_de_senha.token_hash IS
    'SHA-256 do token em hexadecimal. O token em claro nunca e gravado.';
COMMENT ON COLUMN recuperacao_de_senha.usado_em IS
    'Marcado na redefinicao. Link usado nao vale de novo, mesmo dentro do prazo.';
