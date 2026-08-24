# Publicar o Fluentia

Tudo sobe com um comando, mas há três coisas que precisam estar certas antes — e
uma delas não dá para consertar depois.

## O que você precisa antes

| | |
|---|---|
| **Uma máquina com IP público** | Qualquer VPS pequena serve. O app inteiro roda em cerca de 1 GB de RAM |
| **Um domínio apontando para ela** | Um registro `A` com o IP. O certificado é emitido a partir do domínio: sem DNS correto, não há HTTPS |
| **Portas 80 e 443 abertas** | A 80 não é opcional — é por ela que o Let's Encrypt valida o domínio |
| **Docker e Docker Compose** | Nada mais: Java, Node e Postgres vêm dentro das imagens |

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
| **Sem limite de cadastro** | Nada impede alguém de criar contas em massa e gastar a sua chave. Enquanto o endereço não for público, o risco é baixo; se for divulgar, ponha um convite ou um limite por origem antes |
