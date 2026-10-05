# Built by .github/workflows/deploy.yml and pushed to Artifact Registry.
#
# Java 25: micronaut-maven-plugin 5 is compiled for Java 25, so the build
# (and, to match, the runtime) uses JDK 25 - Launch was asked for JDK_25.
# Two stages: Maven builds the shaded runnable jar (micronaut-parent shades on
# `package`), a JRE-only image runs it as a non-root user. The port comes from
# $PORT at RUNTIME (application.properties: micronaut.server.port=${PORT:8080}).
# (Micronaut's own `mvn package -Dpackaging=docker` needs a docker daemon inside
# the build, which a Dockerfile build does not have.)
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /src
COPY pom.xml aot-jar.properties ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests && cp target/app-0.1.jar /app.jar

FROM eclipse-temurin:25-jre AS runtime
ARG BUILD_ID=""
WORKDIR /app
ENV PORT=8080 BUILD_ID=$BUILD_ID
RUN useradd -r -u 10001 app
COPY --from=build /app.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
