# Stage 1: Build stage
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

# Copy Maven wrapper and POM dependencies first for layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source code and build executable JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:26-jdk AS runtime
WORKDIR /app

# Create non-root system user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy compiled JAR from build stage
COPY --from=build /app/target/smartfinance-drive-platform-*.jar app.jar

# Set container permissions
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Configure JVM container memory management and run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
