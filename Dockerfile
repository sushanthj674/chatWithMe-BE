# --- build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies separately from source so code changes don't re-download the world.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# H2 file-mode DB lives here; mount a volume at runtime to persist across restarts.
VOLUME /app/data

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
