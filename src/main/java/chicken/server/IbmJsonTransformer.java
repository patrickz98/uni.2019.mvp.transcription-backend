package chicken.server;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class transforms the raw IBM data into a frontend optimized json.
 */
public class IbmJsonTransformer
{
    /**
     * Transforms the given IBM STT Json to a JSONArray with words
     *
     * @param ibmJson ibm metadata
     *
     * @return all recognized words as a JSONArray
     */
    public static JSONArray toWordArray(JSONObject ibmJson)
    {
        JSONArray speakerLabels = ibmJson.optJSONArray("speakerLabels");
        JSONArray results = ibmJson.getJSONArray("results");

        int count = 0;

        JSONArray words = new JSONArray();

        // creates a JSON for every word with start- and endtime, timestamp,
        // alternatives, speaker and wordconfidence
        for (int inx = 0; inx < results.length(); inx++)
        {
            JSONObject result = results.getJSONObject(inx);
            JSONArray alternatives = result.getJSONArray("alternatives");
            JSONObject best = alternatives.getJSONObject(0);
            JSONArray timestamps = best.getJSONArray("timestamps");
            JSONArray wordConfidence = best.getJSONArray("wordConfidence");

            for (int iny = 0; iny < timestamps.length(); iny++)
            {
                JSONObject wordSrc = timestamps.getJSONObject(iny);

                JSONObject word = new JSONObject();
                word.put("startTime", wordSrc.getDouble("startTime"));
                word.put("endTime", wordSrc.getDouble("endTime"));
                word.put("word", wordSrc.getString("word"));
                word.put("word_confidence", wordConfidence.getJSONObject(iny).getDouble("confidence"));

                //kicks speaker if null
                if (speakerLabels != null)
                {
                    word.put("speaker", speakerLabels.getJSONObject(count).getInt("speaker"));
                    word.put("speaker_confidence", speakerLabels.getJSONObject(count).getDouble("confidence"));
                }

                //adds the word to the Array
                words.put(word);

                //counts the words
                count++;
            }
        }

        return words;
    }

    /**
     * Transforms the given IBM STT Json to a JSONArray with words.
     * This method adds two line breaks if the the speaker changes.
     *
     * @param ibmJson ibm metadata
     *
     * @return all recognized words as a JSONArray
     */
    public static JSONArray toOptimizedWordsArray(JSONObject ibmJson)
    {
        JSONArray words = toWordArray(ibmJson);

        for (int inx = 1; inx < words.length(); inx++)
        {
            JSONObject word1 = words.getJSONObject(inx - 1);
            JSONObject word2 = words.getJSONObject(inx);

            String speaker1 = word1.optString("speaker");
            String speaker2 = word2.optString("speaker");

            if (speaker1 == null || speaker2 == null) continue;

            if (!speaker2.equals(speaker1))
            {
                String word = word2.getString("word");
                word2.put("word", "\n\n" + word);
            }
        }

        return words;
    }
}
