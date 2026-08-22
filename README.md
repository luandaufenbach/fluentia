# Fluentia

Trilha adaptativa de inglês. **Não é um chat com IA**: é um percurso por nível CEFR em que o
app ensina o conceito, cobra com exercícios gerados na hora e decide o próximo passo a partir
dos erros reais de quem está estudando.

## O loop que sustenta o produto

```
orquestrador escolhe o conceito + a cena
        ↓
agente gerador cria um desafio inédito
        ↓
aluno responde
        ↓
agente avaliador detecta o erro específico
        ↓
nota do conceito é recalculada
        ↓
(volta ao topo — a nota nova já muda a próxima escolha)
```

Cada peça tem lógica de decisão de verdade. O orquestrador não sorteia: segue uma ordem de
prioridade explícita — reforçar o que está em vermelho, avançar para o que nunca foi praticado,
consolidar o que ainda não está verde, revisar o que está há mais tempo parado.

## O que existe hoje

| | |
|---|---|
| **Trilha em 5 fases** | Os 16 conceitos de A1 a C2 desenhados como percurso, com a promessa do que cada fase destrava. A cor de cada nó vem da nota real, não de um checkbox |
| **Ensina antes de cobrar** | Cada conceito tem material próprio: explicação, exemplos com tradução e os erros que brasileiro costuma cometer |
| **Nota que decai** | Média ponderada por recência, com esquecimento exponencial — a nota cai sozinha sem prática |
| **Pré-requisitos** | Um conceito só abre quando os anteriores saem do vermelho |
| **Dois modos** | Simulado (custo zero) e com IA, atrás da mesma interface |

## Como rodar

**Pré-requisitos:** Docker e JDK 21.

```bash
cd backend && docker compose --profile completo up -d
```

```bash
cd frontend && npm install && npm run dev
```

Abre em `http://localhost:5173`. Sem chave de API configurada, os agentes simulados rodam o
loop inteiro sem custo nenhum.

Detalhes de cada lado em [`backend/README.md`](backend/README.md) e
[`frontend/README.md`](frontend/README.md).

## Stack

Java 21 + Spring Boot 4 + Postgres no backend, React + TypeScript + Vite no frontend,
Docker para empacotar. Migrations com Flyway, 46 testes incluindo integração contra o
banco de verdade.

## Convenções

Nomes de classe, componente, variável e comentário em **português**. Mensagens de commit
também, organizadas por etapa.
