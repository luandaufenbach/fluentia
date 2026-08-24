# Publicar o Fluentia

Há dois caminhos, e eles não se misturam:

| | |
|---|---|
| **Máquina própria** — este arquivo | Você tem um servidor. O Caddy faz o HTTPS, serve o frontend e repassa `/api`. Fica sempre ligado |
| **Plataforma** — [PLATAFORMA.md](PLATAFORMA.md) | Render, Cloud Run e afins hospedam o container. Sem servidor para manter. O Caddy sai e o Spring serve o frontend, a partir do `Dockerfile` da raiz |

Se a ideia é só ter uma URL para mostrar, sem administrar máquina, vá para
[PLATAFORMA.md](PLATAFORMA.md). O resto deste arquivo é o caminho da máquina própria.

Tudo sobe com um comando, mas há três coisas que precisam estar certas antes — e
uma delas não dá para consertar depois.

## O que você precisa antes

| | |
|---|---|
| **Uma máquina com IP público** | **2 GB de RAM no mínimo.** Rodando, o app cabe em ~1 GB; o problema é o `--build`, que compila Maven e Vite na própria máquina e estoura 1 GB. Ou contrate 2 GB, ou compile em outro lugar |
| **Um domínio apontando para ela** | Um registro `A` com o IP. O certificado é emitido a partir do domínio: sem DNS correto, não há HTTPS |
| **Portas 80 e 443 abertas** | A 80 não é opcional — é por ela que o Let's Encrypt valida o domínio |
| **Docker e Docker Compose** | Nada mais: Java, Node e Postgres vêm dentro das imagens |

Nada disso precisa ser pago — veja [Hospedar sem gastar](#hospedar-sem-gastar) logo abaixo.

## Hospedar sem gastar

Dá para colocar no ar sem pagar nada. Duas peças, e as duas são gratuitas de verdade
— não teste de 30 dias.

### A máquina: Oracle Cloud Always Free

É a única oferta gratuita que dá uma **máquina de verdade, sempre ligada**, e ainda por
cima com região em São Paulo e Vinhedo. Isso importa mais do que parece: as opções
gratuitas de plataforma (tipo Render) desligam o app quando ninguém acessa, e a JVM
demora para voltar. Num link de portfólio, quem clicar espera um minuto olhando tela
branca — que é exatamente a primeira impressão que você não quer causar.

Escolha a forma **ARM (Ampere)**: são 4 núcleos e 24 GB no plano gratuito, contra 1 GB
da forma AMD. Já confirmei que as cinco imagens do projeto têm build ARM, então o
`docker compose` roda lá sem alterar nada.

Duas coisas a saber antes, para não perder tempo:

**Pedem cartão no cadastro.** Não é cobrança, é verificação de identidade — a conta
Always Free não vira paga sozinha. Mas se você não quiser dar o cartão, esta opção está
fora, e aí o caminho é uma plataforma gratuita (comento no fim da seção).

**A criação da máquina ARM costuma falhar** com "out of host capacity". Não é erro seu:
a capacidade gratuita é disputada. Tente outra zona de disponibilidade da mesma região,
ou repita mais tarde.

### O detalhe que faz todo mundo desistir na Oracle

O certificado não é emitido e ninguém entende por quê. São **dois** bloqueios, em
lugares diferentes, e abrir só um não resolve:

1. **Security List da VCN**, no painel da Oracle: libere as portas 80 e 443 de entrada.
2. **O firewall dentro da máquina.** As imagens Ubuntu da Oracle vêm com regras que
   descartam tudo menos o SSH. O painel pode estar liberado e o pacote morre aqui:

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
```

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
```

```bash
sudo netfilter-persistent save
```

Sem o último comando, as regras somem no próximo reboot e o site cai sozinho semanas
depois — o tipo de defeito que ninguém liga à mudança que o causou.

### O endereço: DuckDNS

Um subdomínio gratuito em [duckdns.org](https://www.duckdns.org) (login com GitHub ou
Google). Você escolhe o nome e aponta para o IP da máquina.

O Caddy emite certificado Let's Encrypt para ele **igual** a qualquer domínio pago — não
existe HTTPS de segunda categoria. Fica `https://fluentia.duckdns.org`, e a única perda é
estética.

No `.env`:

```
DOMINIO=fluentia.duckdns.org
```

Trocar por um domínio próprio depois é só mudar essa linha e refazer o DNS. Nada mais no
projeto conhece o endereço.

### Se você não quiser dar o cartão

Aí sobra plataforma gratuita — Render, Google Cloud Run e afins — e ela **não roda este
pacote como está**: essas plataformas terminam o HTTPS por conta própria e esperam um
processo só, então o Caddy sai e o Spring passa a servir o frontend junto com a API. É
uma mudança pequena e eu faço, mas é mudança: peça se for esse o caminho.

Vale saber que as condições dessas ofertas mudam com frequência (memória, CPU, se o
banco gratuito expira). Confirme o que está valendo antes de escolher por causa de um
número que eu escrevi aqui.

## Os três passos

```bash
git clone https://github.com/luandaufenbach/fluentia.git
cd fluentia
cp publicacao/.env.exemplo publicacao/.env
```

Preencha `publicacao/.env` — domínio, senha do banco e chave da API. Nenhum campo
tem valor padrão: subir sem preencher **falha na hora**, em vez de subir errado.

```bash
docker compose --env-file publicacao/.env -f publicacao/docker-compose.producao.yml up -d --build
```

O primeiro build leva alguns minutos: compila o backend com Maven e o frontend com
Vite dentro das imagens. O certificado é emitido sozinho no primeiro acesso.

```bash
docker compose --env-file publicacao/.env -f publicacao/docker-compose.producao.yml logs -f web
```

Quando aparecer `certificate obtained successfully`, o `https://seudominio` está no ar.

## O que muda em relação ao desenvolvimento

O compose de produção não é o de desenvolvimento com outro nome. Três diferenças
importam, e as três são de segurança:

**O cookie de sessão vai como `secure`.** Sem isso o navegador manda a sessão também
em HTTP, e qualquer rede no caminho — o wi-fi da cafeteria, o provedor — lê a sessão
e entra na conta. É a única coisa desta lista que **não dá para consertar depois**:
a sessão que vazou já vazou.

**Nem o banco nem o backend expõem porta.** Só o Caddy publica 80 e 443; o resto
conversa pela rede interna do compose. Postgres aberto na internet é varrido por
robôs em questão de minutos.

**Senha e chave vêm do ambiente, sem valor padrão.** O compose usa `${VAR:?}`, que
recusa subir se a variável estiver vazia.

### O detalhe que quebra o login se faltar

`FORWARD_HEADERS=framework`. O app está atrás do Caddy, então a requisição chega ao
Spring como HTTP puro mesmo tendo sido HTTPS até o proxy. Sem ler os cabeçalhos
encaminhados, o Spring conclui que a conexão é insegura e **recusa o cookie `secure`
que ele mesmo pediu** — o login parece funcionar e a sessão não gruda.

Fica desligado por padrão de propósito: aceitar `X-Forwarded-*` de qualquer origem é
confiar num cabeçalho que o cliente pode escrever. Só ligue com um proxy de verdade
na frente, e um que sobrescreva esses cabeçalhos — o Caddy sobrescreve.

## Instalar no celular

O app tem manifesto e ícones, então instala na tela inicial e abre sem a barra do
navegador.

- **Android/Chrome:** menu → *Adicionar à tela inicial*
- **iPhone/Safari:** compartilhar → *Adicionar à Tela de Início*

## Depois de publicar

**Crie a sua conta pela tela.** A conta semeada nas migrations nasce inativa e sem
senha de propósito: credencial conhecida num repositório clonável é porta dos fundos.

**Promova a conta a administrador** se quiser acessar `/api/diagnostico` e o actuator.
É por SQL, também de propósito — não existe caminho pela interface para virar admin:

```bash
docker compose --env-file publicacao/.env -f publicacao/docker-compose.producao.yml exec banco \
  psql -U "$BANCO_USUARIO" -d "$BANCO_NOME" \
  -c "UPDATE usuario SET papel = 'ADMINISTRADOR' WHERE email = 'voce@exemplo.com';"
```

**Confira o gasto** em `/api/consumo`, que mostra tokens e custo por conta. Um aluno
estudando dez desafios por dia custa cerca de US$ 6 por mês.

**Ponha um teto na própria Anthropic.** `console.anthropic.com` → *Limits* → limite de
gasto mensal. É a única proteção que não depende de nada deste código estar certo: se
algo aqui falhar, ela ainda segura a conta. Configure antes de divulgar o link.

## Cópia de segurança

O banco é o produto: histórico, notas e progresso de todo mundo estão nele. O volume
sobrevive ao recriar dos containers, mas não sobrevive à perda da máquina.

```bash
docker compose --env-file publicacao/.env -f publicacao/docker-compose.producao.yml exec -T banco \
  pg_dump -U "$BANCO_USUARIO" "$BANCO_NOME" | gzip > fluentia-$(date +%F).sql.gz
```

Coloque numa tarefa agendada e leve para fora da máquina. Uma cópia que mora no
mesmo servidor não é cópia de segurança.

## Atualizar

```bash
git pull
docker compose --env-file publicacao/.env -f publicacao/docker-compose.producao.yml up -d --build
```

As migrations do Flyway rodam sozinhas na subida. O certificado fica num volume
próprio: sem isso cada deploy pediria um certificado novo, e o Let's Encrypt limita
por semana.

## Limites conhecidos

Ditos na frente porque a alternativa é descobrir em produção.

| | |
|---|---|
| **Uma instância só** | O controle de sessão única vive na memória do processo. Rodar duas cópias do backend faz cada uma derrubar só as sessões que ela mesma abriu — escalar pede sessão compartilhada (Spring Session com Redis) |
| **Um fuso para todos** | Define a virada do dia na sequência de prática. Público fora do fuso configurado teria a sequência virando na hora errada |
| **O cadastro revela se um e-mail existe** | Inevitável sem confirmação por e-mail |
| **Sem teto de gasto por conta** | O maior risco de dinheiro que sobrou. O limite por origem trava criar contas em massa, mas **uma** conta legítima pode chamar a API sem teto nenhum. O pacote `custo` mede e nunca barra: você vê o gasto em `/api/consumo` depois de gastar. Se o link circular, é isto que eu poria em pé antes |
