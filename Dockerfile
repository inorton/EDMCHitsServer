FROM gradle:jdk8-alpine
RUN whoami ; pwd ; gradle --version
ADD build.gradle .
COPY src src
RUN find src
RUN gradle build --info && find . -type f -name 'hits-*.jar'
