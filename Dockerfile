FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src

EXPOSE 8082

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.arguments=--server.port=8082"]
