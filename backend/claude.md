# Contexto do projeto

App web de aprendizado de inglês que funciona como um currículo estruturado
adaptativo — inspirado na organização por níveis do Cambridge/CEFR, mas com o
conteúdo e o ritmo decididos por um agente com base nos erros reais do usuário, não
um curso fixo igual pra todo mundo.

## A ideia central — não é um chatbot de conversação solta

O produto não é "converse com uma IA". É um motor que:

1. Organiza o conteúdo num mapa de conceitos por nível (A1 a C2), nos moldes do CEFR.
2. Testa o usuário com desafios gerados na hora (nunca reaproveitados) mirando o
   próximo conceito a reforçar.
3. Avalia a resposta, detecta o erro específico e atualiza uma nota de domínio do
   módulo/conceito correspondente.
4. Usa essa nota pra decidir automaticamente o próximo desafio — gerando um desafio
   novo, numa cena diferente, sempre que um conceito precisa de reforço.

Esse loop — desafio, resposta, avaliação, atualização de domínio, novo desafio — é o
coração do produto. Sem o agente gerando conteúdo dinamicamente e mantendo esse
estado por usuário, o produto não existe. Não trate isso como um chat comum: cada
peça acima precisa de lógica de decisão real, não só geração de texto.

## Estrutura de conteúdo — dois eixos independentes

- **Conceitos (o "o quê"):** organizados por nível CEFR, cada um é um módulo com nota
  própria. Progressão de exemplo: A1 → verbo "to be", artigos, presente simples,
  pronomes. A2 → passado simples, comparativos, "there is/are". B1 → presente
  perfeito, condicionais básicos, phrasal verbs comuns. B2 → passado perfeito, voz
  passiva, condicionais mistos. C1/C2 → subjuntivo, inversão, expressões idiomáticas
  avançadas.
- **Temas (o "onde"):** a cena/contexto que envolve o desafio — conversação livre,
  viagem, trabalho, cultura e expressões, inglês pra dev (diferencial, não o módulo
  principal). O tema dá a roupagem; o conceito é o que está sendo avaliado.
- Um módulo (conceito) só é liberado quando os pré-requisitos dele têm nota razoável —
  preserva a progressão por nível sem travar o usuário numa ordem 100% fixa.

## Sistema de nota por módulo

- Cada módulo tem uma nota de 0 a 10, calculada como média ponderada dos últimos
  desafios daquele módulo, com mais peso pros mais recentes — a nota cai com o tempo
  se o usuário não pratica, refletindo esquecimento real em vez de ficar presa num
  valor antigo.
- Faixas de cor: vermelho abaixo de 6, amarelo de 6 a 8,9, verde de 9 a 10 (sem
  lacuna entre as faixas).
- Módulo ainda não tentado aparece como "novo", sem nota.

## Arquitetura de agentes

- **Orquestrador:** decide, a cada sessão, qual módulo/conceito precisa de reforço e
  qual tema/cena usar como contexto.
- **Agente gerador de desafio:** cria um desafio novo (nunca reaproveitado) mirando o
  conceito escolhido, dentro do tema/cena escolhido — pode ser conversacional,
  escrito ou (fase 2) de áudio.
- **Agente avaliador:** analisa a resposta do usuário, detecta o erro específico, e
  atualiza a nota de domínio do conceito correspondente.
- **Agente conversador:** conduz sessões de conversação livre, focadas em fluência e
  captura natural de erro, não num desafio fechado.

No MVP, comece com um único módulo completo (ex.: "verbo to be") rodando o loop
inteiro — desafio, resposta, avaliação, nota — antes de expandir pra outros módulos.
Provar que o loop funciona de ponta a ponta com um conceito só importa mais do que
ter muitos módulos incompletos.

## Sessões com áudio (fase 2 — não implementar ainda)

- Desafios de gramática/vocabulário: texto ou conversação.
- Desafios de pronúncia/escuta: áudio ("ouça e responda") — só entram quando o agente
  avaliador detecta que o problema é de pronúncia, não de estrutura.
- Deixe a arquitetura pronta pra receber esse tipo de desafio depois: o formato do
  desafio já deve ser um campo no modelo de dados, mesmo que só "texto" seja usado no
  início.

## Stack técnica

- Java 21 LTS + Spring Boot 3.x
- Padrão Controller-Service-Repository por domínio (ex: `ModuloController`,
  `DesafioService`, `UsuarioService`)
- Spring AI para integração com a API da Claude — Claude Sonnet para o agente gerador
  de desafio e o agente avaliador (a qualidade da correção é o coração do produto),
  Claude Haiku para tarefas simples
- Postgres + JPA/Hibernate — aqui a persistência já entra no MVP, ao contrário de um
  chat comum, porque a nota por módulo é o dado central do produto
- Docker para empacotar a aplicação

## Modelagem de dados — pontos de atenção

- Usuário tem uma nota por módulo/conceito (tabela relacionando usuário, módulo,
  nota, data da última prática)
- Cada desafio gerado fica registrado no histórico: qual conceito mirava, qual tema
  usou, e o resultado da avaliação
- Esse histórico é o que alimenta tanto o cálculo da nota quanto a decisão do
  orquestrador sobre o próximo desafio

## Roadmap de onboarding (refletir nos endpoints)

1. Cadastro/login
2. Diagnóstico de nível — conversa curta guiada pelo agente, não prova de múltipla
   escolha
3. Definição de objetivo (viagem, trabalho, dev, conversação geral)
4. Configuração de ritmo (minutos por dia, tipo de correção)
5. Primeira sessão real (linha de base de erros e nota inicial dos módulos)
6. Endpoint de dashboard: sugere o próximo desafio com base na nota mais baixa e no
   objetivo do usuário

## Escopo do MVP — comece por aqui

- Só o módulo "verbo to be" (nível A1), com o loop completo funcionando: desafio
  gerado → resposta do usuário → avaliação → nota atualizada → novo desafio
- Pode simular orquestrador + gerador + avaliador num fluxo dentro do mesmo service,
  desde que a lógica de decisão (qual desafio gerar a seguir, com base em quê) já
  exista de verdade — não simplifique isso
- Nota do módulo e histórico de desafios precisam ser salvos no banco desde o início

## Regras de código

- Nomes de variável, classe, método e comentários em **português** (ex:
  `ServicoDesafio`, `calcularNotaDoModulo()`)
- Mensagens de commit também em português, claras e específicas (nunca "fix" ou
  "update" genéricos)

## Controle de versão

- Inicialize um repositório git nesta pasta (`backend/`) se ainda não existir
- Crie um `.gitignore` adequado para Java/Spring/Maven ou Gradle (`target/`,
  `.idea/`, `*.class`, `application-local.yml`, etc.)
- Crie um `README.md` explicando o que é o projeto, como rodar localmente, e o que já
  está implementado
- Trabalhe em commits pequenos e organizados por etapa (ex: "estrutura inicial do
  projeto Spring Boot", "modelo de dados de módulo e nota", "loop de desafio do
  módulo to be") — nunca um commit gigante com tudo junto
- Atualize o README a cada etapa concluída

Comece propondo a estrutura de pastas do projeto, o `pom.xml` (ou `build.gradle`), e
o modelo de dados de módulo/nota/histórico de desafio antes de escrever qualquer
lógica de agente.