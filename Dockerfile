FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod +x mvnw && ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

# docker build -t jhonbrayansilvalaura/article-summarizer:latest .
# docker run -d --name article-summarizer -p 8081:8081 jhonbrayansilvalaura/article-summarizer:latest
# docker push jhonbrayansilvalaura/article-summarizer:latest
