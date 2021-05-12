# Api documentation

Try your request with these debug credentials:
* userId = xxx
* projectId = 123456789

#### Raw IBM transcript

Api: `/ibm/{userId}/{projectId}`

Pain IBM STT json. This is not used in frontend. [See Example](src/main/resources/123456789.ibm.json)

#### Transcript results

Api: `/results/{userId}/{projectId}`

Transcript data optimised for frontend processing. To update transcript just POST the same json with changes.
The initial data is composed from the IBM STT results. [See Example](http://localhost:8080/results/xxx/123456789)

Expected result form GET request:
```json
{
    "transcript": [
        {
            "speaker": 1,
            "startTime": 0.25,
            "endTime": 0.73,
            "word": "presented",
            "word_confidence": 1,
            "speaker_confidence": 0.42
        },
        {
            "speaker": 1,
            "startTime": 0.73,
            "endTime": 0.85,
            "word": "by",
            "word_confidence": 1,
            "speaker_confidence": 0.42
        },
        {
            "speaker": 1,
            "startTime": 0.85,
            "endTime": 1.4,
            "word": "CBS",
            "word_confidence": 0.82,
            "speaker_confidence": 0.42
        }
    ],
    "obfuscate": {
        "Joe": false,
        "Bloomberg": true,
        "Michael": true
    },
    "success": true
}
```

#### Wav audio data

Api: `/wav/{userId}/{projectId}`. Return a audio/wav file. [See Example](http://localhost:8080/wav/xxx/123456789)

#### Download transcript

Api: `/download/{userId}/{projectId}?format=json|txt`

Download transcript. In txt format obfuscation is enabled.

#### Waveform

Api: `/plot/{userId}/{projectId}?chunks=400`

Get a simple json array with ints. These ints are average amplitudes for the corresponding wav.
[See Example](http://localhost:8080/plot/xxx/123456789?chunks=400)

#### Project overview

Api: `/projects/{userId}`

The json branch `projects` contains all project ids. Every entry contains a `status`.
In the `details` field is a simple description for what is going on. When the transcription is ready it contains the first words of it.
[See Example](http://localhost:8080/projects/xxx)

```json
{
    "projects": {
        "asdfasdfas": {
            "details": "ongoing: ffmpeg processing",
            "status": 2
        },
        "bd8f0483-69ad-4469-8e75-9299926d6a71": {
            "cost": "0.11 \u20ac",
            "details": "locally \n\nthe only thing stopping this ruling from doing real damage \n\nis that it is \n\nby its nature \n\ncompletely ineffective",
            "status": 0
        },
        "123456789": {
            "details": "success",
            "status": 0
        },
        "asdfasdfaas": {
            "details": "error: user iq too low",
            "status": 1
        }
    },
    "success": true
}
```

Status meaning:

| `status` | description |
| -------- | ----------- |
| 0        | success     |
| 1        | failed      |
| 2        | ongoing     |
| 3        | unknown     |

#### Upload new audio file

Api: `/upload/{userId}?lang={de/en}`

POST your file in
[MultipartFile](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html) format.

Response:
```json
{
    "success": true,
    "projectId": "f675b058-144d-4cdd-8d50-59acb85192b2"
}
```

#### Create a new user

Api: `/userData`

POST json:
```json
{
    "password": "password",
    "name": "admin",
    "email": "admin@mail.com"
}
```

Response on success:
```json
{
    "success": true,
    "userId": "099ba25e-24fb-4bd4-a78e-dc05f778efdd"
}
```

#### Auth user

Api: `/userDataCheck`

POST json:
```json
{
    "password": "password",
    "email": "admin@mail.com"
}
```

Response on success:
```json
{
    "success": true,
    "userId": "099ba25e-24fb-4bd4-a78e-dc05f778efdd"
}
```