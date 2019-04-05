FROM openjdk:8-jre

WORKDIR /shortener
VOLUME /shortener

COPY build/libs/UrlShortener-1.0-SNAPSHOT.jar /URLShortener.jar

CMD ["java", "-jar", "/URLShortener.jar"]