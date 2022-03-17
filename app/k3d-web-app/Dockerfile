FROM openjdk:13-jdk-alpine

## TROUBLESHOOTING
RUN apk update && apk add bash
# RUN apk --no-cache add curl

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY elasticsearch/elastic-certificates.p12 /tmp/elastic-certificates.p12

ENTRYPOINT ["java","-jar","/app.jar"]