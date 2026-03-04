# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos TODO el backend (incluye agregador y los otros modulos)
COPY .. .

# Compilamos SOLO el agregador y salteamos tests
RUN mvn -f fuenteEstatica/pom.xml -DskipTests clean package

# Etapa 2: Run
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiamos el jar desde la ruta compilada
COPY --from=build /app/fuenteEstatica/target/*.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
