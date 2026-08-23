-- Temas das cenas: sai o recorte profissional, entra o dia a dia.
--
-- "Ingles para dev" era um nicho dentro de um app que ensina a lingua. Quem esta
-- aprendendo ingles precisa pedir comida, marcar consulta e conversar com amigo
-- muito antes de escrever code review — e quem ja escreve code review nao esta no
-- publico deste produto.
--
-- Os desafios que ja usaram esse tema NAO sao apagados: eles fazem parte do
-- historico que alimenta a nota. Passam para conversacao livre, que e a cena mais
-- generica, e a nota de quem praticou continua valendo.

UPDATE desafio
SET tema_id = (SELECT id FROM tema WHERE codigo = 'conversacao_livre')
WHERE tema_id = (SELECT id FROM tema WHERE codigo = 'ingles_para_dev');

-- Objetivo DEV deixa de existir: quem o tinha escolhido cai no objetivo mais amplo.
UPDATE usuario SET objetivo = 'CONVERSACAO_GERAL' WHERE objetivo = 'DEV';

DELETE FROM tema WHERE codigo = 'ingles_para_dev';

-- Cinco cenas novas, todas de vida real. Mais tema significa mais variacao de cena
-- para o mesmo conceito: o desafio muda de roupagem sem mudar o que esta medindo.
INSERT INTO tema (codigo, nome, descricao) VALUES
    ('comida_e_restaurante', 'Comida e restaurante',
     'Pedir, recomendar, reclamar e falar do que gosta.'),
    ('compras_e_servicos', 'Compras e serviços',
     'Loja, banco, farmácia e atendimento em geral.'),
    ('saude_e_bem_estar', 'Saúde e bem-estar',
     'Consulta, sintoma, exercício e rotina de cuidado.'),
    ('vida_social', 'Vida social',
     'Combinar, convidar, contar novidade e conversar com amigos.'),
    ('casa_e_rotina', 'Casa e rotina',
     'Moradia, tarefas, vizinhança e o dia a dia doméstico.');
