package server;

import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import server.Tools.Util;

public class Settings {
    public static String ServerRoot;

    public static int QUEUE = 0;
    public static int max_user_count = 100;

    static MainExtension ext;
    
    public static int[] brokenIslands = new int[] {};

    static {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().contains("linux")) {
            ServerRoot = "/home/ubuntu/MSMSandbox/ServerData";
        } else {
            ServerRoot = "D:\\MSMSandbox\\ServerData";
        }
    }

    public static String getJsonDb() {
        return ServerRoot + "/json_db/Settings.json";
    }

    public static void setExtension(MainExtension extension) {
        ext = extension;
    }

    public static void logServerRoot() {
        if (ext != null) ext.trace("Settings ServerRoot is: " + ServerRoot);
    }

    public static String get(String settingName) {
        String path = getJsonDb();
        File file = new File(path);

        if (!file.exists()) {
            if (ext != null) ext.trace("Settings file does not exist: " + path);
            return "S";
        }

        String contents = Util.ReadFile(file);

        if (ext != null) ext.trace("Settings.json content: [" + contents + "]");

        if (contents == null || contents.isEmpty()) {
            if (ext != null) ext.trace("Settings file is empty or unreadable: " + path);
            return "S";
        }

        try {
            Gson gson = new Gson();
            JsonObject jsonObj = gson.fromJson(contents, JsonObject.class);

            if (ext != null) ext.trace("JSON parsed successfully.");

            if (jsonObj != null && jsonObj.has(settingName)) {
                return jsonObj.get(settingName).getAsString();
            } else {
                if (ext != null) ext.trace("Setting '" + settingName + "' not found in Settings.json");
                return "S";
            }
        } catch (JsonSyntaxException e) {
            if (ext != null) ext.trace("JSON syntax error: " + e.getMessage());
            throw e;  // rethrow for visibility in logs
        } catch (Exception e) {
            if (ext != null) ext.trace("Unexpected error parsing Settings.json: " + e.toString());
            throw e;
        }
    }

}
