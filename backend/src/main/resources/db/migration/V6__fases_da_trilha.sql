-- Fases da trilha: o agrupamento que o aluno enxerga.
--
-- O nivel CEFR continua sendo a verdade tecnica do conteudo, mas "A2" nao diz nada
-- para quem esta comecando. A fase traduz o nivel em uma promessa concreta — o que
-- voce vai conseguir fazer ao terminar — e e isso que sustenta a motivacao entre um
-- modulo e outro.
--
-- Cada fase tem um marco: a frase que descreve a habilidade destravada. O marco e
-- considerado alcancado quando todos os modulos da fase saem do vermelho, o mesmo
-- limite que ja libera o modulo seguinte.

CREATE TABLE fase (
    id         BIGSERIAL PRIMARY KEY,
    codigo     VARCHAR(60)  NOT NULL UNIQUE,
    nome       VARCHAR(120) NOT NULL,
    promessa   TEXT         NOT NULL,
    marco      TEXT         NOT NULL,
    ordem      INTEGER      NOT NULL
);

COMMENT ON COLUMN fase.promessa IS 'O que o aluno vai saber fazer ao terminar a fase.';
COMMENT ON COLUMN fase.marco IS 'A habilidade concreta que marca o fim da fase.';

INSERT INTO fase (codigo, nome, promessa, marco, ordem) VALUES
    ('primeiras_frases', 'Primeiras frases',
     'Montar frases suas sobre quem você é e o que você faz, sem depender de tradutor.',
     'Se apresentar e falar da sua rotina sem travar.', 10),

    ('contar_o_que_aconteceu', 'Contar o que aconteceu',
     'Sair do presente: falar do que já passou, comparar coisas e dizer o que existe ao seu redor.',
     'Contar como foi o seu fim de semana.', 20),

    ('destravar_a_conversa', 'Destravar a conversa',
     'Ligar o passado ao agora, falar de hipóteses e usar os verbos que aparecem em toda conversa real.',
     'Sustentar uma conversa sobre o dia a dia.', 30),

    ('precisao', 'Precisão',
     'Dizer exatamente o que você quis dizer: ordem dos acontecimentos, foco da frase e condição composta.',
     'Participar de uma reunião sem perder o fio.', 40),

    ('naturalidade', 'Naturalidade',
     'Deixar de soar traduzido: registro, ênfase e as expressões que ninguém ensina em aula.',
     'Soar natural, não apenas correto.', 50);

ALTER TABLE modulo ADD COLUMN fase_id BIGINT REFERENCES fase (id);

UPDATE modulo SET fase_id = (SELECT id FROM fase WHERE codigo = 'primeiras_frases')
 WHERE codigo IN ('verbo_to_be', 'artigos', 'pronomes_pessoais', 'presente_simples');

UPDATE modulo SET fase_id = (SELECT id FROM fase WHERE codigo = 'contar_o_que_aconteceu')
 WHERE codigo IN ('passado_simples', 'comparativos', 'there_is_are');

UPDATE modulo SET fase_id = (SELECT id FROM fase WHERE codigo = 'destravar_a_conversa')
 WHERE codigo IN ('presente_perfeito', 'condicionais_basicos', 'phrasal_verbs_comuns');

UPDATE modulo SET fase_id = (SELECT id FROM fase WHERE codigo = 'precisao')
 WHERE codigo IN ('passado_perfeito', 'voz_passiva', 'condicionais_mistos');

UPDATE modulo SET fase_id = (SELECT id FROM fase WHERE codigo = 'naturalidade')
 WHERE codigo IN ('subjuntivo', 'inversao', 'expressoes_idiomaticas');

-- Nenhum modulo pode ficar fora da trilha: sem fase, ele sumiria da tela principal.
ALTER TABLE modulo ALTER COLUMN fase_id SET NOT NULL;
