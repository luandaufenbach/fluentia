# Imagem unica: frontend e backend no mesmo processo, numa origem so.
#
# Existe porque plataformas de hospedagem (Render, Cloud Run e afins) terminam o HTTPS
# por conta propria e esperam UM processo escutando UMA porta. O arranjo com Caddy em
# `publicacao/` continua valendo para maquina propria — la o Caddy e quem faz o TLS.
#
# O ganho nao e so caber na plataforma: com o Spring servindo a pagina, o cookie de
# sessao passa a ser da mesma origem da API. Sem CORS, sem cookie de terceiro para o
# navegador bloquear.
#
# Construa a partir da RAIZ do repositorio:
#   docker build -t fluentia .

# ---------- 1. o frontend ----------
FROM node:22-alpine AS frontend
WORKDIR /frontend

# O lockfile primeiro: a camada de dependencias so refaz quando ele muda, e nao a cada
# alteracao de codigo.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ---------- 2. o backend, ja com a pagina dentro ----------
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /construcao

COPY backend/pom.xml .
RUN mvn -B dependency:go-offline

COPY backend/src ./src

# A pagina entra como recurso estatico ANTES do package, entao ela vai para dentro do
# jar. E o que faz o Spring servi-la sem nenhum servidor de arquivos na frente.
COPY --from=frontend /frontend/dist ./src/main/resources/static

# Os testes exigem Postgres, entao rodam no pipeline e nao dentro da imagem.
RUN mvn -B -DskipTests package

# ---------- 3. o que de fato sobe ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /aplicacao

RUN addgroup -S agente && adduser -S agente -G agente
COPY --from=backend /construcao/target/*.jar aplicacao.jar
USER agente

EXPOSE 8080

# As duas opcoes de JVM existem por causa de container pequeno, que e o caso de todo
# plano gratuito:
#
#   MaxRAMPercentage=75 — sem isto a JVM reserva 25% da memoria do container para o
#   heap. Em 512 MB isso da 128 MB, e o app nao sobe.
#
#   TieredStopAtLevel=1 — desliga o compilador otimizador. Troca desempenho em regime
#   por arranque mais rapido, e num plano gratuito que dorme, quem manda e o arranque:
#   ninguem espera o app aquecer, mas todo mundo espera ele abrir.
ENTRYPOINT ["sh", "-c", "exec java -XX:MaxRAMPercentage=75.0 -XX:TieredStopAtLevel=1 -jar /aplicacao/aplicacao.jar"]
