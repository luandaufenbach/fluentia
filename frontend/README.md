# Agente de Inglês — frontend

Interface web do currículo adaptativo de inglês. Não é um app de "bater papo com IA": é um
currículo por nível CEFR em que o agente decide o próximo desafio a partir dos erros do usuário,
e a interface existe para deixar essa progressão visível.

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
| **Currículo** (`TelaDeModulos`) | ✅ | Lista densa agrupada de A1 a C2, com nota e faixa de cor por módulo, módulos bloqueados mostrando de que dependem, e a sugestão do orquestrador no topo com o motivo da escolha |
| **Desafio** (`TelaDeDesafio`) | ✅ | Cena + enunciado, campo de resposta e correção resumida ao final |
| **Progresso** (`TelaDeProgresso`) | ✅ | Quantos módulos em cada faixa e quais conceitos precisam de atenção agora |
| **Configurações** (`TelaDeConfiguracoes`) | ✅ | Objetivo, ritmo e tipo de correção |
| **Sidebar de temas** | ✅ | Navegação e lista de temas como contexto da cena |
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
ela **é** o currículo, então a progressão precisa ser legível de cima a baixo.

Todos os tokens ficam em [`src/estilos/tokens.css`](src/estilos/tokens.css). Espaçamentos em `rem`
para que a fonte maior do sistema aumente o layout junto, em vez de quebrá-lo.

### Movimento

Decisões seguindo a skill `apple-design` (`npx skills@latest add emilkowalski/skills`):

- Feedback no **pointer-down**, não na soltura — a pressão precisa ser instantânea
- Curva sem overshoot (`cubic-bezier(0.2, 0, 0.2, 1)`, 320 ms), equivalente a uma mola
  criticamente amortecida. O overshoot fica reservado para gesto com momento, que esta
  interface não tem
- `prefers-reduced-motion` troca deslocamento por fade curto, sem eliminar o feedback

A skill recomenda molas em vez de transições CSS para o que é **dirigido por gesto**; como não há
arrastar, deslizar nem sheet aqui, CSS dá conta. Se entrar gesto (fase 2 de áudio, por exemplo),
o caminho é uma biblioteca de mola com velocidade herdada.

## Estrutura

```
src/
  componentes/    IndicadorDeNota, ListaDeModulos, CampoDeResposta,
                  PainelDeCorrecao, SidebarDeTemas
  telas/          TelaDeModulos, TelaDeDesafio, TelaDeProgresso, TelaDeConfiguracoes
  servicos/       api.ts — cliente tipado da API REST
  estilos/        tokens.css, global.css
  tipos.ts        contratos espelhando o backend
```

### Preparado para áudio na fase 2

`CampoDeResposta` decide a entrada pelo campo `formato` do desafio (`TEXTO`, `CONVERSA`, `AUDIO`).
Entrar com áudio é adicionar um ramo nesse componente — `TelaDeDesafio` não muda.

### Correção só ao final

A correção aparece apenas depois do envio, nunca durante a escrita: nota da resposta, cada erro
com trecho errado → correção e explicação, e a nota do módulo já recalculada.

## Convenções

Nomes de componente, variável e comentário em **português**. Mensagens de commit também,
organizadas por etapa.
