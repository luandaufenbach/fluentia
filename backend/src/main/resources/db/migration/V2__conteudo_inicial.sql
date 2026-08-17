-- Conteudo inicial: usuario de desenvolvimento, mapa de conceitos por nivel CEFR e temas.

INSERT INTO usuario (nome, email, objetivo, minutos_por_dia, tipo_de_correcao, nivel_estimado)
VALUES ('Usuario de Desenvolvimento', 'dev@agenteingles.local', 'CONVERSACAO_GERAL', 15, 'DETALHADA', 'A1');

INSERT INTO modulo (codigo, nome, nivel_cefr, descricao, ordem) VALUES
    ('verbo_to_be',            'Verbo "to be"',                'A1', 'Formas am/is/are no presente, afirmativa, negativa e interrogativa.',   10),
    ('artigos',                'Artigos a/an/the',             'A1', 'Uso de artigo indefinido e definido.',                                  20),
    ('pronomes_pessoais',      'Pronomes pessoais',            'A1', 'Pronomes sujeito e objeto.',                                            30),
    ('presente_simples',       'Presente simples',             'A1', 'Rotinas e fatos, com o -s da terceira pessoa.',                          40),
    ('passado_simples',        'Passado simples',              'A2', 'Verbos regulares e irregulares no passado.',                             50),
    ('comparativos',           'Comparativos e superlativos',  'A2', 'Formas -er/-est e more/most.',                                           60),
    ('there_is_are',           '"There is" e "there are"',     'A2', 'Existencia no singular e no plural.',                                    70),
    ('presente_perfeito',      'Presente perfeito',            'B1', 'Have/has + participio, com for, since, already e yet.',                  80),
    ('condicionais_basicos',   'Condicionais basicos',         'B1', 'Primeiro e segundo condicional.',                                        90),
    ('phrasal_verbs_comuns',   'Phrasal verbs comuns',         'B1', 'Phrasal verbs de alta frequencia no dia a dia.',                        100),
    ('passado_perfeito',       'Passado perfeito',             'B2', 'Had + participio para anterioridade no passado.',                       110),
    ('voz_passiva',            'Voz passiva',                  'B2', 'Construcao passiva nos principais tempos verbais.',                     120),
    ('condicionais_mistos',    'Condicionais mistos',          'B2', 'Combinacao de tempos entre condicao e consequencia.',                   130),
    ('subjuntivo',             'Subjuntivo',                   'C1', 'Formas subjuntivas em pedidos, sugestoes e situacoes hipoteticas.',     140),
    ('inversao',               'Inversao',                     'C1', 'Inversao para enfase apos adverbios negativos.',                        150),
    ('expressoes_idiomaticas', 'Expressoes idiomaticas',       'C2', 'Expressoes idiomaticas avancadas e registro natural.',                  160);

-- Pre-requisitos: preservam a progressao por nivel sem travar numa ordem 100% fixa.
INSERT INTO pre_requisito_modulo (modulo_id, pre_requisito_id)
SELECT m.id, p.id
FROM (VALUES
    ('artigos',                'verbo_to_be'),
    ('pronomes_pessoais',      'verbo_to_be'),
    ('presente_simples',       'verbo_to_be'),
    ('passado_simples',        'presente_simples'),
    ('comparativos',           'artigos'),
    ('there_is_are',           'artigos'),
    ('presente_perfeito',      'passado_simples'),
    ('condicionais_basicos',   'presente_simples'),
    ('phrasal_verbs_comuns',   'presente_simples'),
    ('passado_perfeito',       'presente_perfeito'),
    ('voz_passiva',            'passado_simples'),
    ('condicionais_mistos',    'condicionais_basicos'),
    ('subjuntivo',             'condicionais_mistos'),
    ('inversao',               'voz_passiva'),
    ('expressoes_idiomaticas', 'phrasal_verbs_comuns')
) AS dependencia (codigo_do_modulo, codigo_do_pre_requisito)
JOIN modulo m ON m.codigo = dependencia.codigo_do_modulo
JOIN modulo p ON p.codigo = dependencia.codigo_do_pre_requisito;

INSERT INTO tema (codigo, nome, descricao) VALUES
    ('conversacao_livre',      'Conversacao livre',       'Conversa cotidiana sem cenario definido.'),
    ('viagem',                 'Viagem',                  'Aeroporto, hotel, restaurante e deslocamento.'),
    ('trabalho',               'Trabalho',                'Reuniao, e-mail, entrevista e rotina profissional.'),
    ('cultura_e_expressoes',   'Cultura e expressoes',    'Costumes, midia e expressoes do uso real.'),
    ('ingles_para_dev',        'Ingles para dev',         'Code review, documentacao, stand-up e incidentes.');
