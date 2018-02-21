FROM openjdk:8-jdk-slim-stretch

RUN whoami ; pwd 
RUN mkdir -p /hits ; chmod 777 /hits
WORKDIR /hits
ADD ./build/libs/hits-spring-boot-0.3.0.jar .
RUN find . -type f -name 'hits-*.jar'

ENTRYPOINT ["java", "-Xmx64m", "-Xms64m"]
CMD ["-jar", "hits-spring-boot-0.3.0.jar"]

EXPOSE 8080
