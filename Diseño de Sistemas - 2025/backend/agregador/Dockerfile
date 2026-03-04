# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .

RUN mvn -f agregador/pom.xml -DskipTests clean package

# Etapa 2: Runtime
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY --from=build /app/agregador/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-Xmx700m", "-Xms256m", "-jar", "app.jar"]
