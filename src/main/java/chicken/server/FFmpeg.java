package chicken.server;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;

/**
 * Helper class to run ffmpeg
 */
public class FFmpeg
{
    /**
     * Enum to decide output of ffmpeg execution
     */
    enum Output
    {
        STDOUT,
        STDERR
    }

    /**
     * Execute cmd with file as stdin
     *
     * @param output bytes you want to get (either stdout or stderr)
     * @param file input file
     * @param args commands
     *
     * @return return bytes read form stdout or stderr
     */
    @Nullable
    public static byte[] run(Output output, @Nullable File file, String ...args)
    {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(args);

        if (file != null)
        {
            processBuilder.redirectInput(file);
        }

        try
        {
            //start process
            Process process = processBuilder.start();

            //read bytes form streams
            byte[] bytes = Simple.readInputStreamToByteArray(process.getInputStream());
            byte[] error = Simple.readInputStreamToByteArray(process.getErrorStream());

            int exitVal = process.waitFor();

            if (exitVal == 0)
            {
                //
                // Execution was a success!
                //

                return (output == Output.STDOUT) ? bytes : null;
            }
            else
            {
                //
                // Execution failed!
                //

                return (output == Output.STDERR) ? error : null;
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }

        return null;
    }

    /**
     * Write inputFile as wav to path.
     *
     * @param inputFile input file for ffmpeg
     * @param path destination for output
     *
     * @return return error string if something went wrong
     */
    @Nullable
    public static String writeWavError(@NonNull File inputFile, @NonNull String path)
    {
        // ffmpeg (cmd)
        // -i - (read input from stdin)
        // -y (override existing file)
        // -vn (removes video streams)
        // -ac 1 (stereo to mono stream)
        // -acodec pcm_s16le (encoding format)
        String[] ffmpeg = new String[]{
            "ffmpeg", "-i", inputFile.getAbsolutePath(), "-y", "-vn", "-ac", "1", "-acodec", "pcm_s16le", path
        };

        System.out.println(String.join(" ", ffmpeg));
        byte[] err = run(Output.STDERR, null, ffmpeg);

        return (err != null) ? new String(err) : null;
    }

    /**
     * Calculates the average amplitude for audio file.
     *
     * @param file input wav
     * @param chunks how many chunks should be returned
     *
     * @return array with averages
     */
    @NonNull
    public static int[] averageAmplitude(@NonNull File file, int chunks)
    {
        byte[] bytes = Simple.readFileBytes(file);

        if (bytes == null)
        {
            return new int[]{};
        }

        //
        // In wav files the first 44 bytes are header information like file length, etc.
        // These 44 bytes need to be removed!
        //

        int[] data = new int[ (bytes.length - 44) / 2 ];

        for (int inx = 44; inx < bytes.length; inx += 2)
        {
            int index = inx - 44;

            //
            // wav format is pcm_s16le meaning that it is singed 16bit little endian.
            // two bytes need to be seen as an entity!
            //

//            ByteBuffer bb = ByteBuffer.allocate(2);
//            bb.order(ByteOrder.LITTLE_ENDIAN);
//            bb.put(bytes[inx]);
//            bb.put(bytes[inx + 1]);
//            short value = bb.getShort(0);
            short value = (short) ((bytes[inx + 1] & 0xFF) << 8 | (bytes[inx] & 0xFF));

            data[ index / 2 ] = (value < 0) ? value * -1 : value;
        }

        //
        // Calculate average
        //

        int[] averageAmpl = new int[chunks];
        int chunkSize = data.length / chunks;

        for (int inx = 0; inx < chunks; inx++)
        {
            int start = inx * chunkSize;

            int average = Simple.average(data, start, chunkSize);
            averageAmpl[ inx ] = average;
        }

        return averageAmpl;
    }

    /**
     * Get file metadata as json.
     *
     * @param path path to file
     *
     * @return json with metadata
     */
    public static JSONObject fileMetadata(@NonNull String path)
    {
        String[] ffmpeg = new String[]{
                "ffprobe", "-i", path, "-v", "quiet", "-print_format", "json",
                "-show_format", "-show_streams", "-hide_banner"
        };

        byte[] out = run(Output.STDOUT, null, ffmpeg);

        if (out == null)
        {
            return null;
        }

        String jsonStr = new String(out);

        try
        {
            return new JSONObject(jsonStr);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            return null;
        }
    }

    /**
     * Get file sampling rate
     *
     * @param path path to file
     *
     * @return sampling rate
     */
    public static int sampleRate(@NonNull String path)
    {
        JSONObject json = fileMetadata(path);

        if (json == null)
        {
            return -1;
        }

        try
        {
            JSONArray streams = json.optJSONArray("streams");
            JSONObject stream = streams.optJSONObject(0);

            return stream.optInt("sample_rate", -1);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            return -1;
        }
    }

    /**
     * Get file duration
     *
     * @param path path to file
     *
     * @return duration
     */
    public static float duration(@NonNull String path)
    {
        JSONObject json = fileMetadata(path);

        if (json == null)
        {
            return -1;
        }

        try
        {
            JSONArray streams = json.optJSONArray("streams");
            JSONObject stream = streams.optJSONObject(0);

            return stream.optFloat("duration", -1);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            return -1;
        }
    }
}
