package server;

import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.extensions.SFSExtension;

import server.Tools.Util;

public class Settings {
    public static String ServerRoot = "D:\\MSMSandbox\\ServerData";
    //static SFSExtension ext = (SFSExtension) SmartFoxServer.getInstance().getZoneManager().getZoneByName("MySingingMonsters").getExtension();
    
    public static int QUEUE = 0;
    
    static MainExtension ext;
    
    public static String getJsonDb() {
        return ServerRoot + "/json_db/Settings.json";
    }

    public static String get(String settingName) {
        String path = getJsonDb();

        String contents = Util.ReadFile(new File(path));

        try {
            Gson gson = new Gson();
            JsonObject jsonObj = gson.fromJson(contents, JsonObject.class);

            if (jsonObj.has(settingName)) {
                return jsonObj.get(settingName).getAsString();
            } else {
            	ext.trace("Setting '" + settingName + "' not found.");
                return "S";
            }
        } catch (Exception e) {
            ext.trace("Something happened while getting setting " + settingName + ": " + e.toString());
        }

        return "S";
    }
}
