# ==========================================
# Etapa 1: Construcción (Build)
# ==========================================
FROM maven:3.9.4-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copiamos primero el pom.xml y descargamos dependencias para optimizar la caché
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y empaquetamos el .jar
COPY src ./src
# Compilamos saltando los tests
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Ejecución (Run)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos únicamente el archivo .jar generado, dejando atrás Maven
COPY --from=builder /app/target/*.jar app.jar

# Asignamos el usuario seguro exigido por las instrucciones
USER nobody

# Exponemos el puerto de la API
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]