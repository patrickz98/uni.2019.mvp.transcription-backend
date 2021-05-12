package chicken.server;

import org.json.JSONObject;
import org.springframework.lang.NonNull;

import java.util.HashMap;

/**
 * This class stores all project and transcription status.
 */
public class StatusManager
{
    // Singleton: Prevent multiple inconsistent access
    private static StatusManager INSTANCE;

    //
    // Map for status control
    // This map is used in multiple threads!
    // Synchronize your access!
    //
    public final HashMap<String, HashMap<String, ProjectStatus>> STATUS = new HashMap<>();

    /**
     * Get instance.
     *
     * @return StatusManager
     */
    public static StatusManager get()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new StatusManager();
        }

        return INSTANCE;
    }

    /**
     * Put status for user and projectId.
     *
     * @param userId    user id
     * @param projectId project id
     * @param status    status for the project
     */
    public void putStatus(
            @NonNull String userId,
            @NonNull String projectId,
            @NonNull ProjectStatus status
    )
    {
        synchronized (STATUS)
        {
            HashMap<String, ProjectStatus> map = STATUS.getOrDefault(userId, new HashMap<>());
            map.put(projectId, status);

            STATUS.put(userId, map);
        }
    }

    /**
     * Merge persistent project status with ongoing and new new statuses.
     *
     * @param projects persistent projects
     * @param userId   user id
     */
    @NonNull
    public JSONObject mergeStatus(@NonNull JSONObject projects, @NonNull String userId)
    {
        synchronized (STATUS)
        {
            HashMap<String, ProjectStatus> userStatus = STATUS.getOrDefault(userId, null);

            if (userStatus == null)
            {
                return projects;
            }

            for (String uuid : userStatus.keySet())
            {
                ProjectStatus status = userStatus.get(uuid);

                if (projects.has(uuid))
                {
                    userStatus.remove(uuid);
                    continue;
                }

                if (status.status == ProjectStatus.SUCCESS)
                {
                    userStatus.remove(uuid);
                }
                else
                {
                    projects.put(uuid, status.toJSONObject());
                }
            }

            STATUS.put(userId, userStatus);
        }

        return projects;
    }
}
