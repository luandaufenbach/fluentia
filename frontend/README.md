# Fluentia — frontend

Interface web do Fluentia, trilha adaptativa de inglês. Não é um app de "bater papo com IA":
é uma trilha por nível CEFR em que o app **ensina o conceito** e depois cobra, e o agente
decide o próximo desafio a partir dos erros reais — a interface existe para deixar essa
progressão visível.

## Como rodar localmente

**Pré-requisito:** o backend rodando em `http://localhost:8080` (veja `../backend/README.md`).

```bash
npm install
```

```bash
npm run dev
```

Abre em `http://localhost:5173`. O Vite faz proxy de `/api` para o backend, então não há CORS
nem variável de ambiente de URL para configurar em desenvolvimento.

```bash
npm run build
```

## Telas implementadas

| Tela | Estado | O que faz |
|---|---|---|
| **Trilha** (`TelaDaTrilha`) | ✅ | O percurso inteiro em fases, cada uma com a promessa do que destrava e o marco que a fecha. Cada nó é um módulo real: a cor vem da nota, não de um clique. Clicar abre o conteúdo |
| **Conteúdo** (`TelaDeConteudo`) | ✅ | O material de estudo do módulo: resumo, explicação em parágrafos, exemplos com tradução e os erros comuns. Daqui sai o botão para começar os exercícios |
| **Desafio** (`TelaDeDesafio`) | ✅ | Cena + enunciado, campo de resposta e correção resumida ao final. Traz o contador da sessão, o fechamento do dia e o aviso de erro repetido. No desktop, o lembrete do conteúdo fica ao lado |
| **Progresso** (`TelaDeProgresso`) | ✅ | Quantos módulos em cada faixa e quais conceitos precisam de atenção agora |
| **Configurações** (`TelaDeConfiguracoes`) | ✅ | Objetivo, ritmo, tipo de correção e o caminho para refazer o nivelamento |
| **Painel do dia** (`PainelDoDia`) | ✅ | No topo da trilha: quanto falta para fechar hoje, há quantos dias seguidos você aparece e o que o tempo está derrubando. Sem pontos, sem ligas |
| **Barra lateral** | ✅ | Navegação e lista de temas como contexto da cena |
| **Entrada** (`TelaDeAutenticacao`) | ✅ | Cadastro e login. A senha não vai para estado global nem armazenamento local: o que fica é o cookie de sessão, fora do alcance de JavaScript |
| **Nivelamento** (`TelaDeNivelamento`) | ✅ | A conversa curta de entrada: cinco perguntas abertas em ordem crescente, com "não sei esta" como botão de primeira classe. O resultado abre a trilha no nível certo, em vez de mandar todo mundo para A1. Objetivo e ritmo, passos 3 e 4 do onboarding, estão em Configurações |

## Sistema visual de nota

Um ponto colorido pequeno com a nota numérica ao lado do nome do módulo:

| Faixa | Intervalo | Cor |
|---|---|---|
| Vermelho | abaixo de 6 | `#D1414A` |
| Amarelo | 6 a 8,9 | `#D99000` |
| Verde | 9 a 10 | `#2F8F5B` |
| Novo | módulo nunca praticado | cinza, exibindo "novo" no lugar da nota |

Sem lacuna entre as faixas: 5,99 é vermelho, 6,00 é amarelo, 8,99 é amarelo, 9,00 é verde.

## Identidade visual

Amarelo mostarda `#F2B705` como destaque, fundo quase-branco `#FBFAF7` e texto quase-preto
`#1A1A1A`. Cantos arredondados e tipografia grande, com a fonte do sistema — que já traz ajuste
óptico e tabelas de tracking próprias.

### A trilha como percurso

A tela principal desenha o caminho, não uma lista: uma espinha tracejada, as fases ao longo
dela e os conceitos pendurados. A linguagem visual vem dos roadmaps de aprendizado; a
tipografia e a paleta continuam sendo as do app, sem virar outro produto no meio.

Três decisões que a diferenciam de um roadmap comum:

- **O estado vem da nota, não de um clique.** Em roadmap de checkbox o aluno marca o que
  quiser; aqui a cor de cada nó é a faixa real do módulo, e o marco da fase só fecha quando
  todos os conceitos dela saem do vermelho. Não dá para se enganar.
- **Cada fase promete algo concreto.** "A2" não motiva ninguém; "contar como foi o seu fim de
  semana" motiva. O nível CEFR continua sendo a verdade técnica por trás, mas quem lê a tela
  vê a habilidade.
- **"Você está aqui"** marca a fase em andamento — já encostada e ainda não fechada.

Todos os tokens ficam em [`src/estilos/tokens.css`](src/estilos/tokens.css). Espaçamentos em `rem`
para que a fonte maior do sistema aumente o layout junto, em vez de quebrá-lo.

### Layout

O conteúdo é centralizado na coluna ao lado da barra, com largura máxima de `78rem`
(`--largura-do-conteudo`). Antes ele era limitado a `64rem` e ficava colado à esquerda, o que
deixava uma faixa morta em telas grandes.

A partir de `68rem` as telas viram duas colunas, e a segunda tem função — não é preenchimento:

| Tela | Coluna principal | Segunda coluna |
|---|---|---|
| Trilha | fases e conceitos | medidor de progresso e a sugestão do próximo passo |
| Conteúdo | explicação e erros comuns | exemplos, fixos na rolagem, ao alcance do olho |
| Desafio | enunciado e resposta | lembrete do conteúdo e o "voltar para o conteúdo" |

Abaixo disso tudo empilha em coluna única. Verificado em 375, 768 e 1440 px: sem rolagem
horizontal e sem elemento estourando.

### Movimento

Decisões seguindo a skill `apple-design` (`npx skills@latest add emilkowalski/skills`):

- Feedback no **pointer-down**, não na soltura — a pressão precisa ser instantânea
- **Molas** via `motion`, não duração fixa: a entrada da correção e a da tela de conteúdo usam
  `spring` com `bounce: 0`. Sendo mola, a animação é interrompível — uma correção que chega
  enquanto a anterior ainda entra assume da posição e velocidade atuais, em vez de reiniciar
- `bounce: 0` porque nada aqui é dirigido por gesto; overshoot fica reservado para movimento
  com momento, que esta interface não tem
- `MotionConfig reducedMotion="user"` em `main.tsx` faz as molas respeitarem a preferência do
  sistema, do mesmo jeito que o `@media prefers-reduced-motion` faz com as transições CSS
- Alvos de toque com no mínimo `2.75rem` (44 px) — `--alvo-de-toque`, aplicado à navegação e a
  todos os botões

## Estrutura

```
src/
  componentes/    IndicadorDeNota, CampoDeResposta, PainelDeCorrecao,
                  ResumoDoConteudo, BarraLateral
  telas/          TelaDaTrilha, TelaDeConteudo, TelaDeDesafio,
                  TelaDeProgresso, TelaDeConfiguracoes
  servicos/       api.ts — cliente tipado da API REST
  estilos/        tokens.css, global.css
  tipos.ts        contratos espelhando o backend
```

### Ensinar antes de cobrar

O caminho principal é **estudar e depois praticar**: clicar num módulo da trilha abre o conteúdo
dele, e o botão de começar os exercícios pede o desafio **daquele mesmo conceito**
(`/api/desafios/proximo?modulo=codigo`). Quem já sabe o conceito pula direto pelo botão
secundário da sugestão.

Dentro do desafio, `ResumoDoConteudo` mantém o essencial à vista e oferece o caminho de volta ao
material inteiro: travar no meio de uma resposta e ter que sair da tela para reler a regra é onde
o aluno desiste.

`conteudo` não é item de menu porque depende de um módulo escolhido — chega-se a ele pela trilha
ou pelo desafio, nunca do nada.

### Correção só ao final

A correção aparece apenas depois do envio, nunca durante a escrita: nota da resposta, cada erro
com trecho errado → correção e explicação, e a nota do módulo já recalculada.

## Convenções

Nomes de componente, variável e comentário em **português**. Mensagens de commit também,
organizadas por etapa.

## Áudio

Nos dois sentidos, pelo navegador — sem serviço de fala, sem custo por minuto e sem a voz do
aluno saindo da máquina dele. `src/audio/vozDoNavegador.ts` concentra a Web Speech API.

| | |
|---|---|
| **Ouvir** (`BotaoDeOuvir`) | Fala o inglês dos exemplos do conteúdo e das correções. Velocidade em 0,92 — a padrão atropela quem está aprendendo |
| **Falar** (`CampoDeResposta`) | O que o reconhecimento entende vira texto no campo, **editável antes de enviar** |

### Falar não envia direto

O transcrito entra no campo e o aluno confere. Mandar direto puniria a pessoa por falha do
reconhecimento — e é o aluno que leva a nota, não o microfone. A tela diz isso enquanto ouve.

### O botão de ouvir some sem voz em inglês

`lang = "en-US"` **não garante** voz em inglês. Numa máquina que só tem vozes em português — o
caso comum de um Windows brasileiro — o navegador aceita a marcação e lê o inglês com a voz
portuguesa. Num app de idioma isso é pior do que não ter áudio: ensina a pronúncia errada com a
autoridade de um botão oficial.

Por isso o botão só aparece quando existe uma voz `en*` de verdade, e a voz é escolhida
explicitamente em vez de deixada para o navegador resolver pelo `lang`.

Para instalar no Windows: *Configurações → Hora e Idioma → Fala → Gerenciar vozes → Adicionar*.

As vozes carregam de forma assíncrona: na primeira renderização a lista vem vazia, então o botão
também reage ao evento `voiceschanged`. Sem isso ele nunca apareceria, mesmo para quem tem voz.

### O que ainda não existe

Desafio em que **o enunciado é falado** (`formato = AUDIO`, "ouça e responda"). O campo já está
no modelo de dados e a tela já trata o formato, mas o gerador ainda não produz esse tipo de
desafio — ele precisaria de um texto para ouvir separado do enunciado escrito.
