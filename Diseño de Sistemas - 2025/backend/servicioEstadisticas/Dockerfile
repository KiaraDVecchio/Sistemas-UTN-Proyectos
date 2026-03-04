# Etapa 1 - Build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos todo el backend
COPY . .

# Compilamos SOLO servicioEstadisticas
RUN mvn -f servicioEstadisticas/pom.xml -DskipTests clean package

# Etapa 2 - Runtime
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/servicioEstadisticas/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]

