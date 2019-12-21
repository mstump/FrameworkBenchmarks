FROM maven:3.6.1-jdk-11-slim as maven
WORKDIR /jetty
COPY pom.xml pom.xml
COPY src src
RUN mvn compile assembly:single -q -P servlet

FROM openjdk:11.0.3-jdk-slim
WORKDIR /jetty
COPY run.sh run.sh
COPY jetty-javaagent.jar jetty-javaagent.jar
COPY metrics.yaml /etc/vorstella/metrics.yaml
COPY --from=maven /jetty/target/jetty-example-0.1-jar-with-dependencies.jar app.jar
CMD ["/jetty/run.sh"]
