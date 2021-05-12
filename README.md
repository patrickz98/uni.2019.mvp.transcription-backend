## Frontend interaction

A static version of the frontend is included in this project!
Go to `http://localhost:8080/frontend/` to see the bundled version.

For development purposes you can also use the frontend that is running on `http://localhost:3000`  
Please checkout [transcription-mvp-frontend](https://git.informatik.uni-hamburg.de/7zierahn/transcription-mvp-frontend) for details.

## Run with maven

Requirements:
* Java
* Maven
* FFmpeg
* FFprobe
* Set path for transcription data
    * Option 1: Set env variable `export TRANSCRIPT_DB="..."`
    * Option 2: In [Defines.java](src/main/java/chicken/server/Defines.java) change `DATA_PATH_DEFAULT` to a suited path

```
mvn clean spring-boot:run
```

## Run with docker

Requirements:
* [Docker](https://www.docker.com/)

```
docker build -t chicken-transcription-backend .
docker run --rm -it -p 8080:8080 chicken-transcription-backend
```

## Build a new release

Build a new .war with `mvn clean install`.
The .war will be in `target/server.war`.
Go to [basecamp](http://basecamp-demos.informatik.uni-hamburg.de:8080/manager/html) and deploy.

User: `tomcat`

Password: `project1920`

## See [Api Documentation](README-API.md)

## Dev stuff

```
"ffprobe -i FILE -v quiet -print_format json -show_format -show_streams -hide_banner"
```