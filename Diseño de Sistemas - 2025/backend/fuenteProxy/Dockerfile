# Etapa 1: build con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos TODO el backend (incluye agregador y los otros módulos)
COPY .. .

# Compilamos SOLO  fuenteProxy y salteamos tests
RUN mvn -f fuenteProxy/pom.xml -DskipTests clean package

# Etapa 2: imagen liviana solo con el JAR
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiamos el jar generado en la etapa anterior
COPY --from=build /app/fuenteProxy/target/*.jar app.jar

# Railway te inyecta PORT, pero exponemos 8080 como valor por defecto
EXPOSE 8082

# Comando de arranque
CMD ["java", "-jar", "app.jar"]
