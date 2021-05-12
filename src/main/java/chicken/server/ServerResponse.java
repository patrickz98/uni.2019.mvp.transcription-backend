package chicken.server;

import org.json.JSONObject;
import org.springframework.lang.Nullable;

/**
 * This class builds a generic json response for the frontend.
 */
public class ServerResponse
{
    // Response fields
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String PAYLOAD = "payload";
    public static final String PROJECTS = "projects";
    public static final String TRANSCRIPT = "transcript";
    public static final String OBFUSCATE = "obfuscate";
    public static final String USER_ID = "userId";
    public static final String PROJECT_ID = "projectId";

    /**
     * creates and returns a failed JSONObject with an errormessage
     *
     * @param error errmessage
     * @return JSON
     */
    public static JSONObject error(@Nullable String error)
    {
        JSONObject json = new JSONObject();
        json.put(SUCCESS, false);
        json.put(ERROR, error);

        return json;
    }

    /**
     * returns a JSON without an errmassage
     *
     * @return JSON
     */
    public static JSONObject error()
    {
        return error(null);
    }

    /**
     * returns a succeed JSON
     *
     * @return JSON
     */
    public static JSONObject success()
    {
        JSONObject json = new JSONObject();
        json.put(SUCCESS, true);

        return json;
    }
}
