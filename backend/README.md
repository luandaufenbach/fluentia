# Fluentia — backend

Backend do Fluentia, app de aprendizado de inglês que funciona como **trilha adaptativa**:
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

## Ensinar antes de cobrar

Cada módulo tem material de estudo próprio (`ConteudoDoModulo`): um resumo, a explicação em
parágrafos, de 4 a 6 exemplos com tradução e os erros que brasileiro costuma cometer naquele
conceito — estes últimos no mesmo formato *errado → certo* usado pela correção do avaliador,
para que ler antes e errar depois falem a mesma língua.

O conteúdo é **por módulo, não por desafio**: o desafio muda toda vez, a regra do *to be* não.

Quando o aluno estuda um conceito e pede para praticá-lo, `/api/desafios/proximo?modulo=codigo`
gera o desafio daquele conceito em vez de deixar a escolha com o orquestrador — estudar uma
coisa e ser cobrado em outra é o caminho mais curto para o aluno desistir. Se havia um desafio
em aberto de outro módulo, ele é **descartado**: como nunca foi respondido, não existe avaliação
e a média de nenhum módulo se altera.

## Segurança

Até a V6 a API respondia para qualquer um que alcançasse a porta, com um usuário fixo.
A V7 fecha isso. As decisões e o porquê de cada uma:

| Decisão | Por quê |
|---|---|
| **Sessão em cookie `HttpOnly`**, não token no navegador | Token em `localStorage` é legível por qualquer script: uma falha de XSS entrega a credencial. Sessão no servidor também pode ser revogada na hora |
| **Negar por padrão** (`anyRequest().authenticated()` por último) | Endpoint novo nasce protegido. Esquecer de proteger uma rota deixa de ser possível por omissão |
| **BCrypt custo 12**, hash com prefixo `{bcrypt}` | ~250 ms por verificação: imperceptível no login, proibitivo em escala. O prefixo permite trocar de algoritmo sem invalidar senha já cadastrada |
| **CSRF ligado** | Com credencial em cookie, o navegador a envia sozinho até em requisição disparada por outro site. O token quebra isso porque o site atacante não consegue lê-lo |
| **Mesma resposta para conta inexistente e senha errada** | Distinguir os casos entrega uma lista de e-mails válidos de graça |
| **Hash falso verificado quando a conta não existe** | Sem isso, a diferença de tempo entregaria a mesma lista. Medido: 253 ms contra 261 ms |
| **Bloqueio temporário após 5 falhas** | Permanente transformaria tentativa de invasão em negação de serviço contra o dono da conta |
| **Trilha de auditoria** | "Quem entrou nesta conta e quando?" precisa ter resposta. Guarda o evento, nunca o segredo |
| **Conta semeada sem senha e inativa** | Conta com credencial conhecida no repositório é porta dos fundos para quem clonar o projeto |

### Três controles que estavam escritos e não funcionavam

Configuração de segurança é o tipo de código que parece certo lendo e está errado
rodando. Estes três passaram na revisão de código e só caíram ao serem exercitados:

1. **O bloqueio por tentativas nunca disparava.** A recusa lança exceção, a exceção
   desfaz a transação, e a transação desfeita levava junto o incremento do contador. A
   conta ficava eternamente na "primeira" tentativa. Corrigido movendo contador e
   auditoria para `RegistroDeSeguranca`, em transação própria.
2. **A auditoria de falhas era desfeita pelo mesmo motivo** — a trilha só guardava os
   acessos bem-sucedidos, justamente os que menos interessam numa investigação.
   `@Transactional(REQUIRES_NEW)` **só vale em bean separado**: em chamada de um método
   para outro da mesma classe a anotação não tem efeito nenhum.
3. **`maximumSessions(1)` não tinha efeito.** A estratégia de concorrência do Spring
   roda dentro do filtro de autenticação, e o login aqui acontece em endpoint próprio.
   Marcar as sessões como expiradas pelo `SessionRegistry` também não bastou. Resolvido
   em `SessoesAtivas`, invalidando a sessão diretamente.

Cada um virou teste em `SegurancaDaApiIT`.

### Isolamento entre contas

`ServicoDeUsuario.usuarioAtual()` é a única porta de entrada para "de quem são estes
dados": nenhum endpoint aceita identificador de usuário vindo do cliente. Trocar o
número na URL para ler o progresso de outra pessoa não é possível porque não há número
na URL para trocar. `IsolamentoEntreContasIT` prova isso em cinco frentes — desafio,
nota, preferência, histórico e resposta de desafio alheio.

A conta é reconferida no banco a cada requisição, e não lida da sessão: desativar uma
conta tem efeito na requisição seguinte, sem esperar a sessão expirar.

### Limites conhecidos

- **Uma instância só.** O mapa de sessões ativas vive na memória do processo. Com mais
  de uma instância, cada uma derruba apenas as sessões que ela abriu. A saída é sessão
  compartilhada (Spring Session com Redis).
- **O cadastro revela se um e-mail já existe.** Inevitável sem envio de e-mail: não dá
  para cadastrar dois iguais. Mitigar exige confirmação por e-mail.
- **`COOKIE_SEGURO` precisa ser `true` em produção.** Falso no local porque o navegador
  descarta cookie `secure` servido por `http://localhost`.
- **Atrás de proxy reverso**, configure `ForwardedHeaderFilter` com a lista de proxies
  confiáveis. A auditoria usa o endereço da conexão e ignora `X-Forwarded-For` de
  propósito: esse cabeçalho é escrito pelo cliente e pode ser forjado.
- **`/api/diagnostico` exige papel de administrador** e ainda não há como criar um. A
  promoção é por SQL, deliberadamente: é um endpoint que expõe configuração.

## A trilha em fases

O nível CEFR é a verdade técnica do conteúdo, mas "A2" não diz nada para quem está
começando. A tabela `fase` traduz isso na promessa concreta do que se destrava, e é esse
agrupamento que a tela principal mostra:

| Fase | Nível | Marco |
|---|---|---|
| Primeiras frases | A1 | Se apresentar e falar da sua rotina sem travar |
| Contar o que aconteceu | A2 | Contar como foi o seu fim de semana |
| Destravar a conversa | B1 | Sustentar uma conversa sobre o dia a dia |
| Precisão | B2 | Participar de uma reunião sem perder o fio |
| Naturalidade | C1/C2 | Soar natural, não apenas correto |

O marco de uma fase é alcançado quando **todos** os módulos dela saem do vermelho — o mesmo
limite que libera o módulo seguinte. Seria incoerente destravar o próximo conceito e ainda
assim dizer que este não foi vencido.

## Custo por ciclo

Cada chamada grava tokens, modelo e custo na tabela `consumo_de_api`, e `GET /api/consumo`
devolve o extrato da conta. Os números abaixo saíram de lá, contra a API de verdade:

| Chamada | Entrada | Saída | Custo |
|---|---|---|---|
| Geração (lote de 2) | 1.032 | 472 | US$ 0,0102 → **US$ 0,0051 por desafio** |
| Correção **detalhada** | 1.168 | 750 | **US$ 0,0148** |
| Correção **resumida** | 1.175 | 150 | **US$ 0,0058** |

Um desafio respondido custa cerca de **US$ 0,020** com correção detalhada e **US$ 0,011** com
correção resumida — a preferência do aluno derruba 61% da conta, porque o peso está na saída e
o token de saída custa cinco vezes o de entrada.

> Uma medição anterior estimava US$ 0,0071 por ciclo. Estava errada: ela contou a entrada com o
> endpoint de contagem de tokens e supôs a saída. O avaliador roda com raciocínio adaptativo, e
> o raciocínio é cobrado como saída — 750 tokens onde a estimativa supunha 250. Medir a entrada
> e supor a saída subestima justamente a metade cara.

### O que reduz o custo

1. **Lote de desafios por chamada.** Dos tokens de entrada da geração, 666 são custo fixo —
   instrução, dados do módulo e esquema do JSON — que se repetiria a cada desafio. Os
   excedentes ficam com status `NA_FILA` e chegam ao aluno sem chamada nenhuma: 58 ms contra 8 s.
2. **Lote menor na primeira visita ao módulo** (`desafios-por-lote-inicial`). O lote só se paga
   se o aluno voltar àquele conceito. No pior caso — alguém que passa uma vez por cada um dos
   16 módulos — o lote cheio geraria 80 desafios para usar 16. Começar por dois corta esse
   desperdício e mantém a divisão do custo fixo para quem fica.
3. **Lista anti-repetição de 20 para 6 enunciados**, truncados em 90 caracteres. Ela sozinha
   custava 943 tokens. O que garante a não repetição é o histórico gravado no banco, não o
   tamanho desta lista.
4. **A preferência "tipo de correção"**, que é o maior controle isolado, como a tabela mostra.

`modelo-de-geracao` é separado de `modelo-de-raciocinio` para permitir baixar só o gerador de
nível (`MODELO_DE_GERACAO=claude-haiku-4-5`) sem tocar na qualidade da correção.

Modelo sem preço em `precos-por-milhao-de-tokens` tem os tokens gravados e o custo marcado como
**desconhecido**, nunca como zero: o extrato traz esse modelo em `modelosSemPreco` para o total
não ser lido como se estivesse completo.

## O dia do aluno

`GET /api/hoje` junta as três coisas que dão ritmo. Nenhuma delas guarda estado próprio:
tudo é derivado do histórico de avaliações e das notas já gravadas. Uma tabela de "sessão do
dia" seria mais um estado para dessincronizar de um histórico que já é a verdade.

| | |
|---|---|
| **Meta do dia** | Sai do ritmo escolhido em Configurações: 15 min ÷ 3 min por desafio = 5. Piso de 3 e teto de 20 — meta de um desafio não é sessão, e de trinta ninguém cumpre |
| **Sequência de dias** | **Um desafio conta o dia.** Exigir a sessão inteira transformaria a sequência num segundo cobrador, e quem tem quinze minutos ruins perderia semanas de constância — justamente o hábito que ela deveria proteger |
| **Revisão espaçada** | Conceitos que estão caindo por tempo parado, do que mais caiu para o que menos caiu. Quem mudou de faixa vem antes: ali a queda pode ter fechado o módulo seguinte sem o aluno errar nada |

Nota presumida pelo nivelamento não entra na revisão: sem data de prática ela não decai, e
mandar revisar o que nunca foi medido inventaria um esquecimento que não aconteceu.

**A sequência não quebra à meia-noite.** Quem praticou ontem e ainda não praticou hoje mantém
a sequência viva — o dia não acabou. Zerar às 00h01 puniria o relógio, não a falta de prática.

### Fuso horário

Os dois containers rodam em `America/Sao_Paulo` (`TZ` no compose, `PGTZ` no banco). As colunas
são `TIMESTAMP` sem fuso: com a aplicação num fuso e o banco em outro, um `DEFAULT NOW()` grava
três horas à frente do que o Java grava na linha ao lado, e perto da meia-noite isso muda o dia
— o que quebraria a sequência sozinha.

**Limite conhecido:** um fuso para todos. Público fora dele pede fuso por conta.

## Nivelamento de entrada

Antes disto todo mundo começava em A1 — e quem já sabe inglês abandona na primeira tela ao ser
mandado traduzir "eu sou brasileiro".

São **cinco perguntas abertas** em ordem crescente de exigência (A1 → C1), não múltipla escolha:
o que interessa é o que a pessoa **produz**, que é exatamente o que o app vai cobrar depois.
Pular é um botão de primeira classe, porque parar numa pergunta é o sinal mais limpo de onde
está o teto — e a pergunta pulada continua gravada, marcada como pulada.

**Uma única chamada de API**, no fim, com a conversa inteira. Julgar resposta por resposta
custaria cinco chamadas e daria um veredito pior: o nível aparece no conjunto, não numa frase
isolada. Medido: 1.073 tokens de entrada, 1.195 de saída, **US$ 0,021** — uma vez por conta.

O prompt manda **escolher o menor nível na dúvida**, e nível em branco ou fora do quadro CEFR
vira A1. Errar para baixo custa alguns minutos; errar para cima faz a pessoa desistir.

### O que o nível estimado muda de verdade

Os módulos de nível **abaixo** do estimado recebem nota **7,00**, e é isso que transforma a
estimativa em ponto de partida: sem essa nota o pré-requisito de cada módulo continuaria
pendente, e a pessoa cairia em A1 de novo com um nível bonito escrito no perfil e nenhum efeito
na trilha.

Sete, e não dez, porque nada disso foi **provado** aqui — é presunção, não resultado. Sete passa
do limite de liberação (seis) e ainda aparece em **amarelo**, dizendo corretamente que o conceito
não foi demonstrado. Como a quantidade de práticas fica em zero e não há data de prática, essa
nota também **não sofre o decaimento por esquecimento**: não faz sentido "esquecer" o que ainda
não foi medido. Nota já existente nunca é sobrescrita — prática de verdade vale mais que
presunção, mesmo quando a presunção é mais alta.

### Duas conversas ao mesmo tempo

`iniciar` **trava a linha da conta** antes de procurar um nivelamento aberto. Sem a trava, duas
requisições simultâneas — duas abas, um duplo clique, o modo estrito do React chamando o efeito
duas vezes — passam juntas pelo "já existe?" e as duas tentam inserir; a segunda morre no índice
único e vira erro na tela de quem clicou uma vez só. Aconteceu na primeira execução pelo
navegador, antes da trava.

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

Há dois modos, e a diferença entre eles importa para quem for mexer no código.

### Modo 1 — tudo em container

Sobe banco e aplicação juntos. É o modo de "só quero usar":

```bash
docker compose --profile completo up -d --build
```

O `--build` é obrigatório depois de qualquer mudança em código: sem ele o compose reaproveita a
imagem antiga e a alteração simplesmente não aparece.

### Modo 2 — banco em container, aplicação na IDE

É o modo de desenvolvimento. Só o Postgres sobe em container:

```bash
docker compose up -d banco
```

A aplicação roda pela IDE ou pelo Maven, com as variáveis do `.env` exportadas antes:

```bash
set -a; . ./.env; set +a; ./mvnw spring-boot:run
```

**No IntelliJ:** abra a pasta `backend` (a que tem o `pom.xml`, não a raiz do repositório) e rode
`AgenteInglesApplication`. Como a IDE não lê o `.env` sozinha, aponte o arquivo em *Run →
Edit Configurations → Environment variables → Environment file*, ou cole as variáveis ali à mão.

**No VS Code:** com as extensões *Extension Pack for Java* e *Spring Boot Extension Pack*, o botão
Run aparece sobre `AgenteInglesApplication`. O `.env` entra pelo `launch.json`:

```json
{ "type": "java", "name": "Fluentia backend",
  "request": "launch", "mainClass": "br.com.agenteingles.AgenteInglesApplication",
  "projectName": "agente-ingles", "envFile": "${workspaceFolder}/backend/.env" }
```

A API sobe em `http://localhost:8080` nos dois modos. O usuário semeado pelas migrations nasce
**inativo e sem senha**: para usar o app, cadastre-se pela tela.

O frontend é independente e não precisa ser reiniciado por causa do backend:

```bash
cd ../frontend && npm run dev
```

### Ligando os agentes reais

A chave **nunca** vai para o repositório. Copie o exemplo e preencha o `.env`, que já está no
`.gitignore` e é lido automaticamente pelo docker compose:

```bash
cp .env.exemplo .env
```

> Preencha o `.env`, não o `.env.exemplo` nem o `docker-compose.yml` — o exemplo é versionado
> (uma chave colada nele vaza no próximo commit) e o valor do compose é só o padrão de quando a
> variável **não existe**: `USAR_CLAUDE=false` no `.env` vence `${USAR_CLAUDE:-true}` no compose.

Depois de mudar `USAR_CLAUDE` ou a chave, a aplicação **precisa ser reiniciada**: as variáveis são
lidas uma vez, na subida, e a escolha entre agente simulado e agente real é feita na montagem do
contexto do Spring.

```bash
docker compose --profile completo up -d
```

Sem `--build`, porque só o ambiente mudou; o compose recria o container com as variáveis novas.
Rodando pela IDE, basta parar e rodar de novo. Reiniciar **não apaga nada**: o banco vive num
volume separado, com o histórico e as notas intactos.

Para conferir o que está no ar:

```bash
docker exec agente-ingles-aplicacao printenv USAR_CLAUDE
```

`GET /api/diagnostico` responde qual implementação de agente está ativa — deve trazer
`GeradorDeDesafioComClaude` e `AvaliadorComClaude`. Ele exige perfil de administrador e informa
apenas **se** a chave foi lida, nunca o valor dela.

Para pegar a chave: console.anthropic.com → Settings → API keys → Create Key. É preciso adicionar
créditos em Plans & Billing (a API é pré-paga e separada da assinatura do Claude.ai).

### Gerando o conteúdo de ensino

O material de estudo de cada módulo é gerado uma vez pela Claude e vira **migration
versionada** — não é chamado em tempo de uso, então a API fica desligada no dia a dia:

```bash
set -a; . ./.env; set +a; ./mvnw spring-boot:run -Dspring-boot.run.profiles=gerar-conteudo
```

A rotina pula módulos que já têm conteúdo no banco e escreve só o que faltou, num arquivo
`V{próximo}__conteudo_dos_modulos.sql`. Editar esse SQL à mão é o caminho esperado para
corrigir uma explicação: ele é a fonte da verdade do material, não a chamada de API que o
produziu.

> O prompt dessa rotina é escrito **com acentuação**, ao contrário do resto dos comentários
> do projeto. A primeira versão veio sem acento e o modelo espelhou o estilo, devolvendo os
> 16 módulos de material didático sem acentuação nenhuma.

#### Leitura da resposta estruturada

`LeitorDeRespostaEstruturada` existe porque o `.entity()` do Spring AI quebra em dois casos que
aconteceram de verdade contra a API: com raciocínio adaptativo a resposta vem em mais de um bloco
(o de raciocínio, vazio, vem primeiro), e o modelo às vezes abre uma cerca de código ` ``` ` sem
fechá-la. Nos dois casos o sintoma é o mesmo erro enganoso de "end-of-input". O leitor junta os
blocos com texto e recorta o objeto JSON, em vez de depender de o modelo obedecer à instrução de
não usar markdown.

## Testes

```bash
./mvnw test
```

65 testes. Os de integração exigem o Postgres no ar e usam um **banco próprio**
(`agente_ingles_teste`), porque a suíte grava de verdade pela camada HTTP e não pode sujar os
dados locais.

| Classe | O que cobre |
|---|---|
| `LeitorDeRespostaEstruturadaTest` | Os formatos de resposta reais da Claude: cerca de código aberta, bloco de raciocínio vazio e texto em volta do JSON |
| `ConteudoPelaApiIT` | Conteúdo de ensino pela camada HTTP, e a garantia de que **todo** módulo tem material |
| `ServicoDeNotaTest` | Peso por recência, janela de avaliações, decaimento e limites das faixas |
| `AvaliadorSimuladoTest` | Faixas de nota e detecção do erro de concordância |
| `GeradorDeDesafioSimuladoTest` | Não repetição de enunciados e reforço dirigido |
| `LoopDoDesafioIT` | Loop completo contra o Postgres, incluindo desbloqueio por pré-requisito |
| `DesafioPelaApiIT` | O loop pela camada HTTP, **sem** transação de teste em volta |
| `SegurancaDaApiIT` | Negar por padrão, CSRF, cabeçalhos, não revelar contas, bloqueio e sessão única |
| `IsolamentoEntreContasIT` | Uma conta não alcança desafio, nota, preferência nem histórico da outra |

A distinção entre as duas últimas é deliberada: com uma transação aberta pelo teste, associações
lazy continuam carregando e um vazamento de entidade JPA para fora do serviço passa despercebido.

> O Surefire ignora arquivos terminados em `IT` por padrão (sufixo reservado ao Failsafe). Aqui os
> testes de integração são os mais importantes do projeto, então o `pom.xml` os inclui
> explicitamente — sem isso, um `./mvnw test` passaria verde sem executá-los.

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| `GET` | `/api/trilha` | O percurso em fases, com promessa, marco e progresso de cada uma |
| `GET` | `/api/modulos` | Módulos agrupados por nível CEFR, com nota, faixa e bloqueio |
| `GET` | `/api/modulos/{codigo}/conteudo` | Material de estudo: explicação, exemplos e erros comuns |
| `GET` | `/api/desafios/proximo` | Desafio da vez. `?modulo=codigo` pratica o conceito recém-estudado |
| `POST` | `/api/desafios/{id}/resposta` | Avalia, grava o histórico e devolve a correção |
| `GET` | `/api/desafios/historico` | Desafios recentes |
| `GET` | `/api/progresso` | Quantos módulos em cada faixa e o que precisa de atenção |
| `GET` | `/api/dashboard/sugestao` | O que o orquestrador faria agora, sem gerar o desafio |
| `GET` | `/api/temas` | Temas disponíveis |
| `GET` `PUT` | `/api/usuario`, `/api/usuario/preferencias` | Perfil, objetivo, ritmo e tipo de correção |
| `GET` | `/api/diagnostico` | Qual implementação de agente está ativa e se a chave foi lida |

A resposta de referência do desafio **não** é exposta pela API — seria entregar o gabarito.
Há um teste garantindo isso.

## O que já está implementado

- [x] Modelo de dados completo: usuário, módulo com pré-requisitos, tema, nota, histórico e erros
- [x] 16 conceitos semeados de A1 a C2, com a cadeia de pré-requisitos
- [x] Cálculo da nota com peso por recência e decaimento por esquecimento
- [x] Orquestrador com prioridade explícita e rotação de tema
- [x] Loop completo: desafio → resposta → avaliação → nota → novo desafio
- [x] Agentes simulados e com Claude atrás da mesma interface — o caminho com Claude validado
      contra a API real (geração, avaliação e nota fechando o loop)
- [x] Endpoints REST de trilha, conteúdo, desafio, progresso, sugestão e preferências
- [x] Empacotamento Docker e banco de testes separado
- [x] Conteúdo de ensino dos 16 módulos: o app ensina antes de cobrar
- [x] Prática dirigida: praticar justamente o conceito que acabou de ser estudado

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
