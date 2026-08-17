-- Acentuacao dos nomes e descricoes exibidos ao usuario.
--
-- O seed inicial foi escrito sem acento, seguindo a convencao do codigo. A convencao
-- vale para identificador e comentario; texto que aparece na tela e conteudo do
-- produto e precisa da ortografia correta. Os codigos continuam sem acento de
-- proposito: eles sao chave, nao texto.

UPDATE modulo SET descricao = 'Existência no singular e no plural.'                              WHERE codigo = 'there_is_are';
UPDATE modulo SET descricao = 'Have/has + particípio, com for, since, already e yet.'            WHERE codigo = 'presente_perfeito';
UPDATE modulo SET nome = 'Condicionais básicos'                                                  WHERE codigo = 'condicionais_basicos';
UPDATE modulo SET descricao = 'Phrasal verbs de alta frequência no dia a dia.'                   WHERE codigo = 'phrasal_verbs_comuns';
UPDATE modulo SET descricao = 'Had + particípio para anterioridade no passado.'                  WHERE codigo = 'passado_perfeito';
UPDATE modulo SET descricao = 'Construção passiva nos principais tempos verbais.'                WHERE codigo = 'voz_passiva';
UPDATE modulo SET descricao = 'Combinação de tempos entre condição e consequência.'              WHERE codigo = 'condicionais_mistos';
UPDATE modulo SET descricao = 'Formas subjuntivas em pedidos, sugestões e situações hipotéticas.' WHERE codigo = 'subjuntivo';
UPDATE modulo SET nome = 'Inversão',
                  descricao = 'Inversão para ênfase após advérbios negativos.'                   WHERE codigo = 'inversao';
UPDATE modulo SET nome = 'Expressões idiomáticas',
                  descricao = 'Expressões idiomáticas avançadas e registro natural.'             WHERE codigo = 'expressoes_idiomaticas';

UPDATE tema SET nome = 'Conversação livre',
                descricao = 'Conversa cotidiana sem cenário definido.'                           WHERE codigo = 'conversacao_livre';
UPDATE tema SET descricao = 'Reunião, e-mail, entrevista e rotina profissional.'                 WHERE codigo = 'trabalho';
UPDATE tema SET nome = 'Cultura e expressões',
                descricao = 'Costumes, mídia e expressões do uso real.'                          WHERE codigo = 'cultura_e_expressoes';
UPDATE tema SET nome = 'Inglês para dev',
                descricao = 'Code review, documentação, stand-up e incidentes.'                  WHERE codigo = 'ingles_para_dev';
