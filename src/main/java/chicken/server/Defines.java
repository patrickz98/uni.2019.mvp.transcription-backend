package chicken.server;

import org.springframework.lang.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Defines
{
    // private static final String DATA_PATH_DEFAULT = "/Users/patrick/Desktop/db";
    // private static final String DATA_PATH_DEFAULT = "/Users/ksezari/Desktop/xxx1";
    private static final String DATA_PATH_DEFAULT = "/home/chicken19/db";

    @NonNull
    public static String getDataPath()
    {
        String dataPath = System.getenv("TRANSCRIPT_DB");

        if (dataPath != null)
        {
            return dataPath;
        }

        return DATA_PATH_DEFAULT;
    }

    public static String path(String ...str)
    {
        Path path = Paths.get(getDataPath(), String.join(".", str));
        return path.toAbsolutePath().toString();
    }

    public static File file(String ...str)
    {
        return new File(path(str));
    }
}
