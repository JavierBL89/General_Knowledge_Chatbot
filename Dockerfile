# Use an official JDK image
FROM openjdk:17-jdk-slim as build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy project files
WORKDIR /app
COPY . .

# Build the app with verbose output
RUN mvn clean package -DskipTests -X

# List the target directory to see what was built
RUN ls -la /app/target/

# -------------------------
# Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy built jar from build stage (be more specific about the jar name)
COPY --from=build /app/target/*.jar app.jar

# Copy frontend static files - make sure the source path is correct
COPY ./frontend ./frontend

# Debug: List contents to verify
RUN ls -la /app/
RUN ls -la /app/frontend/ || echo "Frontend directory not found"

# Expose port (Render provides $PORT, so just document it)
EXPOSE 8080

# Run the app with more verbose output
CMD ["java", "-jar", "app.jar"]