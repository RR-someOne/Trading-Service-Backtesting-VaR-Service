# Runtime Dockerfile: expects built jar under build/libs/
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the assembled jar from the app module (app/build/libs/app.jar).
# The repository's .dockerignore excludes the root build/ directory from the build
# context, so use the app module's jar which is available at app/build/libs/app.jar.
COPY app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
