# Build container.
FROM gradle:jdk14 AS build

WORKDIR /project
COPY . .

RUN gradle clean shadowJar

# Application final container.
FROM openjdk:14-slim

ARG VERSION=1.0

VOLUME [ "/output" ]
VOLUME [ "/seeds.txt" ]

WORKDIR /app
COPY --from=build /project/build/libs/snowball-"$VERSION"-all.jar snowball.jar

WORKDIR /

ENTRYPOINT [ "java", "-jar", "/app/snowball.jar" ]

CMD [ "--output-dir=/output" ]
