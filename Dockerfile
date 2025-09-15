# -------- Stage 1: Build the Kotlin app --------
FROM gradle:7.5-jdk17 AS builder

WORKDIR /app

COPY . .

# Build the fat JAR
RUN gradle fatJar

# -------- Stage 2: Run the app --------
FROM openjdk:17-slim

WORKDIR /app

# Copy only the built jar
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Set up PORT environment variable (used by Render)
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
