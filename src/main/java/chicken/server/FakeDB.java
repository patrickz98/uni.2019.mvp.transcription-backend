package chicken.server;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * Improvised database for all kinds of data.
 */
public class FakeDB
{
    /**
     * check if file exists
     *
     * @param userId userId
     * @param projectId projectId
     *
     * @return boolean if file exists
     */
    public static boolean ibmDone(@NonNull String userId, @NonNull String projectId)
    {
        File file = Defines.file(userId, projectId, "ibm", "json");
        return file.exists();
    }

    /**
     * returns the results of the transcription from the safed JSON
     *
     * @param userId userId
     * @param projectId projectId
     */
    @Nullable
    public static String getIbmResultsRaw(@NonNull String userId, @NonNull String projectId)
    {
        return Simple.readFileString(Defines.path(userId, projectId, "ibm", "json"));
    }

    /**
     * returns the results of the transcription from the safed JSON
     *
     * @param userId userId
     * @param projectId projectId
     */
    @Nullable
    public static JSONObject getIbmResults(@NonNull String userId, @NonNull String projectId)
    {
        String raw = getIbmResultsRaw(userId, projectId);
        return (raw != null) ? new JSONObject(raw) : null;
    }

    /**
     * safes the ibm results as a JSON
     *
     * @param userId userId
     * @param projectId projectId
     * @param transcript given results as a JSON
     */
    public static void saveIbmResults(
            @NonNull String userId,
            @NonNull String projectId,
            @NonNull JSONObject transcript)
    {
        String file = Defines.path(userId, projectId, "ibm", "json");
        Simple.writeJSON(file, transcript);
    }

    /**
     * Safe the edited results form frontend
     *
     * @param userId userId
     * @param projectId projectId
     * @param results given results as a JSON
     */
    public static void saveResults(@NonNull String userId, @NonNull String projectId, JSONObject results)
    {
        String file = Defines.path(userId, projectId, "results", "json");
        Simple.writeJSON(file, results);
    }

    /**
     * Get a transcript for a uuid
     *
     * @param userId userId
     * @param projectId projectId
     *
     * @return transcript json
     */
    @Nullable
    public static JSONObject getResults(@NonNull String userId, @NonNull String projectId)
    {
        String resultPath = Defines.path(userId, projectId, "results", "json");
        JSONObject results = Simple.readFileJSONObject(resultPath);

        if (results != null)
        {
            return results;
        }

        JSONObject ibm = FakeDB.getIbmResults(userId, projectId);

        if (ibm != null)
        {
            JSONArray transcript = IbmJsonTransformer.toOptimizedWordsArray(ibm);

            results = new JSONObject();
            results.put(ServerResponse.TRANSCRIPT, transcript);

            Simple.writeJSON(resultPath, results);

            return results;
        }

        return null;
    }

    /**
     * Get a project overview
     *
     * @param user userId
     *
     * @return project overview
     */
    public static JSONObject getProjects(@NonNull String user)
    {
        JSONObject projects = new JSONObject();

        Collection<File> files = FileUtils.listFiles(
                new File(Defines.getDataPath()),
                new String[]{"ibm.json"},
                false);

        files.forEach(path ->
        {
            String[] parts = path.getName().split("\\.", 3);

            if (parts.length != 3) return;

            String userId = parts[ 0 ];

            if (! user.equals(userId)) return;

            String uuid = parts[ 1 ];

            JSONObject results = getResults(user, uuid);

            if (results == null)
            {
                return;
            }

            JSONArray transcript = results.getJSONArray(ServerResponse.TRANSCRIPT);

            if (transcript == null) return;

            StringBuilder trans = new StringBuilder();

            for (int inx = 0; inx < Math.min(20, transcript.length()); inx++)
            {
                JSONObject wordInfo = transcript.getJSONObject(inx);
                trans.append(wordInfo.optString("word", ""));
                trans.append(" ");
            }

            ProjectStatus status = ProjectStatus.success(trans.toString().trim());

            //
            // Cost calculation
            //

            String wavPath = Defines.path(user, uuid, "wav");
            float durationSec = FFmpeg.duration(wavPath);
            float price = (durationSec / 3600) * 20.0f;

            status.setCost(String.format("%.2f €", price));

            projects.put(uuid, status.toJSONObject());
        });

        return projects;
    }
}
