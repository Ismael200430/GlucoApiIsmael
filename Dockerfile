# syntax=docker/dockerfile:1

# Etapa 1: compilar el WAR con Maven y JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY src ./src
COPY web ./web

RUN mvn -B clean package -DskipTests

# Etapa 2: ejecutar el WAR en Tomcat 11
FROM tomcat:11.0.27-jdk21-temurin-noble

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
