package chicken.server;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.*;
import java.util.UUID;

/**
 * Helper class for generic stuff
 */
@SuppressWarnings("unused")
public class Simple
{
    /**
     * Reads all data and writes them in a byte[]
     *
     * @param in InputStream
     *
     * @return inputstream as bytes
     */
    @Nullable
    public static byte[] readInputStreamToByteArray(@Nullable InputStream in)
    {
        if (in == null)
        {
            return null;
        }

        int nRead;
        byte[] data = new byte[ 1024 ];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try
        {
            while ((nRead = in.read(data, 0, data.length)) != -1)
            {
                buffer.write(data, 0, nRead);
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            return null;
        }

        return buffer.toByteArray();
    }

    /**
     * reads all bytes from a File
     *
     * @param file file
     *
     * @return file as bytes
     */
    @Nullable
    public static byte[] readFileBytes(@Nullable File file)
    {
        //notnull check
        if ((file == null) || (! file.exists()))
        {
            return null;
        }

        try
        {
            //reads and returns as a byte[]
            InputStream is = new FileInputStream(file);
            return readInputStreamToByteArray(is);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }

        return null;
    }

    /**
     * reads all bytes from a given file
     *
     * @param path filepath
     *
     * @return file as bytes
     */
    @Nullable
    public static byte[] readFileBytes(@NonNull String path)
    {
        return readFileBytes(new File(path));
    }

    /**
     * reads all bytes from a given file
     *
     * @param file file
     *
     * @return file as string
     */
    @Nullable
    public static String readFileString(@Nullable File file)
    {
        byte[] bytes = readFileBytes(file);

        // returns null if error else a new String
        return bytes == null ? null : new String(bytes);
    }

    /**
     * reads a given file and returns as a string
     *
     * @param path filepath
     *
     * @return file as string
     */
    @Nullable
    public static String readFileString(@NonNull String path)
    {
        return readFileString(new File(path));
    }

    /**
     * reads a file and returns a new JSON
     *
     * @param file file
     *
     * @return file as json
     */
    @Nullable
    public static JSONObject readFileJSONObject(@Nullable File file)
    {
        String str = readFileString(file);
        return str != null ? new JSONObject(str) : null;
    }

    /**
     * reads from a given filepath and returns a JSON
     *
     * @param path filepath
     *
     * @return file as json
     */
    @Nullable
    public static JSONObject readFileJSONObject(@NonNull String path)
    {
        return readFileJSONObject(new File(path));
    }

    /**
     * reads a file and returns a new JSON
     *
     * @param file file
     *
     * @return file as json
     */
    @Nullable
    public static JSONArray readFileJSONArray(@Nullable File file)
    {
        String str = readFileString(file);
        return str != null ? new JSONArray(str) : null;
    }

    /**
     * reads from a given filepath and returns a JSON
     *
     * @param path filepath
     *
     * @return file as json
     */
    @Nullable
    public static JSONArray readFileJSONArray(@NonNull String path)
    {
        return readFileJSONArray(new File(path));
    }

    /**
     * Write bytes to file
     *
     * @param file filepath
     * @param bytes data
     */
    public static void writeBytes(@Nullable File file, @Nullable byte[] bytes)
    {
        if (file == null || bytes == null)
        {
            return;
        }

        try
        {
            OutputStream os = new FileOutputStream(file);
            os.write(bytes, 0, bytes.length);
            os.close();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    /**
     * Write bytes to file
     *
     * @param file filepath
     * @param bytes data
     */
    public static void writeBytes(@NonNull String file, @Nullable byte[] bytes)
    {
        writeBytes(new File(file), bytes);
    }

    /**
     * Write str to file
     *
     * @param file filepath
     * @param str data
     */
    public static void writeString(@NonNull String file, @NonNull String str)
    {
        writeBytes(file, str.getBytes());
    }

    /**
     * Write json to file
     *
     * @param file filepath
     * @param json data
     */
    public static void writeJSON(@NonNull String file, @NonNull JSONArray json)
    {
        writeBytes(file, json.toString(4).getBytes());
    }

    /**
     * Write json to file
     *
     * @param file filepath
     * @param json data
     */
    public static void writeJSON(@NonNull String file, @NonNull JSONObject json)
    {
        writeBytes(file, json.toString(4).getBytes());
    }

    /**
     * Calculate average for int array
     *
     * @param array array with ints
     * @param start start
     * @param size size
     *
     * @return average
     */
    public static int average(int[] array, int start, int size)
    {
        int sum = 0;

        for (int inx = 0; inx < size; inx++)
        {
            sum += array[start + inx];
        }

        return sum / size;
    }

    /**
     * Short uuid
     *
     * @return castrated uuid
     */
    public static String uuidShort()
    {
        String uuid = UUID.randomUUID().toString();
        String[] idParts = uuid.split("-");
        return idParts[ 0 ];
    }
}
