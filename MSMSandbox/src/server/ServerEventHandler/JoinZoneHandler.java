package server.ServerEventHandler;

import java.io.File;
import java.util.Random;

import org.json.JSONObject;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import server.MainExtension;
import server.Settings;
import server.Entities.Player;
import server.Tools.Util;

public class JoinZoneHandler extends BaseServerEventHandler {
	
    public static boolean compareVersions(String version1, String version2) {
    	int v1 = Integer.parseInt(version1.replace(".", ""));
    	int v2 = Integer.parseInt(version2.replace(".", ""));

        return v1 > v2;
    }

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		Settings.QUEUE++;
		User user = (User) event.getParameter(SFSEventParam.USER);
        ISession session = user.getSession();
        
        int user_count = getParentExtension().getParentZone().getUserCount();
        int max_user_count = 100;
        
		boolean russian = false; // TODO
        
        if (user_count >= max_user_count) {
            SFSObject response = new SFSObject();
            response.putUtfString("reason", !russian ? "Server is full" : "Сервер заполнен" + "!\n\n(" + user_count + "/" + max_user_count + ")");
            send("gs_player_banned", response, user);
            return;
        }
        
        Random rand = new Random();
        int randomNumber = rand.nextInt(100) + 1;
        
        if (randomNumber == 1) {
        	// Funny
        	SFSObject response = new SFSObject();
        	String text = !russian ? "A law banning My Singing Monsters has been enacted in the U.S. Unfortunately, that means you can't play My Singing Monsters for now. Rest assured, we're working to restore our service in the U.S. Please stay tuned!" : "В РФ принят закон о запрете \"Моих поющих монстров\". К сожалению, это означает, что вы пока не можете играть в My Singing Monsters. Будьте уверены, мы работаем над восстановлением нашего сервиса в Российской Федерации.  Пожалуйста, следите за обновлениями!";
        	response.putUtfString("reason", text);
        	send("gs_player_banned", response, user);
        	return;
        }
        
        user.setPrivilegeId((short) 3);
        
		String userGameId = (String)session.getProperty("user_game_id");
        String actualClientVersion = (String)session.getProperty("client_version");
        String clientDevice = session.getProperty("client_device") != null ? (String)session.getProperty("client_device") : "";
        String clientOs = session.getProperty("client_os") != null ? (String)session.getProperty("client_os") : "";
        String clientPlatform = (String)session.getProperty("client_platform");
        String clientSubplatform = session.getProperty("client_subplatform") != null ? (String)session.getProperty("client_subplatform") : "";
        String rawDeviceId = session.getProperty("raw_device_id") != null ? (String)session.getProperty("raw_device_id") : "";
        String clientLang = session.getProperty("client_lang") != null ? (String)session.getProperty("client_lang") : "";
        String actualIp = session.getProperty("ip_address") != null ? (String)session.getProperty("ip_address") : "";
        String lastUpdateVersion = (String)session.getProperty("last_update_version");
		
        boolean canPlay = compareVersions(actualClientVersion, Settings.get("min_client_version"));
        
        if (!canPlay) {
        	send("gs_client_version_error", SFSObject.newFromJsonData("{\"urls\":[{\"platform\":\"ios\",\"url\":\"itms-apps://apps.apple.com/app/my-singing-monsters/id521671873?ls=1&mt=8\"},{\"platform\":\"android\",\"url\":\"itms-apps://apps.apple.com/app/my-singing-monsters/id521671873?ls=1&mt=8\"},{\"platform\":\"amazon\",\"url\":\"amzn://apps/android?p=com.bigbluebubble.singingmonsters.amazon\"},{\"platform\":\"samsung\",\"url\":\"https://bigbluebubble.ladesk.com/271233-Samsung-Sunset\"}],\"success\":false,\"message\":\"client version fail\"}"), user);
        	return;
        }
        
        int bbbId = (int) session.getProperty("bbb_id");
        
        /*
        
        JSONObject requestJson = new JSONObject();
        requestJson.put("user_game_id", userGameId);
        
        String JSON = Util.PostRequest(MainExtension.DBUrl + "/query/user_game_id_to_bbb_id/", requestJson.toString());
        trace(JSON);
        JSONObject userGameIdToBBBIDJson = new JSONObject(JSON);
        
        if (!userGameIdToBBBIDJson.getBoolean("ok")) {
            SFSObject response = new SFSObject();
            response.putUtfString("reason", userGameIdToBBBIDJson.getString("message"));
            send("gs_player_banned", response, user);
            return;
        }
        
        boolean isNew = (boolean) userGameIdToBBBIDJson.getBoolean("is_new");
        
        Player player = null;
        
        trace(isNew);
        
        if (isNew) {
        	player = new Player(bbbId, "New Player");
        	Util.PostRequest(MainExtension.DBUrl + "/query/user_nolonger_new/", requestJson.toString());
        	
        	JSONObject newPlayerJson = new JSONObject();
        	newPlayerJson.put("bbb_id", bbbId);
        	newPlayerJson.put("display_name", "New Player");
        } else {
        	player = new Player(bbbId, "New Player");
        }
        
        */
        
        Player player = new Player(bbbId, "New Player");
        
        user.setProperty("player_object", player);
        user.setProperty("bbb_id", bbbId);
        user.setProperty("client_version", actualClientVersion);
        user.setProperty("last_update_version", lastUpdateVersion);
        user.setProperty("client_device", clientDevice);
        user.setProperty("client_platform", clientPlatform);
        user.setProperty("client_subplatform", clientSubplatform);
        user.setProperty("client_os", clientOs);
        user.setProperty("ip_address", actualIp);
        user.setProperty("client_lang", clientLang);
        
        if (clientPlatform.equals("ios")) {
            user.setProperty("idfv", rawDeviceId);
         } else if (clientPlatform.equals("android")) {
            user.setProperty("android_id", rawDeviceId);
         } else {
            user.setProperty("android_id", rawDeviceId);
         }
        
		SFSObject response = new SFSObject();
		response.putLong("bbb_id", bbbId);
		
		user.setProperty("initialized", true);
		
		// user game settings
        
		SFSArray settings = MainExtension.client.game_settings;

		for (int i = 0; i < Math.min(settings.size(), 4); i++) {
			SFSObject resp = (SFSObject) settings.getSFSObject(i);
		    trace("Sending game_settings " + (i + 1) + "\n" + resp.getDump());
		    
		    send("game_settings", resp, user);
		}
    	
		send("gs_initialized", response, user);
		
		SFSObject msgResponse = new SFSObject();
		String text = !russian ? "Welcome to the server!" : "Добро пожаловать на сервер!";
		msgResponse.putUtfString("msg", text + "\n\n(" + user_count + "/" + max_user_count + ")");
		
		SFSObject msgResponse2 = new SFSObject();
		String text2 = "we are going to skin you alive";
		msgResponse2.putUtfString("msg", text2);
		
        send("gs_display_generic_message", msgResponse, user);
        // send("gs_display_generic_message", msgResponse2, user);
	}

}
