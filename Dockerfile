# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and POM dependencies first for layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source code and build executable JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from build stage
COPY --from=build /app/target/smartfinance-drive-platform-*.jar app.jar

# Set container permissions
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Configure JVM memory options and run application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
