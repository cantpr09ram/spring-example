FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
COPY dev-run.sh ./dev-run.sh
EXPOSE 8080
CMD ["sh", "./dev-run.sh"]
