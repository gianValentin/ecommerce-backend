FROM eclipse-temurin:21-jdk-alpine
COPY target/ecommerce-backend-0.0.1-SNAPSHOT.jar java-app.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "java-app.jar"]