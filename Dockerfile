# Use an official JDK image
FROM openjdk:17-jdk-slim as build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy project files
WORKDIR /app
COPY . .

# Build the app
RUN mvn clean package -DskipTests

# -------------------------
# Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*-shaded.jar app.jar
# Copy frontend static files
COPY --from=build /app/frontend /app/frontend

# Expose port (Render provides $PORT, so just document it)
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
