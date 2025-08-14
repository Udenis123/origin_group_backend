# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only pom.xml and mvnw files first to cache dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

# Dependencies will be fetched using host Maven cache
RUN ./mvnw dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Build the JAR
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/Org-group-project-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
