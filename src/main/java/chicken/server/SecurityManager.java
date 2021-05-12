package chicken.server;

import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;

/**
 * This class handles all user stuff.
 * It checks if userId and projectId are valid.
 */
public class SecurityManager
{
    public static final String USER_DB_PATH = Defines.path("users", "json");

    // Singleton: Prevent multiple instances that read and write the db file.
    private static SecurityManager INSTANCE;

    // User database
    private final JSONObject authUsers;

    // Hashmap for fast user authentication.
    private final HashMap<String, Boolean> userId = new HashMap<>();

    /**
     * Constructor with db initialisation.
     */
    private SecurityManager()
    {
        JSONObject input = Simple.readFileJSONObject(USER_DB_PATH);

        if (input == null)
        {
            authUsers = new JSONObject();
            return;
        }

        for (String key: input.keySet())
        {
            String id = input.getJSONObject(key).getString("userId");
            userId.put(id, true);
        }

        authUsers = input;
    }

    /**
     * Get instance.
     */
    public static SecurityManager get()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new SecurityManager();
        }

        return INSTANCE;
    }

    /**
     * Check if user is in database.
     *
     * @return user is in db
     */
    public boolean userOk(@Nullable String user)
    {
        if (user == null)
        {
            return false;
        }

        if (user.equals(ApiController.DEBUG_USER))
        {
            return true;
        }

        synchronized (authUsers)
        {
            // user not in db!
            if (userId.getOrDefault(user, false))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if uuid is valid.
     *
     * @return string is uuid
     */
    public boolean uuidOk(@Nullable String uuid)
    {
        if (uuid == null)
        {
            return false;
        }

        if (uuid.equals(ApiController.DEBUG_ID))
        {
            return true;
        }

        return uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    /**
     * Check if user is ok and uuid is valid
     *
     * @return user and uuid are ok
     */
    public boolean ok(String user, String uuid)
    {
        return userOk(user) && uuidOk(uuid);
    }

    /**
     * Add a new user
     *
     * @return user was added
     */
    public boolean addUsers(@NonNull JSONObject json)
    {
        String tableName = json.getString("email");
        String uuid = json.getString("userId");

        synchronized (authUsers)
        {
            if (authUsers.has(tableName))
            {
                System.err.println("User " + tableName + "already exists");
                return false;
            }

            authUsers.put(tableName, json);
            userId.put(uuid, true);

            Simple.writeJSON(USER_DB_PATH, authUsers);
        }

        return true;
    }

    /**
     * Get userId for email and password if they match with existing credentials
     *
     * @return userId
     */
    @Nullable
    public String getUserAuth(@Nullable String email, @Nullable String password)
    {
        if (email == null || password == null)
        {
            return null;
        }

        synchronized (authUsers)
        {
            if (! authUsers.has(email))
            {
                return null;
            }

            JSONObject user = authUsers.getJSONObject(email);

            // "Security"
            if (! user.getString("password").equals(password))
            {
                return null;
            }

            return user.getString("userId");
        }
    }
}
