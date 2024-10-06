FROM maven:3.9.9 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package
FROM openjdk:21-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y bash procps coreutils
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar
EXPOSE 8199
CMD ["java", "-jar", "app.jar"]