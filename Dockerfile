# ---- build ----------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Resolve dependencies in their own layer so code-only changes don't trigger
# a full re-download on every deploy.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- runtime --------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Koyeb overrides this via the PORT environment variable.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
