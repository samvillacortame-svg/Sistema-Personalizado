FROM maven:3.10.0-eclipse-temurin-17-noble AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre-noble

WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --create-home app

COPY --from=build --chown=app:app /workspace/target/gestion-comercial-api-0.1.0.jar /app/app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
