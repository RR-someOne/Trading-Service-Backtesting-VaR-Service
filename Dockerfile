# Runtime Dockerfile: expects built jar under build/libs/
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the assembled fat/boot jar produced by Gradle
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
