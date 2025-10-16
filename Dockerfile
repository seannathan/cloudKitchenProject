# syntax=docker/dockerfile:1
FROM gradle:8.14-jdk21
WORKDIR /app

COPY settings.gradle build.gradle ./
COPY src ./src

RUN /usr/bin/gradle --no-daemon installDist

ENTRYPOINT ["/app/build/install/challenge/bin/challenge"]
