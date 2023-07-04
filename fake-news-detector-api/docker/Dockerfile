FROM maven:latest


WORKDIR /fake-news-detector-api

COPY ./src src
COPY ./pom.xml .

RUN mvn clean package

CMD ["java", "-jar", "/fake-news-detector-api/target/fake-news-detector-api-0.0.1-SNAPSHOT.jar"]
