# Contexto do projeto

Frontend web (React) de um app de aprendizado de inglês estruturado por níveis (nos
moldes do Cambridge/CEFR), mas com o conteúdo decidido por um agente que gera
desafios sob medida a partir dos erros do usuário. Não é um app de "bater papo com
IA" — é um currículo adaptativo, e a interface precisa deixar essa progressão visível
e clara.

## Estrutura de telas

- **Onboarding:** cadastro/login → diagnóstico de nível (conversa curta com o agente,
  não múltipla escolha) → objetivo (viagem, trabalho, conversação geral) → ritmo
  (minutos por dia, tipo de correção)
- **Lista de módulos:** agrupada por nível CEFR (A1 a C2), cada módulo mostrando um
  indicador colorido (vermelho/amarelo/verde) e a nota de 0 a 10 ao lado do nome;
  módulo ainda não tentado aparece como "novo", sem cor de nota. Lista densa, linhas
  separadas por borda fina — não cards arredondados soltos.
- **Desafio:** tela onde o desafio gerado aparece — pode ser conversa, resposta
  escrita, ou (fase 2) resposta em áudio. A correção aparece só ao final da sessão,
  resumida, nunca interrompendo o meio da resposta.
- **Sidebar de temas:** nove cenas de vida real — conversação livre, viagem, trabalho,
  cultura e expressões, comida e restaurante, compras e serviços, saúde e bem-estar,
  vida social, casa e rotina. Funciona como contexto pro desafio, não como currículo
  em si (o currículo é a lista de módulos por nível). No celular a lista sai da tela:
  o rodapé é só navegação.
- **Progresso:** visão geral de quantos módulos estão em cada faixa de cor, e quais
  conceitos mais precisam de atenção agora.
- **Configurações:** objetivo, ritmo, tipo de correção.

## Sistema visual de nota

- Vermelho abaixo de 6, amarelo de 6 a 8,9, verde de 9 a 10 — sem lacuna entre as
  faixas.
- Indicador: um ponto colorido pequeno + a nota numérica ao lado do nome do módulo.
- Módulo sem nota ainda mostra "novo" em cinza, no lugar da nota.

## Identidade visual

- Cor de destaque: amarelo mostarda (`#F2B705`)
- Fundo neutro claro (branco ou quase-branco), texto em quase-preto (`#1A1A1A`)
- Cantos arredondados, tipografia grande e amigável — visual de SaaS indie moderno
  (referência de estilo: AbacatePay), evitando o clichê verde/branco de app de idioma
  e o azul genérico de app corporativo
- Instale e use a skill `apple-design` do repositório `emilkowalski/skills` para
  decisões de animação e movimento (`npx skills@latest add emilkowalski/skills`)

## Stack técnica

- React, consumindo a API REST do backend Spring Boot (pasta separada)
- Sem áudio no MVP, mas o componente de tela de desafio já deve prever múltiplos
  formatos de resposta (texto hoje, áudio depois) sem precisar ser refeito

## Regras de código

- Nomes de variável, componente e comentários em **português** (ex: `ListaDeModulos`,
  `notaDoModulo`)
- Mensagens de commit também em português, claras e específicas (nunca "fix" ou
  "update" genéricos)

## Controle de versão

- Inicialize um repositório git nesta pasta (`frontend/`) se ainda não existir
- Crie um `.gitignore` adequado para projeto React/Node (`node_modules/`, `.env`,
  `build/`, etc.)
- Crie um `README.md` explicando o projeto, como rodar localmente, e quais telas já
  estão implementadas
- Trabalhe em commits pequenos e organizados por etapa (ex: "estrutura inicial do
  projeto React", "lista de módulos com indicador de nota", "tela de desafio") —
  nunca um commit gigante com tudo junto
- Atualize o README a cada etapa concluída

Comece propondo a estrutura de pastas e componentes, com foco na lista de módulos e
na tela de desafio (as duas telas centrais do MVP), antes de implementar o resto.