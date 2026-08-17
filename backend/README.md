# Agente de Inglês — backend

Backend de um app de aprendizado de inglês que funciona como **currículo adaptativo**:
o conteúdo é organizado por nível CEFR (A1 a C2), mas o que praticar a seguir é decidido
por um agente a partir dos erros reais do usuário — não é um curso fixo igual para todo mundo,
e não é um chat de conversação solta.

## O loop que sustenta o produto

```
orquestrador escolhe módulo + tema
        ↓
agente gerador cria um desafio inédito
        ↓
usuário responde
        ↓
agente avaliador detecta o erro específico
        ↓
nota do módulo é recalculada
        ↓
(volta ao topo — a nota nova já muda a próxima escolha)
```

Cada peça tem lógica de decisão de verdade. O orquestrador não sorteia: segue uma ordem de
prioridade explícita, descrita abaixo.

## Como o agente decide

**Escolha do módulo** (`Orquestrador`), em ordem:

1. Módulo liberado em **vermelho** (nota abaixo de 6) — reforço urgente, o mais fraco primeiro
2. Módulo liberado **nunca praticado** — avança o currículo na ordem do nível
3. Módulo liberado **ainda não consolidado** (abaixo de 9) — fecha a lacuna
4. Tudo consolidado — **revisão** do que está há mais tempo sem prática

**Escolha do tema:** vem do objetivo do usuário (viagem, trabalho, dev, conversação geral).
Se o desafio anterior do mesmo módulo já usou esse tema, o orquestrador troca de cena — o
conceito se repete, a roupagem não.

**Liberação por pré-requisito:** um módulo só abre quando todos os pré-requisitos têm nota ≥ 6.
Fica no limite do amarelo de propósito: o usuário avança sem precisar zerar o conceito anterior,
mas não destrava um nível novo carregando um conceito em vermelho.

## Cálculo da nota

Duas etapas separadas (`ServicoDeNota`):

1. **Nota gravada** — média ponderada das últimas 8 avaliações, com a mais recente pesando mais
   (fator de recência 0,65). É sempre recalculada a partir do histórico gravado, nunca a partir
   da nota anterior, para média e banco nunca divergirem.
2. **Decaimento por esquecimento** — aplicado *na leitura*: após 3 dias de tolerância a nota cai
   exponencialmente, com meia-vida de 30 dias. Como é calculado na leitura, a nota cai sozinha
   com o tempo sem precisar de rotina agendada reescrevendo o banco.

Faixas de cor, sem lacuna entre elas: **vermelho** abaixo de 6, **amarelo** de 6 a 8,9,
**verde** de 9 a 10. Módulo nunca praticado aparece como **novo**, sem nota.

## Stack

| Item | Versão / escolha |
|---|---|
| Java | 21 LTS (Temurin) |
| Spring Boot | 4.0.7 |
| Spring AI | 2.0.0 (`spring-ai-starter-model-anthropic`) |
| Banco | Postgres 16 + JPA/Hibernate, schema via Flyway |
| Empacotamento | Docker multi-stage |

> **Desvio do especificado:** o `CLAUDE.md` pede Spring Boot 3.x, mas o Spring Initializr não
> oferece mais 3.x e o Spring AI 2.0 é justamente a linha que integra com o Boot 4. Ficamos em
> Boot 4.0.7 + Spring AI 2.0.0, que é o par suportado.

## Os agentes

Dois contratos, duas implementações cada, escolhidas pela propriedade `agente-ingles.usar-claude`:

| Contrato | Simulado (padrão) | Com Claude |
|---|---|---|
| `AgenteGeradorDeDesafio` | Combina cena do tema com alvo do conceito, filtrando os já usados | `claude-sonnet-5` |
| `AgenteAvaliador` | Compara com a referência e detecta concordância errada de *to be* | `claude-sonnet-5` com raciocínio adaptativo |

O simulado roda o **loop inteiro sem custo de API** e produz nota e erro específico de verdade —
o suficiente para validar a mecânica antes de ligar a chave.

O modelo mais capaz vai no gerador e no avaliador porque a qualidade da correção é o coração do
produto: a nota que sai do avaliador alimenta a média do módulo e a decisão do orquestrador, então
um erro de correção se propaga por todo o currículo do usuário. `claude-haiku-4-5` fica reservado
para tarefas simples (configurado em `modelo-simples`, ainda sem uso).

## Como rodar localmente

**Pré-requisitos:** JDK 21 e Docker.

```bash
docker compose up -d banco
```

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. O usuário de desenvolvimento (`dev@agenteingles.local`)
já vem semeado pelas migrations — a autenticação ainda não existe, e os serviços já recebem o
usuário resolvido, então ligar login depois não muda a assinatura de nada abaixo disso.

### Rodando tudo em container

```bash
docker compose --profile completo up -d --build
```

### Ligando os agentes reais

A chave **nunca** vai para o repositório: vem da variável de ambiente.

```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

```bash
USAR_CLAUDE=true ./mvnw spring-boot:run
```

Para pegar a chave: console.anthropic.com → Settings → API keys → Create Key. É preciso adicionar
créditos em Plans & Billing (a API é pré-paga e separada da assinatura do Claude.ai).

## Testes

```bash
./mvnw test
```

31 testes. Os de integração exigem o Postgres no ar e usam um **banco próprio**
(`agente_ingles_teste`), porque a suíte grava de verdade pela camada HTTP e não pode sujar os
dados locais.

| Classe | O que cobre |
|---|---|
| `ServicoDeNotaTest` | Peso por recência, janela de avaliações, decaimento e limites das faixas |
| `AvaliadorSimuladoTest` | Faixas de nota e detecção do erro de concordância |
| `GeradorDeDesafioSimuladoTest` | Não repetição de enunciados e reforço dirigido |
| `LoopDoDesafioIT` | Loop completo contra o Postgres, incluindo desbloqueio por pré-requisito |
| `DesafioPelaApiIT` | O loop pela camada HTTP, **sem** transação de teste em volta |

A distinção entre as duas últimas é deliberada: com uma transação aberta pelo teste, associações
lazy continuam carregando e um vazamento de entidade JPA para fora do serviço passa despercebido.

> O Surefire ignora arquivos terminados em `IT` por padrão (sufixo reservado ao Failsafe). Aqui os
> testes de integração são os mais importantes do projeto, então o `pom.xml` os inclui
> explicitamente — sem isso, um `./mvnw test` passaria verde sem executá-los.

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| `GET` | `/api/modulos` | Currículo agrupado por nível CEFR, com nota, faixa e bloqueio |
| `GET` | `/api/desafios/proximo` | Desafio da vez (reaproveita o que está em aberto) |
| `POST` | `/api/desafios/{id}/resposta` | Avalia, grava o histórico e devolve a correção |
| `GET` | `/api/desafios/historico` | Desafios recentes |
| `GET` | `/api/progresso` | Quantos módulos em cada faixa e o que precisa de atenção |
| `GET` | `/api/dashboard/sugestao` | O que o orquestrador faria agora, sem gerar o desafio |
| `GET` | `/api/temas` | Temas disponíveis |
| `GET` `PUT` | `/api/usuario`, `/api/usuario/preferencias` | Perfil, objetivo, ritmo e tipo de correção |

A resposta de referência do desafio **não** é exposta pela API — seria entregar o gabarito.
Há um teste garantindo isso.

## O que já está implementado

- [x] Modelo de dados completo: usuário, módulo com pré-requisitos, tema, nota, histórico e erros
- [x] 16 conceitos semeados de A1 a C2, com a cadeia de pré-requisitos
- [x] Cálculo da nota com peso por recência e decaimento por esquecimento
- [x] Orquestrador com prioridade explícita e rotação de tema
- [x] Loop completo: desafio → resposta → avaliação → nota → novo desafio
- [x] Agentes simulados e com Claude atrás da mesma interface
- [x] Endpoints REST de currículo, desafio, progresso, sugestão e preferências
- [x] Empacotamento Docker e banco de testes separado

## O que ainda não está

- [ ] Autenticação (hoje há um usuário de desenvolvimento fixo)
- [ ] Diagnóstico de nível guiado por conversa (passo 2 do onboarding)
- [ ] Agente conversador para sessões de conversação livre
- [ ] Banco de desafios do simulado para os outros 15 módulos (hoje só *verbo to be*
      tem alvos próprios; os demais saem genéricos no modo simulado)
- [ ] Desafios de áudio — o campo `formato` já existe no modelo de dados (`TEXTO`,
      `CONVERSA`, `AUDIO`), então a fase 2 entra sem alterar a estrutura

## Convenções

Nomes de classe, método, variável e comentário em **português**. Mensagens de commit também,
organizadas por etapa.
