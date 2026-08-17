-- Conteudo de ensino dos modulos: o que o aluno le antes de praticar.
--
-- Gerado pela rotina do perfil "gerar-conteudo" e revisado a mao. Editar
-- este arquivo direto e o caminho esperado para corrigir uma explicacao —
-- ele e a fonte da verdade do material, nao a chamada de API que o produziu.

-- Verbo "to be" (A1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Use am com I, is com he/she/it, e are com you/we/they. Na negativa, acrescente not depois do verbo (is not, are not). Na pergunta, inverta a ordem: verbo antes do sujeito (Are you...?).', 'O verbo ''to be'' significa ''ser'' ou ''estar'' em português, e é um dos verbos mais importantes do inglês. Diferente do português, que tem dois verbos separados (ser e estar), o inglês usa apenas um verbo para as duas ideias.

O verbo ''to be'' muda de forma dependendo de quem pratica a ação, ou seja, dependendo do sujeito da frase. No presente, existem três formas: am, is e are.

Usamos ''am'' apenas com ''I'' (eu). Por exemplo: I am (eu sou, eu estou).

Usamos ''is'' com ''he'' (ele), ''she'' (ela) e ''it'' (isso, coisas e animais), além de nomes próprios e substantivos no singular. Por exemplo: he is, she is, it is, Maria is.

Usamos ''are'' com ''you'' (você ou vocês), ''we'' (nós) e ''they'' (eles ou elas), além de substantivos no plural. Por exemplo: you are, we are, they are.

Na forma afirmativa, a estrutura é: sujeito + am/is/are + complemento. Exemplo: She is happy (Ela está feliz).

Na forma negativa, colocamos ''not'' logo depois do verbo. Exemplo: She is not happy (Ela não está feliz). Também é comum usar as formas contraídas: isn''t, aren''t. A forma ''am not'' não tem contração padrão, mas na conversa se usa ''I''m not''.

Na forma interrogativa, invertemos a ordem: colocamos am/is/are antes do sujeito. Exemplo: Is she happy? (Ela está feliz?).

É muito comum usar contrações no inglês falado e escrito informal: I''m (I am), you''re (you are), he''s (he is), she''s (she is), it''s (it is), we''re (we are), they''re (they are). Essas contrações deixam a fala mais natural e são usadas o tempo todo por falantes nativos.' FROM modulo WHERE codigo = 'verbo_to_be';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I am a student.', 'Eu sou estudante.', 'Usa-se ''am'' porque o sujeito é ''I''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'She is tired.', 'Ela está cansada.', 'Usa-se ''is'' com ''she'', e aqui ''to be'' tem sentido de ''estar'', não ''ser''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'They are from Brazil.', 'Eles são do Brasil.', 'Usa-se ''are'' porque o sujeito ''they'' é plural.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'We are not ready yet.', 'Nós ainda não estamos prontos.', 'Forma negativa: ''are'' + ''not''. Poderia ser contraído para ''aren''t''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'Is it cold outside?', 'Está frio lá fora?', 'Forma interrogativa: ''is'' vem antes do sujeito ''it''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'My parents aren''t home right now, but I''m here and my sister is in her room.', 'Meus pais não estão em casa agora, mas eu estou aqui e minha irmã está no quarto dela.', 'Frase combina as três formas do verbo em contextos diferentes, com contrações naturais da fala.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'I is a teacher.', 'I am a teacher.', 'O sujeito ''I'' sempre exige a forma ''am'', nunca ''is'' ou ''are''. Esse erro acontece porque em português não existe essa distinção de forma verbal ligada ao pronome.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'You is my friend.', 'You are my friend.', '''You'' sempre usa ''are'', tanto no singular (você) quanto no plural (vocês). Muitos brasileiros confundem porque pensam em ''você'' como se fosse igual a ''he/she'' no singular.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'She don''t is happy.', 'She is not happy.', 'O verbo ''to be'' nunca usa ''don''t'' ou ''doesn''t'' para formar a negativa. Basta colocar ''not'' depois do próprio verbo ''to be''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'Do you is ready?', 'Are you ready?', 'Para perguntas com o verbo ''to be'', não se usa ''do'' ou ''does''. O próprio verbo ''to be'' (am/is/are) já vai para o início da frase.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'verbo_to_be';

-- Artigos a/an/the (A1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Use a antes de som de consoante, an antes de som de vogal, ambos para algo não específico e singular; use the para algo específico, já conhecido ou único, no singular ou plural.', 'Em inglês, os artigos a, an e the aparecem antes de substantivos e ajudam a mostrar se estamos falando de algo específico ou não específico. Não existe um equivalente direto e automático em português, então é preciso aprender quando cada um se usa.

O artigo a se usa antes de substantivos contáveis no singular que começam com som de consoante, quando falamos de algo não específico, mencionado pela primeira vez ou de forma genérica. Por exemplo, a dog significa um cachorro qualquer, não um cachorro específico que já foi mencionado antes.

O artigo an tem exatamente o mesmo uso de a, mas se usa antes de palavras que começam com som de vogal, mesmo que a letra escrita não seja uma vogal. O que importa é o som, não a letra. Por isso dizemos an hour, porque a letra h não é pronunciada ali, e a umbrella, porque apesar de começar com u, o som inicial é de consoante.

O artigo the é o artigo definido, usado quando falamos de algo específico, que já foi mencionado antes, que é único ou que ambas as pessoas na conversa já sabem qual é. Diferente de a e an, the pode ser usado com substantivos singulares e plurais, e também com substantivos incontáveis.

Uma regra prática para decidir entre a/an e the é perguntar: o ouvinte sabe exatamente de qual coisa eu estou falando? Se a resposta é não, geralmente usamos a ou an. Se a resposta é sim, porque já foi citado antes ou porque só existe um, usamos the.

Também é importante lembrar que substantivos plurais e substantivos incontáveis não usam a nem an, porque esses dois artigos só existem no singular contável. Nesses casos, ou não usamos nenhum artigo, ou usamos the quando a coisa é específica.' FROM modulo WHERE codigo = 'artigos';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I have a cat.', 'Eu tenho um gato.', 'Cat começa com som de consoante e é uma coisa não específica sendo mencionada pela primeira vez, por isso usamos a.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'She wants an apple.', 'Ela quer uma maçã.', 'Apple começa com som de vogal, por isso usamos an em vez de a.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'The book on the table is mine.', 'O livro na mesa é meu.', 'Aqui já sabemos exatamente qual livro e qual mesa, então usamos the nos dois casos.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'He is an honest man.', 'Ele é um homem honesto.', 'A letra h em honest não é pronunciada, então o som inicial é de vogal e por isso usamos an.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'I saw a movie yesterday. The movie was great.', 'Eu vi um filme ontem. O filme foi ótimo.', 'Na primeira frase o filme é mencionado pela primeira vez, por isso a. Na segunda frase já sabemos qual filme é, por isso the.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'The sun is very bright today.', 'O sol está muito brilhante hoje.', 'The sun é usado porque só existe um sol, então ele é sempre específico.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'She wants a apple.', 'She wants an apple.', 'Apple começa com som de vogal, então o correto é usar an, não a. O erro comum é olhar só a primeira letra em vez do som.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'I have the dog.', 'I have a dog.', 'Como é a primeira vez que se fala desse cachorro e não é um cachorro específico já conhecido pelo ouvinte, o certo é usar a, não the.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'I like a music.', 'I like music.', 'Music é um substantivo incontável em inglês, então não se usa a ou an antes dele. Em português dizemos ''eu gosto de música'' sem artigo específico, e em inglês também não se usa artigo indefinido aqui.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'He is an university student.', 'He is a university student.', 'Apesar de university começar com a letra u, o som inicial é de consoante, como ''iú'', por isso o correto é usar a, não an.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'artigos';

-- Pronomes pessoais (A1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Pronomes sujeito (I, you, he, she, it, we, they) vêm antes do verbo e fazem a ação. Pronomes objeto (me, you, him, her, it, us, them) vêm depois do verbo ou de preposições e recebem a ação. I é sempre maiúsculo.', 'Pronomes pessoais são palavras que substituem nomes de pessoas ou coisas para não precisar repeti-los toda hora. Em inglês, existem dois grupos principais: os pronomes sujeito, que fazem a ação do verbo, e os pronomes objeto, que recebem a ação.

Os pronomes sujeito em inglês são: I (eu), you (você, tu), he (ele), she (ela), it (ele/ela para coisas e animais), we (nós), you (vocês) e they (eles, elas). Eles sempre vêm antes do verbo e indicam quem está fazendo a ação.

Uma diferença importante em relação ao português é que o pronome I (eu) sempre é escrito com letra maiúscula em inglês, não importa onde apareça na frase. Isso não tem exceção.

Os pronomes objeto são usados quando a pessoa ou coisa recebe a ação do verbo, ou seja, aparecem depois do verbo ou depois de uma preposição. Eles são: me (mim, me), you (você, te), him (ele, para homens), her (ela, para mulheres), it (ele/ela para coisas e animais), us (nós, nos), you (vocês) e them (eles, elas, para grupos).

Repare que you e it são iguais tanto na forma sujeito quanto na forma objeto. Isso facilita a memorização, mas exige atenção ao contexto da frase para saber qual função a palavra está exercendo.

Uma regra prática para não confundir: se a palavra vem antes do verbo principal, é pronome sujeito. Se vem depois do verbo, ou depois de palavras como to, for, with, at, então é pronome objeto.' FROM modulo WHERE codigo = 'pronomes_pessoais';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I like coffee.', 'Eu gosto de café.', 'I é pronome sujeito, faz a ação de gostar. Sempre maiúsculo, mesmo no meio da frase.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'She calls me every day.', 'Ela me liga todo dia.', 'She é sujeito (quem liga), me é objeto (quem recebe a ligação).' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'We saw them at the party.', 'Nós os vimos na festa.', 'We é sujeito, them é objeto porque vem depois do verbo saw.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'He gave the book to her.', 'Ele deu o livro para ela.', 'Her é objeto porque vem depois da preposição to.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'It is on the table, and I need it now.', 'Está na mesa, e eu preciso dele agora.', 'O primeiro it é sujeito (faz a ação de estar), o segundo it é objeto (recebe a ação de precisar).' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'You and I are friends, and they always help us.', 'Você e eu somos amigos, e eles sempre nos ajudam.', 'You, I e they são sujeitos das suas orações; us é objeto porque recebe a ajuda.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'i am happy.', 'I am happy.', 'O pronome I sempre é maiúsculo em inglês, mesmo no meio ou no início da frase. Em português não maiusculamos ''eu'', mas em inglês essa regra não tem exceção.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'Give the book to I.', 'Give the book to me.', 'Depois de preposições como to, for, with, sempre usamos o pronome objeto (me, him, her, us, them), nunca o pronome sujeito (I, he, she, we, they).' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'Her likes pizza.', 'She likes pizza.', 'Antes do verbo, quando a pessoa faz a ação, usamos o pronome sujeito (she), não o pronome objeto (her). Her só é usado quando ela recebe a ação.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'Me and him went to the store.', 'He and I went to the store.', 'Quando duas pessoas fazem a ação juntas como sujeito da frase, usamos os pronomes sujeito (I, he), não os pronomes objeto (me, him). Além disso, em inglês é mais comum e educado colocar ''I'' por último.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'pronomes_pessoais';

-- Presente simples (A1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Presente simples: usado para rotinas e fatos. Verbo igual ao infinitivo, exceto na terceira pessoa do singular (he, she, it), que recebe ''s'', ''es'' ou ''ies''. Negativas e perguntas usam do/does, e o verbo principal fica sem o ''s''.', 'O presente simples é o tempo verbal que usamos em inglês para falar de rotinas, hábitos e fatos que são sempre verdadeiros. Se você quer dizer o que faz todos os dias, com que frequência faz algo, ou descrever algo que é verdade em geral, você usa o presente simples.

A estrutura básica é sujeito mais verbo no infinitivo sem o ''to''. Por exemplo, ''I work'' significa ''eu trabalho''. O verbo não muda para eu, você, nós e eles: I work, you work, we work, they work.

A única mudança importante acontece na terceira pessoa do singular, ou seja, com he, she e it. Nesses casos, adicionamos a letra ''s'' no final do verbo. Por isso dizemos ''he works'', ''she works'', ''it works'', e não ''he work''.

Existem algumas regras especiais para formar esse ''s''. Verbos terminados em ch, sh, ss, x ou o recebem ''es'' em vez de apenas ''s''. Por exemplo, watch se torna watches, e go se torna goes. Verbos terminados em consoante mais y trocam o y por ''ies'', como study, que se torna studies. Já o verbo have é irregular e se torna has.

Para fazer frases negativas no presente simples, usamos do not ou does not antes do verbo no infinitivo, sem o ''s''. Com he, she e it usamos does not, e com os outros pronomes usamos do not. Por exemplo, ''she does not work'' e não ''she does not works''.

Para perguntas, colocamos do ou does no início da frase, seguido do sujeito e do verbo no infinitivo, sem o ''s''. Por exemplo, ''Does she work?'' e não ''Does she works?''.

Alguns advérbios são companheiros comuns do presente simples porque indicam frequência, como always, usually, often, sometimes e never. Eles geralmente ficam entre o sujeito e o verbo principal, como em ''I always drink coffee''.' FROM modulo WHERE codigo = 'presente_simples';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I work every day.', 'Eu trabalho todos os dias.', 'Sujeito ''I'' não recebe ''s'' no verbo.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'She works at a bank.', 'Ela trabalha em um banco.', 'Terceira pessoa do singular ''she'' exige o ''s'' no final do verbo.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'He watches TV at night.', 'Ele assiste TV à noite.', 'Verbo terminado em ''ch'' recebe ''es'' em vez de apenas ''s''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'They do not like coffee.', 'Eles não gostam de café.', 'Negativa com ''do not'' porque o sujeito ''they'' não é terceira pessoa do singular.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'Does your brother study English?', 'Seu irmão estuda inglês?', 'Pergunta com ''does'' porque ''your brother'' equivale a ''he''; o verbo principal fica sem ''s''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'Water boils at 100 degrees Celsius.', 'A água ferve a 100 graus Celsius.', 'Fato geral, sempre verdadeiro, também usa presente simples com ''s'' porque ''water'' é tratado como ''it''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'He work every day.', 'He works every day.', 'Com he, she e it, o verbo no presente simples precisa do ''s'' no final. Esquecer esse ''s'' é um dos erros mais comuns de brasileiros, porque em português o verbo não muda dessa forma.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'She does not likes chocolate.', 'She does not like chocolate.', 'Depois de does not ou do not, o verbo principal volta para a forma sem ''s'', porque o ''does'' já carrega a marca da terceira pessoa.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'Does he works here?', 'Does he work here?', 'Em perguntas com does, o verbo principal não recebe ''s'', pois essa função já é feita pelo próprio does.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'She go to school every day.', 'She goes to school every day.', 'Verbos terminados em o, como go, recebem ''es'' e não apenas ''s'' na terceira pessoa do singular.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_simples';

-- Passado simples (A2)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'O passado simples indica ações completamente terminadas no passado. Verbos regulares recebem o sufixo ''-ed'' (work -> worked), enquanto verbos irregulares têm formas próprias que precisam ser decoradas (go -> went). A forma é igual para todas as pessoas, e perguntas e negativas usam o auxiliar ''did'' com o verbo na forma base.', 'O passado simples é usado para falar de ações que aconteceram e terminaram completamente no passado. Em português, seria como dizer ''eu trabalhei'', ''ele comeu'', ''eles foram''. A ação já acabou e geralmente vem acompanhada de uma referência de tempo, como ''ontem'', ''na semana passada'' ou ''em 2020''.

Os verbos em inglês se dividem em dois grupos quando vamos para o passado: regulares e irregulares. Os verbos regulares seguem uma regra fixa, muito simples: adiciona-se ''-ed'' ao final do verbo no infinitivo. Por exemplo, ''work'' vira ''worked'', ''play'' vira ''played'', ''clean'' vira ''cleaned''. Essa é a mesma forma para todas as pessoas: eu, você, ele, ela, nós, eles. Não existe conjugação diferente por pessoa como em português.

Existem pequenas variações na escrita ao adicionar ''-ed''. Se o verbo termina em ''e'', apenas se adiciona ''d'': ''live'' vira ''lived''. Se termina em consoante + ''y'', o ''y'' vira ''i'' antes do ''ed'': ''study'' vira ''studied''. Se o verbo é curto e termina em consoante-vogal-consoante, às vezes dobra-se a última consoante: ''stop'' vira ''stopped''.

Já os verbos irregulares não seguem nenhuma regra fixa. Cada um tem sua própria forma no passado, e é preciso decorar essas formas uma por uma. Por exemplo, ''go'' vira ''went'', ''eat'' vira ''ate'', ''have'' vira ''had'', ''see'' vira ''saw''. Não tem lógica ou padrão que sirva para todos, então a única forma de aprender é praticando e memorizando as formas mais comuns aos poucos.

Uma boa notícia é que, tanto para verbos regulares quanto irregulares, a forma do passado é igual para todas as pessoas. Você não precisa se preocupar em conjugar diferente para ''eu'', ''você'', ''ele'' ou ''eles''. A dificuldade real está em lembrar qual é a forma certa de cada verbo irregular e em escrever corretamente o ''-ed'' dos regulares.

Para fazer perguntas e negativas no passado simples, usamos o verbo auxiliar ''did'' (que é o passado de ''do'') junto com o verbo no infinitivo, sem conjugação. Por exemplo: ''Did you work yesterday?'' (Você trabalhou ontem?) e ''I did not work yesterday'' (Eu não trabalhei ontem). Note que o verbo principal volta para a forma base, sem ''-ed'' e sem a forma irregular, porque o ''did'' já indica que estamos no passado.' FROM modulo WHERE codigo = 'passado_simples';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I worked yesterday.', 'Eu trabalhei ontem.', 'Verbo regular ''work'' recebe ''-ed'' para formar o passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'She studied English last year.', 'Ela estudou inglês no ano passado.', 'O ''y'' de ''study'' vira ''i'' antes de adicionar ''-ed'', porque vem depois de consoante.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'They went to the beach last weekend.', 'Eles foram à praia no fim de semana passado.', '''Go'' é irregular e vira ''went'' no passado, sem seguir nenhuma regra fixa.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'He didn''t eat breakfast this morning.', 'Ele não comeu café da manhã hoje de manhã.', 'Na negativa, usamos ''didn''t'' e o verbo ''eat'' volta para a forma base, sem virar ''ate''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'Did you see the movie last night?', 'Você viu o filme ontem à noite?', 'Na pergunta, usamos ''did'' no início e o verbo ''see'' fica na forma base, não vira ''saw''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'We stopped at the gas station and had lunch.', 'Nós paramos no posto de gasolina e almoçamos.', '''Stop'' dobra a consoante final antes do ''-ed'', e ''have'' é irregular, virando ''had''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'I goed to school yesterday.', 'I went to school yesterday.', '''Go'' é um verbo irregular, então não se adiciona ''-ed''. A forma correta no passado é ''went'', que precisa ser memorizada.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'Did you went to the party?', 'Did you go to the party?', 'Quando já usamos ''did'' na pergunta, o verbo principal volta para a forma base (infinitivo sem ''to''). Não se usa a forma do passado depois de ''did''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'She don''t worked yesterday.', 'She didn''t work yesterday.', 'Na negativa do passado, usa-se ''didn''t'' (não ''don''t'', que é presente) e o verbo principal fica na forma base, sem ''-ed''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'I studyed hard for the test.', 'I studied hard for the test.', 'Quando o verbo termina em consoante + ''y'', o ''y'' se transforma em ''i'' antes de adicionar ''-ed''. A grafia correta é ''studied'', não ''studyed''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_simples';

-- Comparativos e superlativos (A2)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Adjetivos curtos (uma sílaba) usam -er e the -est. Adjetivos longos (duas ou mais sílabas) usam more e the most. Comparativos vêm com ''than''; superlativos vêm com ''the''. Alguns adjetivos são irregulares, como good/better/the best e bad/worse/the worst.', 'Em inglês, usamos comparativos para comparar duas coisas e superlativos para dizer qual é a maior, menor, melhor, entre três ou mais coisas.

A forma que você usa depende do tamanho do adjetivo, ou seja, de quantas sílabas ele tem.

Para adjetivos curtos, com uma sílaba, você adiciona -er para o comparativo e -est para o superlativo. O superlativo sempre vem acompanhado da palavra ''the'' antes dele. Exemplo: tall vira taller (comparativo) e the tallest (superlativo).

Alguns adjetivos curtos precisam de um pequeno ajuste na escrita antes de adicionar -er ou -est. Se o adjetivo termina em ''e'', você só adiciona -r ou -st, como em nice, nicer, the nicest. Se termina em consoante depois de uma vogal curta, você dobra a consoante final, como em big, bigger, the biggest. Se termina em ''y'' depois de consoante, o ''y'' vira ''i'', como em happy, happier, the happiest.

Para adjetivos longos, com duas ou mais sílabas, normalmente não adicionamos -er ou -est. Em vez disso, usamos more antes do adjetivo para o comparativo e most antes do adjetivo para o superlativo, sempre com ''the'' antes de most. Exemplo: expensive vira more expensive e the most expensive.

Alguns adjetivos de duas sílabas, principalmente os terminados em ''y'', ''er'', ''ow'' e ''le'', também podem usar -er e -est, como happy, happier, the happiest ou simple, simpler, the simplest. Mas na dúvida, para adjetivos de duas sílabas ou mais, o mais seguro é usar more e most.

Existem também adjetivos irregulares, que não seguem nenhuma dessas regras e precisam ser decorados. Os mais comuns são good, better, the best e bad, worse, the worst.

Para comparar duas coisas usando o comparativo, a estrutura é: sujeito + verbo + comparativo + than + a outra coisa. Para dizer que algo é o superlativo de um grupo, a estrutura é: sujeito + verbo + the + superlativo + (of/in + grupo).' FROM modulo WHERE codigo = 'comparativos';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'My brother is taller than me.', 'Meu irmão é mais alto do que eu.', 'Tall tem uma sílaba, então usamos -er. O comparativo vem seguido de ''than''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'This is the tallest building in the city.', 'Este é o prédio mais alto da cidade.', 'Para o superlativo, usamos the + adjetivo-est.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'This phone is more expensive than that one.', 'Este telefone é mais caro do que aquele.', 'Expensive tem três sílabas, então usamos more, não -er.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'This is the most expensive phone in the store.', 'Este é o telefone mais caro da loja.', 'Superlativo de adjetivo longo: the most + adjetivo.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'Her new job is better than her old one.', 'O novo emprego dela é melhor do que o antigo.', 'Good é irregular: vira better no comparativo, não gooder.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'This was the worst movie I have ever seen, but it was cheaper than the other tickets.', 'Este foi o pior filme que eu já vi, mas foi mais barato do que os outros ingressos.', 'Combina o superlativo irregular the worst com o comparativo regular cheaper, mostrando as duas regras juntas.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'This car is more fast than mine.', 'This car is faster than mine.', 'Fast é um adjetivo curto, de uma sílaba, então usa -er. Usar more com adjetivos curtos é um erro comum de quem traduz ''mais'' direto do português.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'She is the beautifulest girl in the class.', 'She is the most beautiful girl in the class.', 'Beautiful tem três sílabas, então precisa de most, e não do sufixo -est, que só serve para adjetivos curtos.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'He is more good than his brother at football.', 'He is better than his brother at football.', 'Good é irregular e vira better no comparativo. Não existe ''more good'' nem ''gooder'' em inglês.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'This is more tall building in the city.', 'This is the tallest building in the city.', 'O superlativo sempre precisa do artigo ''the'' antes dele, e tall, por ser curto, usa -est, não more.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'comparativos';

-- "There is" e "there are" (A2)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Use ''there is'' para uma coisa (singular) e ''there are'' para duas ou mais coisas (plural), para dizer que algo existe em um lugar.', 'Em português, usamos o verbo ''ter'' ou ''haver'' para dizer que algo existe em algum lugar, como em ''tem um gato na cozinha'' ou ''há um gato na cozinha''. Em inglês, essa ideia de existência é expressa com a estrutura ''there is'' ou ''there are'', seguida do substantivo.

A escolha entre ''there is'' e ''there are'' depende do número do substantivo que vem depois. Usamos ''there is'' quando o substantivo é singular, ou seja, quando existe apenas uma coisa. Usamos ''there are'' quando o substantivo é plural, ou seja, quando existem duas ou mais coisas.

''There is'' também é usado com substantivos incontáveis, como ''water'', ''milk'', ''time'', porque essas palavras não têm plural. Já ''there are'' é sempre usado com substantivos contáveis no plural, geralmente terminados em ''s''.

Na forma contraída, ''there is'' pode ser escrito como ''there''s'', usada bastante na fala e em textos informais. ''There are'' não tem uma forma contraída comum na escrita, mas na fala às vezes soa como ''there''re'', embora seja mais raro de aparecer escrito assim.

Para formar a negativa, basta acrescentar ''not'' depois do verbo: ''there is not'' (there isn''t) ou ''there are not'' (there aren''t). Para perguntas, invertemos a ordem: ''Is there...?'' ou ''Are there...?''.

Uma diferença importante em relação ao português é que, em inglês, essa estrutura sempre precisa do ''there'' antes do verbo. Não é possível simplesmente dizer ''is a book on the table'', do jeito que em português dizemos apenas ''tem um livro na mesa'' sem precisar de uma palavra equivalente ao ''there''.' FROM modulo WHERE codigo = 'there_is_are';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'There is a book on the table.', 'Tem um livro na mesa.', '''Book'' é singular, por isso usamos ''there is''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'There are two books on the table.', 'Tem dois livros na mesa.', '''Books'' está no plural, por isso usamos ''there are''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'There''s a spider in the bathroom!', 'Tem uma aranha no banheiro!', 'Forma contraída de ''there is'', muito comum na fala.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'There isn''t any milk in the fridge.', 'Não tem leite na geladeira.', '''Milk'' é incontável, então usamos ''there isn''t'', mesmo sem ser uma coisa só.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'Are there any restaurants near here?', 'Tem algum restaurante aqui perto?', 'Pergunta com substantivo plural, por isso ''are there''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'There aren''t many students in this class today.', 'Não tem muitos alunos nesta turma hoje.', 'Negativa no plural, usando ''there aren''t'' com ''students''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'It has a lot of people at the party.', 'There are a lot of people at the party.', 'Muitos brasileiros traduzem ''tem'' diretamente como ''has'' ou ''it has'', mas para dizer que algo existe em um lugar o inglês usa ''there is'' ou ''there are'', não o verbo ''have''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'There is two cats in the garden.', 'There are two cats in the garden.', 'Como ''cats'' está no plural, o verbo precisa ser ''are'', não ''is''. É comum esquecer de concordar o verbo com o substantivo depois de ''there''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'Is a problem with the computer.', 'There is a problem with the computer.', 'Em português, a frase pode começar direto com ''é'' ou ''tem'', mas em inglês é obrigatório usar ''there'' antes do verbo para indicar existência.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'There are some water in the bottle.', 'There is some water in the bottle.', '''Water'' é um substantivo incontável e não tem plural, então mesmo com a palavra ''some'' o verbo correto é ''is'', não ''are''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'there_is_are';

-- Presente perfeito (B1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Presente perfeito: have/has + particípio passado. Usa-se for com duração de tempo, since com ponto de início, already em afirmativas (já) e yet em negativas e perguntas (ainda/já), geralmente no fim da frase.', 'O presente perfeito é um tempo verbal em inglês que conecta o passado com o presente. Ele descreve ações que aconteceram em um momento não especificado antes de agora, ou que começaram no passado e continuam até o momento presente. É formado com o verbo auxiliar have (ou has, na terceira pessoa do singular: he, she, it) seguido do particípio passado do verbo principal.

O particípio passado dos verbos regulares termina em -ed, igual ao passado simples: work vira worked, play vira played. Já os verbos irregulares têm formas próprias que precisam ser memorizadas, como go que vira gone, eat que vira eaten, e see que vira seen. Essas formas ficam sempre na terceira coluna das tabelas de verbos irregulares.

A estrutura afirmativa é sujeito + have/has + particípio. Por exemplo: I have finished, she has arrived. Na forma negativa, acrescenta-se not depois do auxiliar, formando haven''t ou hasn''t: I haven''t finished, she hasn''t arrived. Nas perguntas, o auxiliar vem antes do sujeito: Have you finished? Has she arrived?

Duas palavras muito usadas com o presente perfeito são for e since, que indicam duração. For se usa com um período de tempo, como for two years (por dois anos) ou for a long time (por muito tempo). Since se usa com um ponto específico no tempo em que a ação começou, como since 2010 ou since Monday. A diferença é que for conta a duração, enquanto since marca o início.

Outras duas palavras importantes são already e yet. Already significa ''já'' e normalmente aparece em frases afirmativas, indicando que algo aconteceu antes do esperado ou mais cedo do que se pensava. Ele fica entre o auxiliar e o particípio: I have already finished. Yet significa ''ainda'' ou ''já'' em perguntas e negativas, e geralmente vai no final da frase: I haven''t finished yet (ainda não terminei) ou Have you finished yet? (você já terminou?).


É importante não confundir o presente perfeito com o passado simples. O passado simples é usado para ações completas em um momento específico e definido do passado, como yesterday ou last year. O presente perfeito é usado quando o momento exato não importa ou não é mencionado, ou quando a ação tem relevância para o presente.' FROM modulo WHERE codigo = 'presente_perfeito';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I have finished my homework.', 'Eu terminei minha lição de casa.', 'Ação completa sem momento específico mencionado; o foco é o resultado presente.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'She has lived in London since 2015.', 'Ela mora em Londres desde 2015.', 'Since marca o ponto no tempo em que a ação começou e que continua até agora.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'We have known each other for ten years.', 'Nós nos conhecemos há dez anos.', 'For indica a duração total do período, não o momento de início.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'He has already eaten lunch.', 'Ele já almoçou.', 'Already aparece em frase afirmativa, entre o auxiliar has e o particípio eaten.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'Have you finished the report yet?', 'Você já terminou o relatório?', 'Yet em pergunta, no final da frase, perguntando se algo esperado já aconteceu.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'They haven''t arrived yet, but they have already called twice.', 'Eles ainda não chegaram, mas já ligaram duas vezes.', 'Combina yet na negativa (fim da frase) e already na afirmativa (antes do particípio) na mesma frase.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'I have finished my homework yesterday.', 'I finished my homework yesterday.', 'Yesterday é um momento específico e definido do passado, então exige o passado simples, não o presente perfeito.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'I have this job since three years.', 'I have had this job for three years.', 'Since não se usa com um período de duração como ''três anos''; use for para duração. Além disso, o verbo precisa estar no presente perfeito (have had), não no presente simples.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'She has already arrive.', 'She has already arrived.', 'Depois do auxiliar has, o verbo principal precisa estar no particípio passado (arrived), não no infinitivo sem to.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'Did you finish the report yet?', 'Have you finished the report yet?', 'Yet, quando pergunta sobre algo que pode ter acontecido até agora, normalmente combina com o presente perfeito, não com o passado simples (did).' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'presente_perfeito';

-- Condicionais básicos (B1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Primeiro condicional (if + presente simples, will + infinitivo) fala de situações reais e possíveis no futuro. Segundo condicional (if + passado simples, would + infinitivo) fala de situações hipotéticas ou irreais no presente ou futuro. No segundo condicional, usa-se ''were'' para todas as pessoas com o verbo ''to be''.', 'Condicionais são frases que expressam uma condição e o seu resultado, geralmente ligadas pela palavra ''if'' (se). Em inglês existem várias estruturas condicionais, e neste conceito vamos ver as duas mais usadas no dia a dia: o primeiro condicional e o segundo condicional.

O primeiro condicional fala de situações reais e possíveis no futuro. Usamos quando achamos que a condição pode realmente acontecer. A estrutura é: if + presente simples, will + verbo no infinitivo sem ''to''. Por exemplo, ''se chover, eu vou levar um guarda-chuva'' é uma condição real, que pode acontecer amanhã.

Já o segundo condicional fala de situações hipotéticas, improváveis ou puramente imaginárias no presente ou futuro. Usamos quando a condição é irreal, contrária aos fatos, ou muito distante da realidade. A estrutura é: if + passado simples, would + verbo no infinitivo sem ''to''. Por exemplo, ''se eu fosse rico, eu viajaria o mundo'' expressa um desejo ou uma situação que não é real agora.

Um ponto importante: no segundo condicional, quando o verbo é ''to be'', o correto em inglês formal é sempre usar ''were'' para todas as pessoas, inclusive ''I'' e ''he/she/it''. Assim, dizemos ''if I were you'' e não ''if I was you'', embora na fala informal muitos nativos usem ''was''. Para o aluno de inglês, o mais seguro é aprender e usar ''were''.

Repare também que a ordem das orações pode mudar. Você pode começar pela condição (''if...'') ou pelo resultado (''will/would...''). Quando a oração com ''if'' vem primeiro, usamos vírgula antes da segunda parte. Quando o resultado vem primeiro, não usamos vírgula. Por exemplo: ''If it rains, I will stay home'' ou ''I will stay home if it rains'', ambas corretas, mas a segunda sem vírgula.

A diferença principal entre os dois condicionais não é apenas de tempo verbal, mas de atitude do falante em relação à possibilidade. O primeiro condicional trata a condição como algo provável ou real; o segundo condicional trata a condição como algo improvável, hipotético ou imaginário. Por isso, a mesma ideia pode ser expressa nos dois condicionais dependendo de quão realista o falante considera a situação.' FROM modulo WHERE codigo = 'condicionais_basicos';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'If it rains, I will stay home.', 'Se chover, eu vou ficar em casa.', 'Primeiro condicional: condição real e possível, com presente simples depois de ''if'' e ''will'' na consequência.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'If you study, you will pass the exam.', 'Se você estudar, você vai passar na prova.', 'Situação futura considerada provável, por isso usamos o primeiro condicional.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'If I had a lot of money, I would travel around the world.', 'Se eu tivesse muito dinheiro, eu viajaria pelo mundo.', 'Segundo condicional: situação hipotética, pouco provável ou imaginária, com passado simples depois de ''if'' e ''would'' na consequência.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'If I were you, I would talk to her.', 'Se eu fosse você, eu falaria com ela.', 'Uso de ''were'' em vez de ''was'' com ''I'', comum em conselhos hipotéticos.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'She will call you if she needs help.', 'Ela vai te ligar se ela precisar de ajuda.', 'A oração com ''if'' vem depois do resultado, por isso não usamos vírgula.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'If they lived closer, we would see them more often.', 'Se eles morassem mais perto, nós os veríamos com mais frequência.', 'Segundo condicional usado para uma situação atual que é o oposto da realidade.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'If it will rain, I will stay home.', 'If it rains, I will stay home.', 'Depois de ''if'' no primeiro condicional, usamos o presente simples, nunca ''will''. O ''will'' aparece apenas na oração de resultado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'If I was you, I would study more.', 'If I were you, I would study more.', 'No segundo condicional, o correto no inglês formal é usar ''were'' para todas as pessoas com o verbo ''to be'', mesmo com ''I''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'If I have more time, I would travel more.', 'If I had more time, I would travel more.', 'É preciso manter a coerência entre as duas partes da frase: se a consequência usa ''would'' (segundo condicional), a condição precisa estar no passado simples, não no presente.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'If she will come, we will start the meeting.', 'If she comes, we will start the meeting.', 'Brasileiros costumam traduzir literalmente o futuro do português (''se ela vier'') usando ''will'' depois de ''if'', mas em inglês a oração condicional usa presente simples mesmo falando de uma ação futura.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_basicos';

-- Phrasal verbs comuns (B1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Phrasal verbs combinam um verbo com uma partícula (como up, on, off) para criar um significado novo, que deve ser aprendido como bloco único; alguns aceitam o objeto entre verbo e partícula (separáveis), outros não (inseparáveis), e pronomes sempre ficam entre os dois quando o verbo é separável.', 'Phrasal verbs são combinações de um verbo com uma partícula, que pode ser uma preposição (como up, on, off, in, out) ou um advérbio. Juntos, o verbo e a partícula formam um significado novo, que muitas vezes não tem relação direta com o significado do verbo sozinho.

Por exemplo, o verbo look sozinho significa olhar. Mas look for significa procurar, look after significa cuidar de, e look up significa pesquisar ou consultar. Cada combinação cria um verbo praticamente novo, com sentido próprio.

Esse é um dos maiores desafios do inglês para brasileiros, porque em português não temos essa estrutura da mesma forma. Nós usamos verbos diferentes para cada ideia, como procurar, cuidar e pesquisar, enquanto o inglês usa o mesmo verbo base com partículas diferentes. Por isso, phrasal verbs precisam ser aprendidos como um bloco único, não traduzidos palavra por palavra.

Alguns phrasal verbs são separáveis, ou seja, o objeto pode ficar entre o verbo e a partícula ou depois dela. Por exemplo, você pode dizer turn off the light ou turn the light off, e ambos estão corretos. Mas quando o objeto é um pronome, como it ou them, ele precisa ficar obrigatoriamente entre o verbo e a partícula: turn it off, nunca turn off it.

Outros phrasal verbs são inseparáveis, o que significa que a partícula sempre vem logo depois do verbo, sem nada no meio, mesmo com pronomes. Por exemplo, look after her está correto, mas look her after está errado.

No dia a dia, phrasal verbs comuns aparecem o tempo todo em conversas, mensagens, séries e músicas. Dominar os mais frequentes, como get up, wake up, turn on/off, look for, give up, find out e get along, já ajuda muito a soar mais natural e a entender inglês falado sem parecer um texto de livro.' FROM modulo WHERE codigo = 'phrasal_verbs_comuns';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I get up at seven every day.', 'Eu me levanto às sete todos os dias.', 'Get up é um dos phrasal verbs mais básicos e usados para rotina diária.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'Can you turn off the TV, please?', 'Você pode desligar a TV, por favor?', 'Turn off é separável: também é possível dizer turn the TV off.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'Turn it off, please.', 'Desligue ela, por favor.', 'Com pronome (it), o objeto fica obrigatoriamente entre o verbo e a partícula.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'I''m looking for my keys.', 'Estou procurando minhas chaves.', 'Look for é inseparável e significa procurar, não tem relação direta com olhar.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'She had to look after her little brother yesterday.', 'Ela teve que cuidar do irmão mais novo dela ontem.', 'Look after significa cuidar de; é inseparável, então o objeto sempre vem depois da partícula.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'I found out the truth after talking to him.', 'Eu descobri a verdade depois de falar com ele.', 'Find out significa descobrir uma informação; muito comum em conversas e notícias.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'I look my keys for.', 'I look for my keys.', 'Look for é inseparável; a partícula for precisa vir logo depois do verbo look, nunca depois do objeto.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'Turn off it.', 'Turn it off.', 'Quando o objeto é um pronome (it, them, her, him), ele deve ficar entre o verbo e a partícula em phrasal verbs separáveis.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'I woke at six.', 'I woke up at six.', 'Wake up é o phrasal verb usado para acordar; sem o up, wake sozinho soa incompleto ou tem outro uso mais formal e raro no dia a dia.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'She takes care after her mother.', 'She looks after her mother.', 'Brasileiros tendem a traduzir cuidar de literalmente; o phrasal verb correto e natural em inglês é look after, não uma tradução direta de take care.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'phrasal_verbs_comuns';

-- Passado perfeito (B2)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'O passado perfeito (had + particípio) mostra que uma ação aconteceu antes de outra ação já no passado, estabelecendo uma ordem clara entre dois momentos passados.', 'O passado perfeito, em inglês past perfect, é formado com had mais o particípio passado do verbo (had + past participle). Ele existe para mostrar que uma ação aconteceu antes de outra ação no passado, criando uma linha do tempo clara entre dois momentos que já ficaram para trás.

A lógica é a seguinte: quando você conta uma história no passado simples e precisa voltar ainda mais no tempo para explicar algo que já tinha acontecido antes, você usa o passado perfeito para esse evento anterior. O passado simples marca o ponto de referência da narrativa, e o passado perfeito marca o que veio antes dele.

A estrutura afirmativa é sujeito mais had mais particípio passado, como em she had finished. Na negativa, usa-se had not ou hadn''t mais particípio, como em she hadn''t finished. Na interrogativa, o had vai antes do sujeito, como em had she finished?

É importante entender que o passado perfeito só faz sentido quando existe, explícita ou implicitamente, um segundo momento no passado que serve de referência. Se você disser apenas I had eaten sem nenhum contexto, a frase fica incompleta, porque o ouvinte não sabe em relação a quando essa ação aconteceu antes.

Uma armadilha comum é achar que o passado perfeito é usado sempre que se fala de algo há muito tempo atrás, mas não é bem assim. O que importa não é a distância no tempo, e sim a relação de anterioridade entre dois eventos passados. Se há apenas um evento passado sendo contado, geralmente o passado simples é suficiente.

Em inglês falado informal, é comum contrair had a ''d, como em she''d finished, o que pode confundir porque a mesma contração ''d também é usada para would. A diferença fica clara pelo verbo que segue: had é seguido de particípio passado, enquanto would é seguido de verbo no infinitivo sem to.' FROM modulo WHERE codigo = 'passado_perfeito';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'I had already left when she called.', 'Eu já tinha saído quando ela ligou.', 'Sair aconteceu antes de ela ligar; os dois eventos estão no passado, mas há uma ordem clara entre eles.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'By the time we arrived, the movie had already started.', 'Quando chegamos, o filme já tinha começado.', 'By the time é uma expressão comum que introduz o momento de referência no passado simples, enquanto o evento anterior vai no passado perfeito.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'She hadn''t seen that movie before, so she wanted to watch it.', 'Ela não tinha visto aquele filme antes, então quis assistir.', 'A forma negativa hadn''t mostra que a ação de ver o filme não aconteceu antes do momento em que ela decidiu assistir.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'Had you ever traveled abroad before that trip?', 'Você já tinha viajado para fora do país antes daquela viagem?', 'Na pergunta, had vem antes do sujeito you, e a referência é a viagem específica mencionada depois.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'When I got home, my roommate had already cooked dinner and cleaned the kitchen.', 'Quando cheguei em casa, meu colega de quarto já tinha cozinhado o jantar e limpado a cozinha.', 'Duas ações anteriores ao momento de chegada em casa são marcadas com o mesmo had, sem precisar repeti-lo.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'If I had known you were coming, I would have baked a cake.', 'Se eu tivesse sabido que você viria, eu teria feito um bolo.', 'Aqui o passado perfeito aparece dentro de uma condicional do tipo três, para falar de uma situação hipotética que não aconteceu no passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'I had went to the store yesterday.', 'I went to the store yesterday.', 'Quando há apenas um evento passado sendo contado, sem outro evento posterior para servir de referência, usa-se o passado simples, não o passado perfeito. Além disso, o particípio de go é gone, não went.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'When she arrived, he had leaved already.', 'When she arrived, he had left already.', 'Leave é um verbo irregular; o particípio passado correto é left, não leaved. O had está certo aqui porque a saída dele aconteceu antes da chegada dela.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'I had lived in London for two years before I moving to Paris.', 'I had lived in London for two years before I moved to Paris.', 'Depois de before, usa-se o passado simples do segundo evento (moved), e o passado perfeito fica reservado para o evento que aconteceu antes dele (had lived).' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'She''d finish her homework before dinner, so she watched TV.', 'She''d finished her homework before dinner, so she watched TV.', 'A contração ''d de had deve ser seguida do particípio passado (finished), não da forma base do verbo (finish), que é o padrão usado depois de would.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'passado_perfeito';

-- Voz passiva (B2)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'A voz passiva desloca o foco da frase da ação para quem ou o que a recebe, usando a estrutura TO BE (no tempo verbal correto) + particípio passado, com o agente introduzido por ''by'' apenas quando necessário.', 'Na voz ativa, o sujeito da frase é quem pratica a ação: ''The chef cooks the meal'' (o chef cozinha a refeição). Na voz passiva, o foco muda para quem ou o que recebe a ação, e o agente (quem pratica a ação) pode até ser omitido: ''The meal is cooked'' (a refeição é cozida). Isso é muito comum em inglês, mais até do que em português em certos contextos, especialmente em textos formais, científicos, jornalísticos e institucionais, quando o autor da ação é desconhecido, óbvio ou irrelevante.

A estrutura básica da voz passiva é: sujeito (que era o objeto na ativa) + verbo TO BE conjugado no tempo verbal desejado + particípio passado do verbo principal + (by + agente, se necessário). O verbo TO BE é o elemento que muda de forma para indicar o tempo verbal, enquanto o verbo principal permanece sempre no particípio passado (a terceira forma do verbo, como ''written'', ''made'', ''done'', ''seen'').

Vamos ver como fica em cada tempo verbal principal. No presente simples: ''is/are + particípio'' (The house is cleaned every week). No passado simples: ''was/were + particípio'' (The house was cleaned yesterday). No futuro com ''will'': ''will be + particípio'' (The house will be cleaned tomorrow). No presente perfeito: ''has/have been + particípio'' (The house has been cleaned). No passado perfeito: ''had been + particípio'' (The house had been cleaned before they arrived). Com modais (can, must, should, etc.): ''modal + be + particípio'' (The house must be cleaned).

Um ponto importante é o uso de ''by'' para introduzir o agente da ação, mas isso só é necessário quando essa informação é relevante ou nova para quem lê ou ouve. Se o agente for óbvio, genérico ou desconhecido, ele costuma ser omitido: ''English is spoken in many countries'' (não precisamos dizer quem fala).

A voz passiva também é usada com frequência em construções impessoais, muito comuns em inglês acadêmico e jornalístico, como ''It is believed that...'', ''It is said that...'', ''It has been reported that...''. Essas estruturas evitam apontar diretamente quem afirma algo, dando um tom mais objetivo e formal ao texto.

Outro uso relevante é quando o verbo principal tem dois objetos, como ''give'', ''send'', ''show'', ''offer''. Nesses casos, tanto o objeto direto quanto o indireto podem virar sujeito da voz passiva, embora a construção com o objeto indireto como sujeito seja mais natural e comum em inglês: ''They gave her a prize'' pode virar ''She was given a prize'' (mais natural) ou ''A prize was given to her'' (também correto, mas menos comum no dia a dia).' FROM modulo WHERE codigo = 'voz_passiva';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'The report is written every month.', 'O relatório é escrito todo mês.', 'Presente simples na passiva: ''is'' + particípio. O foco está na ação recorrente, não em quem escreve.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'The window was broken last night.', 'A janela foi quebrada ontem à noite.', 'Passado simples na passiva: ''was'' + particípio. Não sabemos ou não importa quem quebrou a janela.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'The results will be announced tomorrow.', 'Os resultados serão anunciados amanhã.', 'Futuro com ''will'' na passiva: ''will be'' + particípio, indicando uma ação futura.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'This song has been recorded by many artists.', 'Essa música já foi gravada por muitos artistas.', 'Presente perfeito na passiva: ''has been'' + particípio. Aqui o agente (''by many artists'') é relevante, por isso aparece.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'The documents must be signed before Friday.', 'Os documentos devem ser assinados antes de sexta-feira.', 'Uso de modal (must) na passiva: ''must be'' + particípio, indicando obrigação sem especificar quem deve assinar.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'It is believed that the ancient city was destroyed by an earthquake.', 'Acredita-se que a cidade antiga foi destruída por um terremoto.', 'Construção impessoal comum em textos formais: ''It is believed that...'' evita dizer quem acredita nisso, e a segunda oração também está na passiva.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'The letter is wrote by John.', 'The letter is written by John.', 'Depois do verbo TO BE na passiva, usa-se o particípio passado (terceira forma do verbo), não o passado simples. ''Wrote'' é passado simples de ''write'', mas o particípio correto é ''written''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'The car was fixed for the mechanic.', 'The car was fixed by the mechanic.', 'Para indicar quem pratica a ação na voz passiva, usa-se a preposição ''by'', e não ''for''. ''For'' tem outro sentido, como finalidade ou benefício.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'The house is being clean right now.', 'The house is being cleaned right now.', 'Na passiva contínua (''is/are being + particípio''), é preciso usar o particípio passado do verbo, e não o adjetivo ou a forma base. ''Clean'' aqui deveria ser ''cleaned''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'A decision was make yesterday.', 'A decision was made yesterday.', 'Muitos alunos usam a forma base do verbo (''make'') em vez do particípio passado (''made'') depois de ''was/were'' na voz passiva, provavelmente por interferência do português, onde a concordância verbal funciona de outro jeito.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'voz_passiva';

-- Condicionais mistos (B2)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Condicionais mistos combinam tempos diferentes entre condição e consequência: passado hipotético causando efeito no presente (if + had + particípio, would + verbo base) ou presente hipotético causando efeito no passado (if + past simple, would have + particípio).', 'Os condicionais mistos são construções em que a condição e a consequência pertencem a momentos diferentes do tempo, misturando as estruturas do segundo condicional (hipotético, presente ou futuro) com as do terceiro condicional (hipotético, passado). Isso permite expressar como uma situação irreal do passado afeta o presente, ou como uma característica permanente hipotética teria mudado um evento passado.

Existem dois tipos principais de condicionais mistos. O primeiro tipo combina uma condição no passado (usando o past perfect, ''if + had + particípio'') com uma consequência no presente (usando ''would + verbo base''). Esse tipo é usado quando uma ação ou evento não realizado no passado tem um efeito hipotético sobre o presente. Por exemplo, se alguém não estudou medicina no passado, essa pessoa não é médica hoje.

O segundo tipo combina uma condição no presente, geralmente uma característica ou estado permanente e hipotético (usando ''if + past simple''), com uma consequência no passado (usando ''would have + particípio''). Esse tipo é usado quando uma característica atual e hipotética teria influenciado um evento específico no passado. Por exemplo, se alguém não fosse tão tímido por natureza (característica do presente), teria aceitado aquele convite há dois anos (evento do passado).

A chave para dominar os condicionais mistos é entender que você não precisa seguir a estrutura fixa do segundo ou terceiro condicional completo. Em vez disso, você escolhe o tempo verbal da condição (if-clause) de acordo com quando ela se refere, e escolhe o tempo verbal da consequência de acordo com quando ela se refere, de forma independente uma da outra. Isso exige atenção ao contexto e à lógica temporal da frase, não apenas à memorização de uma fórmula.

Esses condicionais são muito usados na fala e na escrita para expressar arrependimentos, especulações sobre causas e consequências, e para conectar hipóteses de diferentes períodos temporais de forma natural e sofisticada.' FROM modulo WHERE codigo = 'condicionais_mistos';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'If I had studied harder, I would have a better job now.', 'Se eu tivesse estudado mais, eu teria um emprego melhor agora.', 'Condição no passado (não estudei) com consequência no presente (não tenho um bom emprego agora).' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'If she weren''t so shy, she would have spoken up at the meeting yesterday.', 'Se ela não fosse tão tímida, ela teria falado na reunião ontem.', 'Condição no presente, uma característica permanente (a timidez), com consequência específica no passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'If we had left earlier, we wouldn''t be stuck in traffic right now.', 'Se tivéssemos saído mais cedo, não estaríamos presos no trânsito agora.', 'Ação não realizada no passado (não saímos cedo) afetando a situação atual (estar preso no trânsito).' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'If he were more organized, he wouldn''t have lost the documents last week.', 'Se ele fosse mais organizado, ele não teria perdido os documentos na semana passada.', 'Característica geral e hipotética do presente influenciando um evento pontual do passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'If you hadn''t helped me back then, I wouldn''t be who I am today.', 'Se você não tivesse me ajudado naquela época, eu não seria quem eu sou hoje.', 'Evento específico do passado com consequência duradoura e presente sobre a identidade da pessoa.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'If I weren''t so afraid of flying, I would have accepted that job offer in London two years ago.', 'Se eu não tivesse tanto medo de voar, eu teria aceitado aquela oferta de emprego em Londres há dois anos.', 'Característica permanente e presente (o medo) impedindo uma decisão específica no passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'If I had studied harder, I would have a better job.', 'If I had studied harder, I would have a better job now.', 'Embora a estrutura verbal esteja correta, é importante incluir marcadores temporais como ''now'' ou ''today'' quando a frase mistura tempos, para deixar claro que a consequência é presente e não passada, evitando ambiguidade com o terceiro condicional completo.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'If I had more money, I would have bought that house last year.', 'If I had had more money, I would have bought that house last year.', 'Quando a condição também se refere ao passado (ter tido dinheiro naquele momento específico), é necessário usar o past perfect completo ''had had'', e não apenas o past simple, que indicaria uma condição presente.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'If she was more careful, she would avoid the accident yesterday.', 'If she were more careful, she would have avoided the accident yesterday.', 'A consequência se refere a um evento específico do passado (o acidente de ontem), então deve-se usar ''would have + particípio'', e não ''would + verbo base'', que é usado para consequências no presente ou futuro.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'If I would have known, I would help you now.', 'If I had known, I would help you now.', 'A oração condicional (if-clause) nunca usa ''would have''; ela deve usar ''had + particípio'' (past perfect) para expressar a condição hipotética do passado. O ''would have'' é reservado apenas para a oração de consequência quando ela também se refere ao passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'condicionais_mistos';

-- Subjuntivo (C1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'O subjuntivo em inglês aparece em dois contextos principais: depois de verbos e expressões de sugestão, exigência ou necessidade, quando o verbo subordinado fica na forma base sem flexão, e em situações hipotéticas ou contrárias ao fato, quando se usa were no lugar de was e estruturas com wish e if only para expressar desejo ou lamentação.', 'O subjuntivo em inglês é um modo verbal que expressa algo que não é um fato consumado, mas sim uma sugestão, uma exigência, um desejo ou uma situação hipotética e contrária à realidade. Ao contrário do português, que tem um subjuntivo rico em terminações próprias e muito usado no dia a dia, o inglês moderno preserva apenas resquícios desse modo, e por isso muitos brasileiros deixam de reconhecê-lo ou tentam traduzir a estrutura portuguesa palavra por palavra, o que quase sempre soa estranho ou errado.

O primeiro uso importante é o chamado subjuntivo presente, também chamado de mandative subjunctive. Ele aparece depois de verbos que expressam sugestão, exigência, recomendação ou insistência, como suggest, recommend, insist, demand, request, propose, e depois de expressões como it is essential that, it is important that, it is necessary that. A regra é que o verbo da oração subordinada fica na forma base, sem flexão de terceira pessoa e sem marcação de tempo, independentemente do sujeito. Isso significa que dizemos he suggest, e não he suggests, mesmo com he. Essa forma é praticamente idêntica em todas as pessoas, inclusive na negativa, em que se usa not antes do verbo, sem o auxiliar do: I insist that she not go, e não I insist that she doesn''t go.

O segundo uso é o subjuntivo em situações hipotéticas, mais conhecido pela estrutura do segundo condicional e por frases com wish e if only. Aqui a marca mais famosa é o uso de were no lugar de was para todas as pessoas, especialmente na expressão if I were you. Esse were subjuntivo indica que a situação é imaginária, contrária ao fato presente, e contrasta com o was do passado real ou de perguntas sobre fatos verdadeiros. Em registro mais informal, was ainda aparece bastante com I, he, she, it em condicionais hipotéticos, mas were é considerado mais correto e é a forma preferida em contextos formais e na escrita cuidada.

O terceiro uso, ligado ao segundo, aparece depois de wish e if only para expressar desejo sobre algo que não é verdade no presente ou lamentação sobre o passado. Para o presente, usa-se o passado simples do verbo, com were no lugar de was; para o passado, usa-se had plus particípio passado, formando o passado perfeito. Essa estrutura é puramente gramatical, não indica tempo real, apenas o grau de distância da realidade.

É importante entender que, no inglês contemporâneo, sobretudo no americano, o mandative subjunctive é mais comum e mais natural do que se imagina, aparecendo com frequência até em fala cotidiana depois de verbos como suggest e recommend. Já no inglês britânico, é mais comum evitar esse subjuntivo e usar should mais infinitivo, como em I suggest that he should go, embora a forma com subjuntivo puro também seja aceita e até considerada mais elegante em registro formal.' FROM modulo WHERE codigo = 'subjuntivo';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'If I were you, I would apologize.', 'Se eu fosse você, eu pediria desculpas.', 'Were é usado com I porque a situação é hipotética; is a forma clássica do subjuntivo em condicionais irreais.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'The teacher suggested that he study harder.', 'O professor sugeriu que ele estudasse mais.', 'Study fica sem o ''s'' final mesmo com he, porque é o subjuntivo presente depois de suggest.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'It is essential that every employee be informed of the changes.', 'É essencial que todo funcionário seja informado das mudanças.', 'Be, e não is, porque a expressão it is essential that exige o subjuntivo presente.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'I wish I had more time to finish this project.', 'Eu gostaria de ter mais tempo para terminar este projeto.', 'Had, no passado simples, expressa desejo sobre uma situação presente que não é verdadeira.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'If only she had told me the truth earlier.', 'Se ao menos ela tivesse me contado a verdade antes.', 'Had told, no passado perfeito, expressa lamentação sobre algo que não aconteceu no passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'The committee demanded that the report not be released until Monday.', 'O comitê exigiu que o relatório não fosse divulgado até segunda-feira.', 'A negativa do subjuntivo usa not antes do verbo, sem o auxiliar do; be, não is, porque o sujeito é the report.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'I suggest that he studies harder.', 'I suggest that he study harder.', 'Depois de suggest, o verbo da oração subordinada fica na forma base, sem o ''s'' de terceira pessoa, porque é o subjuntivo presente, não o presente simples comum.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'If I was you, I would call her.', 'If I were you, I would call her.', 'Em condicionais hipotéticos e em registro cuidado, usa-se were para todas as pessoas, mesmo com I, he, she, it, marcando que a situação é irreal.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'It is important that she doesn''t miss the meeting.', 'It is important that she not miss the meeting.', 'A negativa do subjuntivo presente é formada apenas com not antes do verbo, sem o auxiliar does, porque a construção não segue as regras do presente simples comum.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'I wish I have more free time.', 'I wish I had more free time.', 'Depois de wish, para expressar desejo sobre o presente, usa-se o passado simples, e não o presente, porque a estrutura indica que a situação atual não é como se gostaria.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'subjuntivo';

-- Inversão (C1)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Advérbios e expressões negativas ou restritivas como never, rarely, seldom, hardly, no sooner e not only, quando colocados no início da frase para dar ênfase, exigem inversão: auxiliar antes do sujeito, como em perguntas, inserindo do/does/did se necessário.', 'Em inglês, quando uma frase começa com um advérbio ou expressão de sentido negativo ou restritivo, a ordem normal de sujeito e verbo se inverte, como acontece nas perguntas. Esse recurso é chamado de inversão e serve para dar ênfase, criar um efeito mais formal ou mais dramático à frase. Em vez de dizer ''I have never seen such a mess'', um falante mais formal ou literário pode dizer ''Never have I seen such a mess'', colocando o advérbio negativo no início e invertendo o sujeito com o auxiliar.

A regra prática é a seguinte: quando a frase começa com uma expressão negativa ou restritiva como never, rarely, seldom, hardly, scarcely, no sooner, not only, under no circumstances, little, on no account, in no way, o restante da oração segue a estrutura de pergunta, ou seja, auxiliar mais sujeito mais verbo principal. Se não houver auxiliar na frase original, é necessário inserir do, does ou did, exatamente como fazemos nas perguntas comuns.

Esse tipo de inversão é típico de registro formal, escrito, discursos, textos literários ou jornalísticos, e também aparece em fala quando se quer soar mais enfático ou dramático. Em conversas informais do dia a dia, os falantes tendem a preferir a ordem normal, sem inversão, guardando essa construção para contextos que pedem um tom mais elevado ou impactante.

Algumas expressões pedem atenção especial por causa da estrutura correlativa que criam, como ''no sooner... than'' e ''hardly... when'', usadas para descrever duas ações que aconteceram em sequência muito rápida. Nesses casos, a inversão ocorre na primeira oração, logo após a expressão negativa, e a segunda oração continua na ordem normal.

Vale notar que a inversão só ocorre quando o advérbio negativo está no início da frase, com função enfática. Se o mesmo advérbio aparecer em posição normal, no meio ou perto do verbo principal, a ordem sujeito-verbo permanece inalterada, como em ''I have rarely seen such dedication'', sem inversão.' FROM modulo WHERE codigo = 'inversao';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'Never have I seen such dedication.', 'Nunca vi tanta dedicação.', 'O advérbio ''never'' no início exige a inversão do auxiliar ''have'' com o sujeito ''I''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'Rarely does she complain about her job.', 'Raramente ela reclama do trabalho.', 'Como não há auxiliar na frase original, é necessário inserir ''does'' antes do sujeito.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'Hardly had we arrived when the storm began.', 'Mal tínhamos chegado quando a tempestade começou.', '''Hardly... when'' descreve duas ações em sequência rápida; a inversão ocorre logo após ''hardly''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'No sooner had he sat down than the phone rang.', 'Mal ele tinha se sentado quando o telefone tocou.', 'Estrutura correlativa ''no sooner... than'', com inversão apenas na primeira oração.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'Not only did they win the match, but they also broke the record.', 'Eles não só venceram a partida, como também bateram o recorde.', '''Not only'' no início pede o auxiliar ''did'' antes do sujeito, mesmo em frase afirmativa no passado.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'Under no circumstances should you share your password with strangers.', 'Em hipótese alguma você deve compartilhar sua senha com estranhos.', 'Expressão restritiva formal ''under no circumstances'' provoca inversão com o modal ''should''.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'Never I have seen such a thing.', 'Never have I seen such a thing.', 'O aluno mantém a ordem normal sujeito-verbo por influência do português; em inglês, após ''never'' no início, o auxiliar vem antes do sujeito.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'Rarely she goes to the gym.', 'Rarely does she go to the gym.', 'Quando a frase original não tem auxiliar, é preciso inserir ''do/does/did'' para permitir a inversão, assim como em perguntas.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'No sooner he arrived than it started to rain.', 'No sooner had he arrived than it started to rain.', 'Falta o auxiliar ''had'' antes do sujeito; a expressão ''no sooner'' exige inversão completa com auxiliar mais sujeito.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'I have never seen such dedication -> Never I have seen such dedication.', 'Never have I seen such dedication.', 'Ao mover o advérbio negativo para o início por ênfase, o aluno esquece de inverter também o auxiliar com o sujeito, mantendo a ordem normal por engano.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'inversao';

-- Expressões idiomáticas (C2)
INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
SELECT id, 'Em C2, dominar expressões idiomáticas significa escolher a expressão certa para o registro e a nuance exatos do contexto, respeitar as restrições gramaticais fixas dessas unidades e saber, sobretudo, quando é mais elegante não usar nenhuma.', 'Em nível C2, o desafio não é mais entender expressões idiomáticas isoladas, mas dominar o registro em que cada uma se encaixa e usá-las com a naturalidade de um falante nativo educado. Uma expressão idiomática é uma combinação de palavras cujo sentido não se deduz da soma literal dos termos: ''kick the bucket'' não fala de chutar balde nenhum, significa morrer, geralmente em tom informal ou de humor negro. O que separa um falante avançado de um nativo é a sensibilidade para saber quando uma expressão idiomática é apropriada, quando soa forçada, datada, regional demais ou incompatível com o tom do texto.

Expressões idiomáticas carregam registro embutido. Algumas são coloquiais e soariam deslocadas em um relatório formal, como ''it''s raining cats and dogs''. Outras são de registro neutro a formal e aparecem naturalmente em jornalismo ou discurso político, como ''to weather the storm'' ou ''to be at a crossroads''. Há ainda expressões marcadamente britânicas, americanas ou datadas, e usá-las fora de contexto geográfico ou geracional pode soar artificial, como se o falante tivesse decorado uma lista de um livro didático antigo.

Um ponto crucial em C2 é reconhecer que expressões idiomáticas quase nunca são intercambiáveis com sinônimos literais sem perda de nuance. ''To let the cat out of the bag'' e ''to spill the beans'' significam ambos revelar um segredo, mas o primeiro sugere revelação acidental ou prematura, o segundo costuma implicar confissão voluntária, às vezes sob pressão. Escolher a expressão certa é uma questão de precisão semântica, não apenas de vocabulário.

Outro aspecto avançado é a flexibilidade morfológica limitada dessas expressões. Muitas não aceitam passivização, mudança de tempo verbal livre ou inserção de advérbios no meio, porque são unidades fixas armazenadas como blocos. Dizer ''the beans were spilled by him'' é gramaticalmente possível mas soa estranho porque a expressão perde a naturalidade coloquial que a define. Um falante C2 sabe manipular essas restrições intuitivamente, sem forçar a estrutura.

Por fim, o uso hábil de expressões idiomáticas em C2 inclui saber quando NÃO usá-las. Em contextos acadêmicos, jurídicos ou diplomáticos, o excesso de idiomatismo pode soar informal demais ou até deselegante. A maestria real está em dosar: usar a expressão certa, no momento certo, para o efeito retórico certo, seja para criar cumplicidade, humor, ênfase ou economia expressiva.' FROM modulo WHERE codigo = 'expressoes_idiomaticas';

INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 1, 'He really let the cat out of the bag about the surprise party.', 'Ele realmente deixou escapar o segredo sobre a festa surpresa.', 'Revelação acidental ou prematura de um segredo, geralmente sem intenção maliciosa.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 2, 'After hours of interrogation, he finally spilled the beans.', 'Depois de horas de interrogatório, ele finalmente confessou tudo.', 'Sugere revelação sob pressão ou após resistência, diferente de um deslize acidental.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 3, 'The company managed to weather the storm during the recession.', 'A empresa conseguiu superar a crise durante a recessão.', 'Registro neutro a formal, comum em textos de economia e negócios; sugere resiliência frente a adversidade prolongada.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 4, 'She''s been burning the candle at both ends preparing for the trial.', 'Ela está se esgotando trabalhando dia e noite para se preparar para o julgamento.', 'Expressão que descreve esforço insustentável, geralmente com conotação de preocupação por parte de quem fala.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 5, 'The negotiations reached a crossroads, and both sides had to reconsider their strategy.', 'As negociações chegaram a uma encruzilhada, e ambos os lados tiveram que reconsiderar sua estratégia.', 'Registro formal, típico de jornalismo político e diplomático; indica momento decisivo sem viés coloquial.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
SELECT c.id, 6, 'I wouldn''t touch that deal with a bargepole; it reeks of fraud.', 'Eu não tocaria nesse acordo nem a pau; tem cheiro de fraude.', 'Expressão britânica informal e vívida, transmite forte rejeição; soaria deslocada em registro formal americano.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';

INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 1, 'The beans were spilled by the intern during the meeting.', 'The intern spilled the beans during the meeting.', 'Expressões idiomáticas fixas resistem à passivização porque são armazenadas como blocos coloquiais; forçar a voz passiva quebra a naturalidade e soa como tradução literal em vez de fala nativa.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 2, 'He kicked, unfortunately, the bucket last year.', 'He kicked the bucket last year, unfortunately.', 'Inserir advérbios no meio de uma expressão idiomática fixa desfaz a unidade fraseológica; advérbios devem ficar fora do bloco idiomático, geralmente no início ou no fim da frase.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 3, 'In the official report, we can say the project is raining cats and dogs of problems.', 'In the official report, we can say the project is fraught with problems.', 'Usar uma expressão idiomática marcadamente coloquial ou até sem sentido fora do seu uso fixo (aqui distorcida) em um contexto formal quebra o registro esperado; em relatórios oficiais prefira expressões idiomáticas de registro neutro a formal ou linguagem direta.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';
INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
SELECT c.id, 4, 'She let the cat out of the bag on purpose to sabotage the plan.', 'She spilled the beans on purpose to sabotage the plan.', '''Let the cat out of the bag'' carrega a nuance de revelação acidental ou não premeditada; para uma ação deliberada e intencional, ''spill the beans'' ou ''gave it away deliberately'' são escolhas semanticamente mais precisas.' FROM conteudo_do_modulo c
  JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = 'expressoes_idiomaticas';

