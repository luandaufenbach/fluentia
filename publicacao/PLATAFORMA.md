# Publicar numa plataforma (Render, Cloud Run)

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

## Cloud Run: as duas travas obrigatórias

```bash
gcloud run deploy fluentia --source . --region southamerica-east1 --allow-unauthenticated --max-instances=1 --min-instances=0
```

`--max-instances=1` **não é só economia, é correção.** Este app guarda as sessões na
memória do processo. Com duas instâncias, cada uma só conhece as sessões que ela mesma
abriu — o usuário cai para a tela de login ao ser atendido pela outra, sem padrão
nenhum que ajude a entender por quê.

`--min-instances=0` é o que mantém a conta zerada. Com 1, o Cloud Run mantém CPU
alocada 24 h por dia, o que estoura a cota gratuita com folga e vira cobrança.

Depois: **Billing → Budgets & alerts**, e um orçamento baixo. Ele avisa, não bloqueia —
mas avisa cedo.

Já verifiquei que o app não tem tarefa agendada nem thread de fundo, e que a rotina de
geração de conteúdo só roda no perfil `gerar-conteudo`. Nada dispara no arranque, então
acordar não custa nada além do próprio arranque.

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

No Render, o `render.yaml` já traz isso ligado. No Cloud Run, as variáveis vão no
comando (`BANCO_SENHA` e `ANTHROPIC_API_KEY` de preferência pelo Secret Manager, não
aqui — o comando fica no histórico do shell):

```bash
gcloud run services update fluentia --region southamerica-east1 --update-env-vars FORWARD_HEADERS=framework,COOKIE_SEGURO=true,USAR_CLAUDE=true,TZ=America/Sao_Paulo,BANCO_URL=jdbc:postgresql://SEU-HOST.neon.tech/neondb?sslmode=require,BANCO_USUARIO=SEU_USUARIO
```

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
