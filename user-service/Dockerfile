FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the essentials for the build process
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

# Copy source code
COPY src/ ./src/

# Use a cache mount to persist the .m2 folder between builds.
# This single RUN command will download dependencies AND package the application.
# On subsequent builds, Maven will find most dependencies in the cache
# and only download new ones, making it much faster.
RUN --mount=type=cache,target=/root/.m2 ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar


# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Expose the application port
EXPOSE 8081

# Set the entry point - the Docker container will be started with env file variables
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

