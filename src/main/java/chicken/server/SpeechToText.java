package chicken.server;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import org.json.JSONObject;
import org.springframework.lang.NonNull;

import java.io.File;

/**
 * Class for handling IBM transcription.
 */
public class SpeechToText
{
    private final static String API_URL = "https://stream-fra.watsonplatform.net/speech-to-text/api";

    //Patricks Key
    //private final static String API_KEY = "lqEcJyJTwEkkSo4dRzvoc-QVaqgPkHTy2cg4bXwHZtFV";

    //Thorbens Key
    private final static String API_KEY = "fe7bm5j6UNQlpVQmcSVBfpFho37gfK1y408KYAqXApI2";

    // Standard quality models
    private final static String EN_HIGH_QUALITY = "en-US_BroadbandModel";
    private final static String DE_HIGH_QUALITY = "de-DE_BroadbandModel";
    private final static String EN_LOW_QUALITY  = "en-US_NarrowbandModel";
    private final static String DE_LOW_QUALITY  = "de-DE_NarrowbandModel";

    /**
     * starts the process
     *
     * @param lang chosen language
     * @param user user
     * @param uuid id of the project
     * @param path the given path to audio
     */
    public static void startProcessing(
            @NonNull String lang,
            @NonNull String user,
            @NonNull String uuid,
            @NonNull String path)
    {
        System.out.printf("startProcessing: lang=%s user=%s uuid=%s path=%s started",
                lang, user, uuid, path);

        (new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                File file = new File(path);
                file.deleteOnExit();

                System.out.println("startProcessing: " +
                        "user=" + user + " " +
                        "uuid=" + uuid + " " +
                        "ffmpeg started");

                ApiController.status.putStatus(
                        user,
                        uuid,
                        ProjectStatus.ongoing("ffmpeg started"));

                // Safe a .wav file on the given Datapath on your decive
                // creates the path of the .wav and safes it in wavpath
                String wavPath = Defines.path(user, uuid, "wav");
                String error = FFmpeg.writeWavError(file, wavPath);

                if (error != null)
                {
                    System.out.println("startProcessing: " +
                            "user=" + user + " " +
                            "uuid=" + uuid + " " +
                            "ffmpeg.error=" + error);

                    ApiController.status.putStatus(
                            user,
                            uuid,
                            ProjectStatus.failed("ffmpeg failed: " + error));

                    return;
                }

                System.out.println("startProcessing: " +
                        "user=" + user + " " +
                        "uuid=" + uuid + " " +
                        "ffmpeg succeeded");

                ApiController.status.putStatus(
                        user,
                        uuid,
                        ProjectStatus.ongoing("ffmpeg succeeded"));

                //noinspection ResultOfMethodCallIgnored
                file.delete();

                transcription(lang, user, uuid, wavPath);
            }
        })).start();
    }

    /**
     * Decide quality
     *
     * @param path path to the audio file
     */
    private static boolean useHighQuality(@NonNull String path)
    {
        int sampleRate = FFmpeg.sampleRate(path);

        if (sampleRate <= 0)
        {
            return false;
        }

        return sampleRate >= 16000;
    }

    /**
     * starts the transcription with given path to the audiofile, the id and the chosen language
     *
     * @param lang      chosen language
     * @param user      user
     * @param uuid      id of the project
     * @param audioPath path to the audiofile
     */
    public static void transcription(
            @NonNull String lang,
            @NonNull String user,
            @NonNull String uuid,
            @NonNull String audioPath)
    {
        System.out.println("transcription: " +
                "lang=" + lang + " " +
                "user=" + user + " " +
                "uuid=" + uuid + " " +
                "started");

        ApiController.status.putStatus(
                user,
                uuid,
                ProjectStatus.ongoing("transcription started"));

        // Connect to the IBM watson API
        Authenticator authenticator = new IamAuthenticator(API_KEY);
        com.ibm.watson.speech_to_text.v1.SpeechToText service = new com.ibm.watson.speech_to_text.v1.SpeechToText(authenticator);
        service.setServiceUrl(API_URL);

        File file = new File(audioPath);
        boolean useHighQuality = useHighQuality(audioPath);

        System.out.println("transcription: " +
                "lang=" + lang + " " +
                "user=" + user + " " +
                "uuid=" + uuid + " " +
                "useHighQuality=" + useHighQuality);

        RecognizeOptions.Builder builder = null;

        try
        {
            builder = new RecognizeOptions.Builder().audio(file);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }

        if (builder == null)
        {
            System.out.println("transcription: " +
                    "lang=" + lang + " " +
                    "user=" + user + " " +
                    "uuid=" + uuid + " " +
                    "transcription failed");

            ApiController.status.putStatus(
                    user,
                    uuid,
                    ProjectStatus.failed("transcription failed"));

            return;
        }

        if (lang.equals("de"))
        {
            builder.model(useHighQuality ? DE_HIGH_QUALITY : DE_LOW_QUALITY);
            builder.speakerLabels(false);
        }

        if (lang.equals("en"))
        {
            builder.model(useHighQuality ? EN_HIGH_QUALITY : EN_LOW_QUALITY);
            builder.speakerLabels(true);
        }

        //
        // Set options for transcription
        //

        RecognizeOptions options = builder.contentType(HttpMediaType.AUDIO_WAV)
                .wordConfidence(true)
                .inactivityTimeout(-1)
                .timestamps(true)
                .build();

        //
        // Start IBM transcription
        //

        JSONObject transcript = new JSONObject();

        try
        {
            SpeechRecognitionResults ibmResult = service.recognize(options).execute().getResult();
            transcript = new JSONObject(ibmResult);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();

            ApiController.status.putStatus(
                    user,
                    uuid,
                    ProjectStatus.failed("IBM transcription failed"));
        }

        // No transcript present in IBM result json.
        if (!transcript.has("results"))
        {
            System.out.println("transcription: " +
                    "lang=" + lang + " " +
                    "user=" + user + " " +
                    "uuid=" + uuid + " " +
                    "transcription failed");

            ApiController.status.putStatus(
                    user,
                    uuid,
                    ProjectStatus.failed("transcription failed"));

            return;
        }

        System.out.println("transcription: " +
                "lang=" + lang + " " +
                "user=" + user + " " +
                "uuid=" + uuid + " " +
                "transcription success");

        FakeDB.saveIbmResults(user, uuid, transcript);

        ApiController.status.putStatus(
                user,
                uuid,
                ProjectStatus.success());
    }
}
