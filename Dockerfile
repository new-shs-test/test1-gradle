FROM eclipse-temurin:17-jdk-alpine
LABEL authors="daw-leic51n-g14"

COPY build/libs/jvm-0.0.1.jar jvm-0.0.1.jar

ENTRYPOINT ["java","-jar","/jvm-0.0.1.jar"]
