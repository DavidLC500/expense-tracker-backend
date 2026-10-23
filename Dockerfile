# syntax=docker/dockerfile:1

# ---- Build stage: compila con Maven Wrapper, sin ejecutar tests (ya corren en CI antes de
# construir la imagen; los de integración necesitan Testcontainers/Docker-in-Docker, que no
# está disponible dentro de este propio build) ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/main/ src/main/
RUN ./mvnw -B -q clean package -Dmaven.test.skip=true

# ---- Runtime stage: JRE ligera, usuario no root ----
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S expensetracker && adduser -S expensetracker -G expensetracker
WORKDIR /app
COPY --from=build /app/target/expense-tracker-api.jar app.jar
USER expensetracker

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
