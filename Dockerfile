FROM ubuntu:latest

EXPOSE 8080

ENV WORKDIR /app

WORKDIR $WORKDIR
COPY ./ /app

ENV TRANSCRIPT_DB $WORKDIR/db
RUN mkdir -p $TRANSCRIPT_DB

RUN apt-get update; \
    apt-get upgrade -y; \
    apt-get dist-upgrade -y; \
    apt-get autoremove -y; \
    apt-get install ffmpeg openjdk-8-jdk -y;

RUN ./mvnw install

CMD ["./mvnw", "spring-boot:run"]
