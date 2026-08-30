# Publicar numa plataforma (Render, Cloud Run)

> **Instalação em uso:** projeto `fluentia-ingles`, serviço `fluentia` em `us-central1`,
> banco no Neon em `us-east-2` (Ohio). No ar em
> <https://fluentia-268391932069.us-central1.run.app>.
>
> As variáveis de ambiente ficam num arquivo YAML passado com `--env-vars-file`, e não em
> `--set-env-vars`: a lista separada por vírgula quebra em valores com vírgula ou igual, e a
> `BANCO_URL` tem query string. **Esse arquivo substitui o conjunto inteiro a cada deploy** —
> variável posta por comando avulso some no deploy seguinte.

O outro caminho é o [README](README.md): máquina própria com Caddy fazendo o HTTPS. Este
aqui é para plataformas que hospedam o container por você — sem servidor para manter,
sem certificado para renovar, e de graça.

A diferença técnica é uma só, e ela explica todo o resto: **a plataforma termina o HTTPS
e espera um processo só**. Então o Caddy sai de cena e o Spring passa a servir o
frontend junto com a API, a partir do `Dockerfile` na raiz do repositório.

Um efeito colateral bem-vindo: com a página e a API na mesma origem, o cookie de sessão
deixa de ser cookie de terceiro e o CORS deixa de existir.

## Cloud Run ou Render?

O mesmo `Dockerfile` roda nos dois. A escolha não é técnica — é sobre quem pode te
cobrar.

| | Google Cloud Run | Render |
|---|---|---|
| **CPU** | 1 vCPU de verdade | 0.1 vCPU |
| **Java sobe bem?** | Sim | Talvez — a JVM precisa de muito mais CPU no arranque do que em regime |
| **Cartão** | Exigido | Não pede |
| **Pode gerar cobrança?** | **Sim, se passar da cota** | **Não. É teto rígido: bateu o limite, o serviço para** |
| **Acorda em** | ~15 s | ~1 min |

**Para este app, Cloud Run é a escolha melhor.** A diferença de CPU é justamente onde
Java sofre.

**A única razão real para preferir o Render** é que ele não consegue te cobrar. A cota
gratuita do Cloud Run é um desconto numa conta que tem cartão: se você passar, vem
fatura. O Render simplesmente para.

## Primeiro, o banco (vale para os dois)

Veja [O banco: Neon](#o-banco-neon) logo abaixo e tenha as três variáveis em mãos antes
de criar o serviço. Nos dois caminhos o banco é externo.

## Cloud Run, passo a passo

Ordem importa: o banco existe antes do serviço, e a `URL_BASE` só pode ser preenchida
depois do primeiro deploy, porque é ele quem revela o endereço.

### 1. A conta do Google

Cloud Run **exige cartão**, mesmo dentro da cota gratuita. A cota é um desconto numa
conta que pode faturar, não um teto: passou, cobra. Quem não quer cartão de jeito
nenhum fica no Render, que simplesmente para.

### 2. O CLI

Instalador em <https://cloud.google.com/sdk/docs/install>, ou:

```powershell
winget install Google.CloudSDK
```

### 3. Projeto e APIs

O identificador é único no Google inteiro, então `fluentia` sozinho não passa:

```bash
gcloud auth login
gcloud projects create fluentia-SEU-SUFIXO --name=Fluentia
gcloud config set project fluentia-SEU-SUFIXO
```

Ligue o faturamento no console (**Billing → Link a billing account**) e habilite o que
o deploy usa:

```bash
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com secretmanager.googleapis.com
```

### 4. Os segredos

Três valores nunca podem ir no comando: `ANTHROPIC_API_KEY`, `BANCO_SENHA` e
`SMTP_SENHA`. O comando fica no histórico do shell, e o histórico vaza junto com a
máquina. Crie os três pelo console (**Secret Manager → Create secret**), colando o
valor no formulário, e libere a conta de serviço que roda o container:

```bash
PROJETO=$(gcloud config get-value project)
NUMERO=$(gcloud projects describe "$PROJETO" --format='value(projectNumber)')
for segredo in anthropic-api-key banco-senha smtp-senha; do
  gcloud secrets add-iam-policy-binding "$segredo" \
    --member="serviceAccount:$NUMERO-compute@developer.gserviceaccount.com" \
    --role=roles/secretmanager.secretAccessor
done
```

### 5. O deploy

`us-central1` e não São Paulo: a região brasileira é da faixa de preço mais cara, e a
diferença de latência não aparece num app cujas respostas dependem de uma chamada de
IA que leva segundos.

```bash
gcloud run deploy fluentia --source . \
  --region=us-central1 \
  --allow-unauthenticated \
  --max-instances=1 --min-instances=0 \
  --memory=1Gi --cpu=1 --cpu-boost \
  --set-env-vars=USAR_CLAUDE=true,FORWARD_HEADERS=framework,COOKIE_SEGURO=true,TZ=America/Sao_Paulo,BANCO_URL=jdbc:postgresql://SEU-HOST.neon.tech/neondb?sslmode=require,BANCO_USUARIO=SEU_USUARIO,SMTP_SERVIDOR=smtp.gmail.com,SMTP_USUARIO=SEU_EMAIL \
  --set-secrets=ANTHROPIC_API_KEY=anthropic-api-key:latest,BANCO_SENHA=banco-senha:latest,SMTP_SENHA=smtp-senha:latest
```

Por que cada trava está aí:

- `--max-instances=1` **não é economia, é correção.** As sessões vivem na memória do
  processo (`SessoesAtivas`). Com duas instâncias, cada uma só conhece as sessões que
  ela mesma abriu, e o usuário cai para o login ao ser atendido pela outra — sem
  padrão nenhum que ajude a entender por quê.
- `--min-instances=0` é o que mantém a conta zerada. Com 1, o Cloud Run mantém CPU
  alocada 24 h por dia e a cota gratuita não cobre isso.
- `--cpu-boost` dá CPU extra durante o arranque. É onde a JVM sofre, e é grátis.
- `--memory=1Gi` porque 512 MB é apertado para Spring com JPA e Spring AI juntos. A
  memória só é cobrada enquanto o container atende, e ele dorme quase o tempo todo.

### 6. A URL, que só existe agora

```bash
URL=$(gcloud run services describe fluentia --region=us-central1 --format='value(status.url)')
gcloud run services update fluentia --region=us-central1 --update-env-vars=URL_BASE=$URL
```

Sem isso, o link de recuperação de senha chega no e-mail apontando para
`http://localhost:5173` — o app não quebra, e o link não funciona para ninguém.

### 7. Os freios

**Billing → Budgets & alerts**, com um orçamento baixo. Ele avisa, não bloqueia, mas
avisa cedo. E o teto de gasto no console da Anthropic, que é a única proteção que não
depende de nenhum código deste repositório estar certo.

## O que dormir custa

O app não tem tarefa agendada nem thread de fundo, e a geração de conteúdo só roda no
perfil `gerar-conteudo`. Nada dispara sozinho no arranque, então acordar custa só o
próprio arranque — algo entre 10 e 20 segundos com o `--cpu-boost` ligado.

O preço real do sono é outro: **dormir apaga as sessões.** Elas moram na memória do
processo, e o processo morre quando o Cloud Run escala para zero. Quem estava logado
volta para a tela de entrada depois de uns 15 minutos sem ninguém usando o app.

Para um app de portfólio isso é aceitável. A correção, quando incomodar, é guardar a
sessão no Postgres (Spring Session JDBC): passa a sobreviver ao sono e ao deploy, e de
quebra derruba a necessidade do `--max-instances=1`.

## O banco: Neon

**Não use o Postgres gratuito do Render.** Ele é apagado 30 dias depois de criado, com
14 dias de carência, e leva os dados junto. Num app de portfólio isso significa perder
tudo sozinho enquanto você nem está olhando.

O [Neon](https://neon.com) é gratuito, dá 0,5 GB e não tem prazo de validade. Crie um
projeto e copie a string de conexão, que vem neste formato:

```
postgresql://usuario:senha@ep-algo-123.us-east-2.aws.neon.tech/neondb?sslmode=require
```

Ela **não** serve como está — o Java usa outro formato e quer usuário e senha separados.
Quebre em três:

| Variável | Valor |
|---|---|
| `BANCO_URL` | `jdbc:postgresql://ep-algo-123.us-east-2.aws.neon.tech/neondb?sslmode=require` |
| `BANCO_USUARIO` | o `usuario` da string |
| `BANCO_SENHA` | a `senha` da string |

Dois detalhes que quebram a conexão se você errar: o prefixo vira **`jdbc:postgresql://`**
(não `postgresql://`), e o **`?sslmode=require` tem que ficar** — o Neon recusa conexão
sem TLS.

O Neon suspende o banco após 5 minutos parado e religa em menos de um segundo. Some com
o tempo de sono da plataforma, não com o seu.

## Render: o caminho sem cartão

O repositório já tem `render.yaml`, então o Render monta o serviço sozinho:

**New → Blueprint →** aponte para este repositório.

Ele vai pedir os quatro valores marcados como secretos — os três do banco e a
`ANTHROPIC_API_KEY`. Eles ficam no painel do Render e nunca no git.

O primeiro build demora: compila o frontend com Vite e o backend com Maven dentro da
imagem. Quando terminar, a URL é `https://fluentia-algo.onrender.com`.

## Confira que a sessão gruda

Este é o teste que importa, e é onde falha quem esquece um detalhe: **entre, recarregue
a página e veja se continua logado.**

Se cair para a tela de login, a causa quase certa é `FORWARD_HEADERS`. A plataforma
repassa a requisição em HTTP puro, o Spring conclui que a conexão é insegura e recusa o
cookie `secure` que ele mesmo pediu — o login parece funcionar e a sessão não gruda.

No Render, o `render.yaml` já traz isso ligado. No Cloud Run, `FORWARD_HEADERS=framework`
e `COOKIE_SEGURO=true` já vão no comando de deploy do passo 5.

## Depois de publicar

**Crie sua conta pela tela.** A conta semeada nas migrations nasce inativa e sem senha
de propósito — credencial conhecida num repositório clonável é porta dos fundos.

**Ponha um teto de gasto no console da Anthropic** (Settings → Limits). É a única
proteção que não depende de nenhum código deste repositório estar certo.

**Faça cópia do banco de vez em quando.** O Neon não expira, mas conta gratuita não vem
com garantia nenhuma:

```bash
pg_dump "postgresql://usuario:senha@ep-algo-123.us-east-2.aws.neon.tech/neondb?sslmode=require" | gzip > fluentia-$(date +%F).sql.gz
```

## O que muda em relação à máquina própria

| | Máquina própria (Caddy) | Plataforma |
|---|---|---|
| **HTTPS** | Caddy pede ao Let's Encrypt | A plataforma já entrega |
| **Frontend** | Caddy serve os arquivos | Vai dentro do jar, o Spring serve |
| **Banco** | Postgres em container ao lado | Externo (Neon) |
| **Sempre ligado** | Sim | Não no plano gratuito: dorme sem acesso |
| **Domínio** | Você aponta o DNS | Ganha um `.onrender.com` |
| **Manutenção** | Sua (sistema, reboot, firewall) | Nenhuma |
