FROM node:24-alpine AS frontend-build

WORKDIR /workspace/frontend
COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN npm install --global pnpm@11.25.0 && pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm build

FROM maven:3.10.0-eclipse-temurin-17-noble AS api-build

WORKDIR /workspace
COPY backend/pom.xml ./pom.xml
RUN mvn -B -ntp dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /workspace/frontend/dist ./src/main/resources/static
RUN mvn -B -ntp -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre-noble

WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --create-home app
COPY --from=api-build --chown=app:app /workspace/target/gestion-comercial-api-0.1.0.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
