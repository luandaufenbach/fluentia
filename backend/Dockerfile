# Etapa de build: compila com o JDK 21 e o Maven Wrapper do proprio projeto.
FROM maven:3.9-eclipse-temurin-21 AS construcao
WORKDIR /construcao

# Copia so o pom primeiro para a camada de dependencias ser reaproveitada entre builds.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Os testes exigem Postgres, entao rodam no pipeline e nao dentro da imagem.
RUN mvn -B -DskipTests package

# Etapa final: so o runtime, sem Maven nem codigo-fonte.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /aplicacao

# Nao roda como root.
RUN addgroup -S agente && adduser -S agente -G agente
COPY --from=construcao /construcao/target/*.jar aplicacao.jar
USER agente

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/aplicacao/aplicacao.jar"]
