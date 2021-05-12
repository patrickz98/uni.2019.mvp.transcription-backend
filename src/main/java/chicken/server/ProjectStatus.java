package chicken.server;

import org.json.JSONObject;

/**
 * Class for project status.
 */
public class ProjectStatus
{
    // Possible status codes
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
    public static final int ONGOING = 2;
    public static final int UNKNOWN = 3;

    // Selected status codes
    public final int status;

    // Additional information for status, for example an error description, etc.
    public final String details;

    // Cost for project
    public String cost;

    /**
     * Constructor
     *
     * @param status status code
     */
    private ProjectStatus(int status)
    {
        this.status = status;
        this.details = null;
    }

    /**
     * Constructor
     *
     * @param status status code
     * @param details description
     */
    private ProjectStatus(int status, String details)
    {
        this.status = status;
        this.details = details;
    }

    /**
     * Project was a success
     *
     * @return instance with as success status
     */
    public static ProjectStatus success()
    {
        return new ProjectStatus(SUCCESS);
    }

    /**
     * Project was a success, with details
     *
     * @param details success message (excerpt for transcript)
     *
     * @return instance with success as status and details
     */
    public static ProjectStatus success(String details)
    {
        return new ProjectStatus(SUCCESS, details);
    }

    /**
     * Project was a failure
     *
     * @return instance with as failure status
     */
    public static ProjectStatus failed()
    {
        return new ProjectStatus(FAILED);
    }

    /**
     * Project was a failure, with details
     *
     * @param details failure message (which part failed)
     *
     * @return instance with failure as status and details
     */
    public static ProjectStatus failed(String details)
    {
        return new ProjectStatus(FAILED, details);
    }

    /**
     * Project is ongoing
     *
     * @return instance with ongoing as status
     */
    public static ProjectStatus ongoing()
    {
        return new ProjectStatus(ONGOING);
    }

    /**
     * Project is ongoing
     *
     * @param details details
     *
     * @return instance with ongoing as status and details
     */
    public static ProjectStatus ongoing(String details)
    {
        return new ProjectStatus(ONGOING, details);
    }

    /**
     * Project has unknown status (shouldn't happen)
     *
     * @return instance with unknown as status
     */
    public static ProjectStatus unknown()
    {
        return new ProjectStatus(UNKNOWN);
    }

    /**
     * Project has unknown status (shouldn't happen)
     *
     * @param details details
     *
     * @return instance with unknown as status
     */
    public static ProjectStatus unknown(String details)
    {
        return new ProjectStatus(UNKNOWN, details);
    }

    /**
     * Set cost for transcription
     *
     * @param cost cost in euros
     */
    public void setCost(String cost)
    {
        this.cost = cost;
    }

    /**
     * Instance to JsonObject
     *
     * @return JsonObject with all information.
     */
    public JSONObject toJSONObject()
    {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("details", details);
        json.put("cost", cost);

        return json;
    }
}
