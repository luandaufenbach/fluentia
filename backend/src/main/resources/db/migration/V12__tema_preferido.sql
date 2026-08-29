-- Tema preferido do aluno, escolhido nos Ajustes.
--
-- Ate aqui a unica influencia sobre a cena era o objetivo, que so mapeia tres dos nove
-- temas: viagem, trabalho e conversacao livre. Os outros seis — casa e rotina, comida,
-- compras, cultura, saude e vida social — apareciam apenas quando o orquestrador
-- trocava de cena por repeticao. Existiam no conteudo e eram inalcancaveis por escolha.
--
-- NULL continua sendo o normal e significa "sem preferencia, use o objetivo": ninguem
-- precisa escolher nada para o app seguir funcionando como antes.
ALTER TABLE usuario ADD COLUMN tema_preferido_id BIGINT;

-- ON DELETE SET NULL porque este caso ja aconteceu neste projeto: a V10 apagou o tema
-- "Ingles para dev". Uma preferencia apontando para um tema removido precisa degradar
-- sozinha para "sem preferencia", e nao impedir a remocao nem apontar para o vazio.
ALTER TABLE usuario
    ADD CONSTRAINT usuario_tema_preferido_fkey
    FOREIGN KEY (tema_preferido_id) REFERENCES tema (id) ON DELETE SET NULL;

COMMENT ON COLUMN usuario.tema_preferido_id IS
    'Tema que o aluno prefere nas cenas. NULL = decidir pelo objetivo.';
