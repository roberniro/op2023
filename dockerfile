FROM openjdk:11

WORKDIR /app
COPY build/libs/moongkl-here-mobileapi-0.0.1-SNAPSHOT.jar ./moongkl-here-mobileapi.jar
ENTRYPOINT ["java", "-jar", "moongkl-here-mobileapi.jar"]
EXPOSE 8080