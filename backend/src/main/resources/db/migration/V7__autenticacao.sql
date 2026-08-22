-- Autenticacao e trilha de auditoria.
--
-- Ate aqui todo endpoint respondia para qualquer um que alcancasse a porta, e o
-- usuario era fixo. Esta migration fecha isso.
--
-- Decisoes que valem registro:
--
-- 1. senha_hash e VARCHAR(100) e guarda o hash COM o prefixo do algoritmo
--    ({bcrypt}$2a$12$...). O prefixo permite trocar de algoritmo depois sem
--    invalidar a senha de quem ja se cadastrou: o verificador le o prefixo e usa
--    o algoritmo certo para cada hash.
--
-- 2. A senha nunca aparece em texto puro em lugar nenhum — nem aqui, nem em log,
--    nem em resposta de API.
--
-- 3. O usuario de desenvolvimento semeado na V2 fica SEM senha e INATIVO. Conta
--    semeada com senha conhecida e porta dos fundos: quem clonar o repositorio
--    teria a credencial. Quem quiser usar o app se cadastra pela tela.

ALTER TABLE usuario
    ADD COLUMN senha_hash        VARCHAR(100),
    ADD COLUMN papel             VARCHAR(20)  NOT NULL DEFAULT 'ALUNO',
    ADD COLUMN ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    ADD COLUMN tentativas_falhas INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_ate     TIMESTAMP,
    ADD COLUMN ultimo_acesso_em  TIMESTAMP;

COMMENT ON COLUMN usuario.senha_hash IS 'Hash com prefixo do algoritmo. Nunca a senha.';
COMMENT ON COLUMN usuario.bloqueado_ate IS 'Bloqueio temporario por tentativas falhas seguidas.';

-- E-mail e a credencial de login: a comparacao precisa ser insensivel a caixa,
-- senao Joao@x.com e joao@x.com viram contas diferentes e a unicidade nao protege.
CREATE UNIQUE INDEX idx_usuario_email_normalizado ON usuario (LOWER(email));

UPDATE usuario SET ativo = FALSE, senha_hash = NULL WHERE email = 'dev@agenteingles.local';

-- Trilha de auditoria de autenticacao.
--
-- Guarda o que aconteceu, nunca o segredo: e-mail tentado, origem, resultado. Sem
-- isto nao ha como investigar um acesso indevido nem detectar ataque em andamento.
-- O e-mail fica em coluna propria porque a tentativa pode ser de conta inexistente,
-- e ai nao ha usuario_id para referenciar.
CREATE TABLE evento_de_autenticacao (
    id           BIGSERIAL PRIMARY KEY,
    usuario_id   BIGINT REFERENCES usuario (id) ON DELETE SET NULL,
    email        VARCHAR(180) NOT NULL,
    tipo         VARCHAR(40)  NOT NULL,
    origem       VARCHAR(45),
    detalhe      VARCHAR(200),
    ocorrido_em  TIMESTAMP    NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN evento_de_autenticacao.origem IS 'Endereco de origem. 45 caracteres cobrem IPv6.';
COMMENT ON COLUMN evento_de_autenticacao.detalhe IS 'Motivo tecnico. Nunca credencial.';

CREATE INDEX idx_evento_por_email ON evento_de_autenticacao (LOWER(email), ocorrido_em DESC);
CREATE INDEX idx_evento_por_data ON evento_de_autenticacao (ocorrido_em DESC);
