FROM maven:3.10-eclipse-temurin-25-noble AS dependencies

WORKDIR /app
COPY pom.xml .
RUN mvn -B -e org.apache.maven.plugins:maven-dependency-plugin:3.9.0:go-offline -DexcludeArtifactIds=domain

FROM maven:3.10-eclipse-temurin-25-noble AS builder

WORKDIR /app
COPY --from=dependencies /root/.m2 /root/.m2
COPY --from=dependencies /app/ /app
COPY /src /app/src
RUN mvn -B -e clean install -DskipTests

FROM eclipse-temurin:25-jdk

COPY --from=builder /app/target/f1.jar /f1.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/f1.jar"]
