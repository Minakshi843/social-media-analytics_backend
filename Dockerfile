# Stage 1: Compile the application using Maven and JDK 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /workspace
COPY pom.xml .
# Prefetch dependencies for faster subsequent runs
RUN mvn dependency:go-offline
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Minimalist JRE image to run the binary
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080

# Run commands
ENTRYPOINT ["java", "-jar", "app.jar"]
