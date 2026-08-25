FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=builder /workspace/target/sky-scala-oauth-0.1.0.jar /app/sky-scala-oauth.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/sky-scala-oauth.jar"]
