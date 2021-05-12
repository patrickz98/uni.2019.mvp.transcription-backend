package chicken.server;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/**
 * Main class for all api calls
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@RestController
public class ApiController
{
    public static final StatusManager status = StatusManager.get();
    public static final SecurityManager security = SecurityManager.get();

    // Debug ids for files in resources
    public static final String DEBUG_USER = "xxx";
    public static final String DEBUG_ID = "123456789";

    /**
     * Returns the ibm transcription results as a String.
     * (This is not used by the frontend, it's just for dev. purposes)
     *
     * @param response  Serverresponse
     * @param userId    user
     * @param projectId projectId
     * @return returns the JSON as a String
     */
    @RequestMapping(
            value = "/ibm/{userId}/{projectId}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public String ibmResults(
            HttpServletResponse response,
            @PathVariable String userId,
            @PathVariable String projectId)
    {
        // set header to be reachable form anywhere
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.ok(userId, projectId))
        {
            return ServerResponse.error("Permission denied").toString(4);
        }

        System.out.println("ibmResults: userId=" + userId + " projectId=" + projectId);

        // imports the results
        String results = FakeDB.getIbmResultsRaw(userId, projectId);

        if (DEBUG_ID.equals(projectId))
        {
            InputStream in = getClass().getResourceAsStream("/" + projectId + ".ibm.json");
            byte[] bytes = Simple.readInputStreamToByteArray(in);

            if (bytes != null)
            {
                results = new String(bytes);
            }
        }

        if (results == null)
        {
            return ServerResponse.error("file not found!").toString(4);
        }
        else
        {
            JSONObject json = ServerResponse.success();
            json.put(ServerResponse.PAYLOAD, new JSONObject(results));

            //sets status on success and returns
            return json.toString(4);
        }
    }

    /**
     * Returns the transformed results as a String
     * <p>
     * TODO: Refactor this to transcription
     *
     * @param response  Serverresponse
     * @param userId    user id
     * @param projectId project id
     *
     * @return returns the transformed results as a String
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/results/{userId}/{projectId}",
            produces = {"application/json"})
    public String results(
            HttpServletResponse response,
            @PathVariable String userId,
            @PathVariable String projectId)
    {
        // setting -> Header reachable form anywhere
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.ok(userId, projectId))
        {
            return ServerResponse.error("Permission denied").toString(4);
        }

        System.out.println("results: GET userId=" + userId + " projectId=" + projectId);

        JSONObject json = null;

        if (DEBUG_ID.equals(projectId))
        {
            InputStream in = getClass().getResourceAsStream("/" + projectId + ".ibm.json");
            byte[] bytes = Simple.readInputStreamToByteArray(in);

            if (bytes == null)
            {
                return ServerResponse.error().toString(4);
            }

            String str = new String(bytes);
            JSONObject ibm = new JSONObject(str);

            JSONArray transcript = IbmJsonTransformer.toOptimizedWordsArray(ibm);

            json = new JSONObject();
            json.put(ServerResponse.TRANSCRIPT, transcript);

            JSONObject obfuscate = new JSONObject();
            obfuscate.put("Michael", true);
            obfuscate.put("Bloomberg", true);
            obfuscate.put("Joe", false);

            json.put(ServerResponse.OBFUSCATE, obfuscate);
        }

        json = (json == null) ? FakeDB.getResults(userId, projectId) : json;

        if (json != null)
        {
            JSONObject resp = ServerResponse.success();

            for (String key: json.keySet())
            {
                resp.put(key, json.get(key));
            }

            return resp.toString(4);
        }

        return ServerResponse.error(projectId + ": no results found!").toString(4);
    }

    /**
     * Entry point for changes to the transcript
     *
     * @param response  Serverresponse
     * @param userId    user id
     * @param projectId project id
     * @param str       modified transcript json as string
     *
     * @return returns success message
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/results/{userId}/{projectId}",
            produces = {"application/json"})
    public String results(
            HttpServletResponse response,
            @PathVariable String userId,
            @PathVariable String projectId,
            @RequestBody String str)
    {
        // setting -> Header reachable form anywhere
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.ok(userId, projectId))
        {
            return ServerResponse.error("Permission denied").toString(4);
        }

        JSONObject json = new JSONObject(str);
        System.out.println("results: POST projectId=" + projectId);

        FakeDB.saveResults(userId, projectId, json);

        return ServerResponse.success().toString(4);
    }

    /**
     * Returns the wav for the requested project as a byteArray
     *
     * @param response  Serverresponse
     * @param userId    user id
     * @param projectId project id
     *
     * @return returns wav
     */
    @RequestMapping(
            value = "/wav/{userId}/{projectId}",
            produces = {"audio/wav"})
    public byte[] wav(
            HttpServletResponse response,
            @PathVariable String userId,
            @PathVariable String projectId)
    {
        // setting -> Header reachable form anywhere
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.ok(userId, projectId))
        {
            return null;
        }

        System.out.println("wav: projectId=" + projectId);

        if (DEBUG_ID.equals(projectId))
        {
            InputStream in = getClass().getResourceAsStream("/" + projectId + ".wav");
            return Simple.readInputStreamToByteArray(in);
        }

        return Simple.readFileBytes(Defines.path(userId, projectId, "wav"));
    }

    /**
     * Download the selected projects text or json file.
     *
     * @param response  Serverresponse
     * @param userId    user id
     * @param projectId project id
     *
     * @return returns the transformed results as a byteArray
     */
    @RequestMapping(
            value = "/download/{userId}/{projectId}",
            produces = {"text/plain"})
    public ResponseEntity<String> downloadText(
            HttpServletResponse response,
            @PathVariable String userId,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "json") String format)
    {
        // setting -> Header reachable form anywhere
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.ok(userId, projectId))
        {
            return ResponseEntity.badRequest().body(null);
        }

        System.out.println("downloadText: projectId=" + projectId);

        JSONObject json = FakeDB.getResults(userId, projectId);

        if (DEBUG_ID.equals(projectId))
        {
            InputStream in = getClass().getResourceAsStream("/" + projectId + ".ibm.json");
            byte[] bytes = Simple.readInputStreamToByteArray(in);

            if (bytes == null)
            {
                return ResponseEntity.notFound().build();
            }

            String str = new String(bytes);
            JSONObject ibm = new JSONObject(str);

            JSONArray trans = IbmJsonTransformer.toOptimizedWordsArray(ibm);
            json = new JSONObject();
            json.put(ServerResponse.TRANSCRIPT, trans);
        }

        if (json == null)
        {
            return ResponseEntity.notFound().build();
        }

        if (format.equals("txt"))
        {
            StringBuilder txt = new StringBuilder();

            JSONArray trans = json.getJSONArray(ServerResponse.TRANSCRIPT);

            JSONObject obfsc = json.optJSONObject(ServerResponse.OBFUSCATE);

            int speaker = 999;
            for (int inx = 0; inx < trans.length(); inx++)
            {
                JSONObject word = trans.getJSONObject(inx);

                if (word.optInt("speaker") != speaker)
                {
                    speaker = word.optInt("speaker");
                    txt.append("\n");
                    txt.append("\n");

                    System.err.println(speaker);

                    if ((speaker + "").equals("0"))
                    {
                        txt.append("Speaker default:");
                    }
                    else
                    {
                        txt.append("Speaker ").append(speaker).append(":");
                    }

                    txt.append("\n");
                    txt.append("\n");
                }

                boolean obfuscateWord = (obfsc != null)
                        && obfsc.optBoolean(word.getString("word"), false);

                if (obfuscateWord)
                {
                    txt.append("*****");
                    txt.append(" ");
                    continue;
                }

                txt.append(word.optString("word", ""));
                txt.append(" ");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/plain"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + projectId + ".txt\"")
                    .body(txt.toString().trim());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/json"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + projectId + ".json\"")
                .body(json.toString(4));
    }

    /**
     * Return the average amplitude values for project wav.
     *
     * @param response  ServerResponse
     * @param userId    user id
     * @param projectId project id
     * @param chunks    number of values to be returned
     *
     * @return int array of average amplitude values as a json.
     */
    @RequestMapping(
            value = "/plot/{userId}/{projectId}",
            produces = {"application/json"})
    public String plot(
            HttpServletResponse response,
            @PathVariable String userId,
            @PathVariable String projectId,
            @RequestParam int chunks) throws Exception
    {
        // setting -> Header reachable form anywhere
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.ok(userId, projectId))
        {
            return ServerResponse.error("Permission denied").toString(4);
        }

        File file = new File(Defines.path(userId, projectId, "wav"));

        if (DEBUG_ID.equals(projectId))
        {
            file = new File(getClass().getResource("/" + projectId + ".wav").toURI());
        }

        JSONArray json = new JSONArray();

        for (long inx : FFmpeg.averageAmplitude(file, chunks))
        {
            json.put(inx);
        }

        return json.toString(4);
    }

    /**
     * Return a static version of the frontend loaded form the resources.
     *
     * @param response ServerResponse
     * @param request  Request
     * @param path     Path to requested resource
     *
     * @return Requested frontend resource
     */
    @RequestMapping(
            value = {"/frontend/", "/frontend/{path}/**"},
            produces = {"text/html"})
    public String frontend(
            HttpServletResponse response,
            HttpServletRequest request,
            @PathVariable("path") Optional<String> path) throws Exception
    {
        response.addHeader("Access-Control-Allow-Origin", "*");

        // index.html as default
        String pathFile = "index.html";

        if (path.isPresent())
        {
            pathFile = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            pathFile = pathFile.substring("/frontend/".length());
        }

        System.out.println("index: pathFile=" + pathFile);

        URI uri = getClass().getResource("/frontend/" + pathFile).toURI();
        File file = new File(uri);

        return Simple.readFileString(file);
    }

    /**
     * Index page with no relevant information.
     *
     * @return Json with success message
     */
    @RequestMapping(
            value = "/",
            produces = {"application/json"})
    public String index(HttpServletResponse response)
    {
        response.addHeader("Access-Control-Allow-Origin", "*");

        return ServerResponse.success().toString(4);
    }

    /**
     * Indexpage for projects, this will list all projects in the db.
     * It is a map with project uuids as key with price and short excerpt added.
     *
     * @param userId user id
     *
     * @return projects
     */
    @RequestMapping(
            value = "/projects/{userId}",
            produces = {"application/json"})
    public String projects(
            HttpServletResponse response,
            @PathVariable String userId)
    {
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.userOk(userId))
        {
            return ServerResponse.error("Permission denied").toString(4);
        }

        if (userId == null)
        {
            return ServerResponse.error("no user in query").toString(4);
        }

        JSONObject projects = FakeDB.getProjects(userId);
        projects = status.mergeStatus(projects, userId);

        // debug stuff added
//        projects.put("asdfasdfaas", ProjectStatus.failed("user iq too low").toJSONObject());
//        projects.put("asdfasdfas", ProjectStatus.ongoing("ffmpeg").toJSONObject());
        projects.put(DEBUG_ID, ProjectStatus.success("Example Project").toJSONObject());

        JSONObject json = ServerResponse.success();
        json.put(ServerResponse.PROJECTS, projects);
        return json.toString(4);
    }

    /**
     * Uploads the chosen file and starts transcribing
     *
     * @param response ServerResponse
     * @param lang     Path
     * @param userId   userId
     *
     * @return JSON as a String
     */
    @PostMapping(
            value = "/upload/{userId}",
            produces = {"application/json"})
    public String upload(
            HttpServletResponse response,
            @RequestParam("file") MultipartFile file,
            @PathVariable String userId,
            @RequestParam(required = false) String lang)
    {
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (! security.userOk(userId))
        {
            return ServerResponse.error("Permission denied").toString(4);
        }

        System.out.println("upload: userId=" + userId);

        if (file == null || file.isEmpty())
        {
            return ServerResponse.error("File is empty!").toString(4);
        }

        if (lang == null)
        {
            lang = "en";
        }

        String uuid = UUID.randomUUID().toString();

        try
        {
            String fileName = file.getOriginalFilename();

            //set fileName
            if (fileName == null)
            {
                fileName = "no-name";
            }

            // prevent file dislocation
            fileName = fileName.replaceAll("\\.\\.", "_");
            fileName = fileName.replaceAll("/", "_");
            fileName = fileName.replaceAll("~", "_");
            fileName = fileName.replaceAll("%", "_");
            fileName = fileName.replaceAll("\\$", "_");

            System.out.println("upload: fileName=" + fileName);

            Path path = Paths.get(Defines.path("tmp", lang, userId, uuid, fileName));

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING);

            // starts transcription process
            SpeechToText.startProcessing(lang, userId, uuid, path.toString());
        }
        catch (Exception exc)
        {
            exc.printStackTrace();

            String msg = String.format("Failed to store file %s", file.getOriginalFilename());
            return ServerResponse.error(msg).toString(4);
        }

        JSONObject json = ServerResponse.success();
        json.put(ServerResponse.PROJECT_ID, uuid);

        return json.toString(4);
    }

    /**
     * Create a new user
     *
     * @param response ServerResponse
     * @param str      json as string with all user information
     *
     * @return JSON as a String
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/userData",
            produces = {"application/json"})
    public String userData(HttpServletResponse response, @RequestBody String str)
    {
        response.addHeader("Access-Control-Allow-Origin", "*");

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject(str);
        json.put("userId", uuid);

        System.out.println("userData: userId=" + uuid);

        if (security.addUsers(json))
        {
            JSONObject resp = ServerResponse.success();
            resp.put(ServerResponse.USER_ID, uuid);

            return resp.toString(4);
        }
        else
        {
            return ServerResponse.error("User cloud not be added!").toString(4);
        }
    }

    /**
     * Function for checking user credentials
     *
     * @param response ServerResponse
     * @param str      json as string with credentials
     *
     * @return JSON as a String
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/userDataCheck",
            produces = {"application/json"})
    public String userDataCheck(HttpServletResponse response, @RequestBody String str)
    {
        response.addHeader("Access-Control-Allow-Origin", "*");

        JSONObject json = new JSONObject(str);

        String email = json.optString("email");
        String password = json.optString("password");

        String userId = security.getUserAuth(email, password);
        System.out.println("userDataCheck: userId=" + userId);

        if (userId != null)
        {
            JSONObject resp = ServerResponse.success();
            resp.put(ServerResponse.USER_ID, userId);

            return resp.toString(4);
        }
        else
        {
            return ServerResponse.error("Permission denied!").toString(4);
        }
    }
}
