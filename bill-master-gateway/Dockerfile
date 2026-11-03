FROM internal.example.com/hub.docker.com/adoptopenjdk/openjdk11:centos
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
