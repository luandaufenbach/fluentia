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
| **Trilha** (`TelaDeModulos`) | ✅ | Lista densa agrupada de A1 a C2, com nota e faixa de cor por módulo, módulos bloqueados mostrando de que dependem, e a sugestão do orquestrador no topo. Clicar num módulo abre o conteúdo dele |
| **Conteúdo** (`TelaDeConteudo`) | ✅ | O material de estudo do módulo: resumo, explicação em parágrafos, exemplos com tradução e os erros comuns. Daqui sai o botão para começar os exercícios |
| **Desafio** (`TelaDeDesafio`) | ✅ | Cena + enunciado, campo de resposta e correção resumida ao final. No desktop, o lembrete do conteúdo fica ao lado, com "voltar para o conteúdo" |
| **Progresso** (`TelaDeProgresso`) | ✅ | Quantos módulos em cada faixa e quais conceitos precisam de atenção agora |
| **Configurações** (`TelaDeConfiguracoes`) | ✅ | Objetivo, ritmo e tipo de correção |
| **Barra lateral** | ✅ | Navegação e lista de temas como contexto da cena |
| **Onboarding** | ⬜ | Cadastro/login e diagnóstico de nível ainda não existem — o backend usa um usuário de desenvolvimento fixo. Objetivo e ritmo, que são os passos 3 e 4, já estão em Configurações |

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

A lista de módulos é densa, com linhas separadas por borda fina, não cards arredondados soltos:
ela **é** a trilha, então a progressão precisa ser legível de cima a baixo.

Todos os tokens ficam em [`src/estilos/tokens.css`](src/estilos/tokens.css). Espaçamentos em `rem`
para que a fonte maior do sistema aumente o layout junto, em vez de quebrá-lo.

### Layout

O conteúdo é centralizado na coluna ao lado da barra, com largura máxima de `78rem`
(`--largura-do-conteudo`). Antes ele era limitado a `64rem` e ficava colado à esquerda, o que
deixava uma faixa morta em telas grandes.

A partir de `68rem` as telas viram duas colunas, e a segunda tem função — não é preenchimento:

| Tela | Coluna principal | Segunda coluna |
|---|---|---|
| Trilha | níveis A1 a C2 | continuação dos níveis, para não virar uma rolagem longa |
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
  componentes/    IndicadorDeNota, ListaDeModulos, CampoDeResposta,
                  PainelDeCorrecao, ResumoDoConteudo, BarraLateral
  telas/          TelaDeModulos, TelaDeConteudo, TelaDeDesafio,
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

### Preparado para áudio na fase 2

`CampoDeResposta` decide a entrada pelo campo `formato` do desafio (`TEXTO`, `CONVERSA`, `AUDIO`).
Entrar com áudio é adicionar um ramo nesse componente — `TelaDeDesafio` não muda.

### Correção só ao final

A correção aparece apenas depois do envio, nunca durante a escrita: nota da resposta, cada erro
com trecho errado → correção e explicação, e a nota do módulo já recalculada.

## Convenções

Nomes de componente, variável e comentário em **português**. Mensagens de commit também,
organizadas por etapa.
