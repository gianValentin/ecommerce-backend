# Etapa 1: Construcción del proyecto
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Instalar Maven
RUN apk add --no-cache maven

# Copiar archivos de configuración del proyecto
COPY pom.xml .

RUN mvn clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip

# Copiar código fuente
COPY src src

# Ejecutar Maven para compilar y empaquetar
RUN mvn clean package -DskipTests -Ppostgresql

# Etapa 2: Ejecución de la aplicación
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copiar el JAR compilado desde la etapa anterior
COPY --from=builder /build/target/ecommerce-backend-0.0.1-SNAPSHOT.jar java-app.jar

# Exponer puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar","java-app.jar"]
