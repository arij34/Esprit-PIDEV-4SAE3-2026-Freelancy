# Etape de runtime seulement - le build est fait par Jenkins
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/Matching-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]