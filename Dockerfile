# Etapa 1: Construcción del proyecto
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copiar Maven Warpper
#RUN apk add --no-cache maven
COPY .mvn .mvn
COPY mvnw .

# Copiar archivos de configuración del proyecto
COPY pom.xml .

# empaqueta solo dependencias, omite Test porque no conienten aun la carpeta src
RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip

# Copiar código fuente
COPY src src

# Ejecutar Maven para compilar y empaquetar
RUN ./mvnw clean package

# Etapa 2: Ejecución de la aplicación
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copiar el JAR compilado desde la etapa anterior
COPY --from=builder /build/target/*.jar java-app.jar

# Exponer puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar","java-app.jar"]
