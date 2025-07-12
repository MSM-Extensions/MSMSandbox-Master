package server.ServerEventHandler;

import java.util.Random;

import org.json.JSONArray;
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
import server.Entities.PlayerIsland;
import server.Entities.PlayerIslandFactory;
import server.Entities.PlayerStructure;
import server.Tools.Util;

public class JoinZoneHandler extends BaseServerEventHandler {
	
    public static boolean compareVersions(String version1, String version2) {
    	int v1 = Integer.parseInt(version1.replace(".", ""));
    	int v2 = Integer.parseInt(version2.replace(".", ""));

        return v1 > v2;
    }

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		User user = (User) event.getParameter(SFSEventParam.USER);

        ISession session = user.getSession();
        int user_count = getParentExtension().getParentZone().getUserCount();
        if (!(boolean) session.getProperty("isBot")) {
        
        long serverTime = Util.getUnixTime();
        
        String actualIp = session.getProperty("ip_address") != null ? (String)session.getProperty("ip_address") : "";
        
		boolean russian = false; //Util.ipToCountry(actualIp).equals("Russia");
		
		if (session.getProperty("client_version").equals("4.6.1")) {
			SFSObject response = new SFSObject();
			response.putBool("success", false);
			response.putUtfString("message", "Your MSM Sandbox version is outdated!");

			SFSArray urls = new SFSArray();

			SFSObject android = new SFSObject();
			android.putUtfString("platform", "android");
			android.putUtfString("url", "https://t.me/msmprivate/1162");
			urls.addSFSObject(android);

			SFSObject ios = new SFSObject();
			ios.putUtfString("platform", "ios");
			ios.putUtfString("url", "https://roblox.com");
			urls.addSFSObject(ios);

			response.putSFSArray("urls", urls);

			send("gs_client_version_error", response, user);
			return;
		}
        
        if (user_count >= Settings.max_user_count) {
            SFSObject response = new SFSObject();
            response.putUtfString("reason", (!russian ? "Server is full" : "Сервер заполнен") + "!\n\n(" + user_count + "/" + Settings.max_user_count + ")");
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
        
		String userGameId = (String)session.getProperty("user_game_id");
        String clientLang = session.getProperty("client_lang") != null ? (String)session.getProperty("client_lang") : "";
        
        boolean canPlay = (boolean) session.getProperty("can_play");
        
        if (!canPlay) {
            SFSObject response = new SFSObject();
            response.putUtfString("reason", (String) session.getProperty("cant_play_reason") + "\n\nSupport ID: "+userGameId);
            send("gs_player_banned", response, user);
            return;
        }
        
        int bbbId = (int) session.getProperty("bbb_id");
        
        JSONObject json = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT EXISTS(SELECT 1 FROM players WHERE id = '" + userGameId + "') AS player_exists;"));
        
        int exists = json.getJSONArray("result").getJSONArray(0).getInt(0);
        
        Player player = new Player(bbbId, userGameId, "New Player");
        
        if (exists == 1) {
        	JSONArray result = new JSONObject(
        		    MainExtension.sqlHandler.sendCommand(
        		        "SELECT active_island, nickname, date_created, direct_place FROM players WHERE id = '" + userGameId + "';"
        		    )
        		).getJSONArray("result").getJSONArray(0);

        	player.active_island = result.getInt(0);
        	player.display_name = result.getString(1);
        	player.date_created = result.getInt(2);
        	player.direct_place = result.getInt(3);
               	
        	JSONArray resultArray = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT * FROM player_islands WHERE user_game_id = '" + userGameId + "';")).getJSONArray("result");
        	
        	boolean foundActiveIsland = false;
        	
        	for (int i = 0; i < resultArray.length(); i++) {
        	    JSONArray row = resultArray.getJSONArray(i);
        	    int islandId = row.getInt(3);
        	    long user_island_id = row.getInt(0);
        	    
        	    int islandType = MainExtension.client.getIslandTypeById(islandId);
        	    
        	    if (islandType == -1) {
                    SFSObject response = new SFSObject();
                    response.putUtfString("reason", "The server messed up parsing your islands. Please try again.\n\nSupport ID: "+userGameId);
                    send("gs_player_banned", response, user);
                    return;
        	    }
        	    
        	    PlayerIsland island = PlayerIsland.createNewIsland(bbbId, islandId, islandType, user_island_id);
        	    
            	result = new JSONObject(
            		    MainExtension.sqlHandler.sendCommand(
            		        "SELECT likes, dislikes FROM player_islands WHERE user_island_id = '" + user_island_id + "';"
            		    )
            		).getJSONArray("result").getJSONArray(0);
            	
            	island.likes = result.getInt(0);
            	island.dislikes = result.getInt(1);
        	    
        	    PlayerIsland.addPlayerMonsters(island, user_island_id, userGameId);
        	    PlayerIsland.addPlayerStructures(island, user_island_id, userGameId);
        	    
        	    if (!(Util.isIslandBroken(islandId))) {
        	    	player.addIsland(island);
        	    	if (!foundActiveIsland) {
        	    		foundActiveIsland = player.active_island == user_island_id;
        	    	}
        	    } else {
        	    	trace("Repairing account...");
        	    	MainExtension.sqlHandler.sendCommand("DELETE FROM player_islands WHERE user_island_id = '" + user_island_id + "';");
        	    	if (player.active_island == user_island_id) {
        	    		player.active_island = resultArray.getJSONArray(0).getInt(0);
        	    	}
        	    }
        	}
        	
        	if (!foundActiveIsland) {
        		player.active_island = resultArray.getJSONArray(0).getInt(0);
        	}
        } else {
        	long user_island_id = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT COUNT(*) FROM player_islands;")).getJSONArray("result").getJSONArray(0).getInt(0) + 1;
        	
        	MainExtension.sqlHandler.sendCommand("INSERT INTO players (last_login, id, date_created, active_island, nickname) VALUES ('"+serverTime+"', '"+ userGameId+"', '"+ serverTime +"', '1', 'New Player');");
        	MainExtension.sqlHandler.sendCommand("INSERT INTO player_islands (user_game_id, island_id) VALUES ('" + userGameId + "', '1');");
    	    PlayerIsland island = PlayerIsland.createNewIsland(bbbId, 1, 1, user_island_id);
    	    
    	    player.active_island = user_island_id;
    	    player.date_created = serverTime;
    	    
    	    PlayerIslandFactory.createInitialStructures(island, userGameId);
    	    
    	    player.addIsland(island);
    	    
    	    MainExtension.sqlHandler.sendCommand("UPDATE players SET active_island = '" + user_island_id + "' WHERE id = '" + userGameId + "';");
    	    
			MainExtension.sqlHandler.sendCommand(
					"INSERT INTO user_friends (user_1, user_2) VALUES ('"
					+ bbbId + "', '"
					+ 2201 + "');"
			);
			
	        Util.PostRequest(
	        	    "https://discord.com/api/webhooks/1388224087003889834/WR8a9JEcMCv9tmQGkXJPsoueXyX1tuoE6aYhr6yh3rvpHda95lNCkWzrFAPtOJx1S5H3",
	        	    "{\"content\": \"new account created!\"}"
	        	);
        }
    	
        JSONArray result = new JSONObject(
        	    MainExtension.sqlHandler.sendCommand(
        	        "SELECT coins, diamonds, food, `keys`, starpower, relics, shards FROM players WHERE id = '" + userGameId + "';"
        	    )
        	).getJSONArray("result").getJSONArray(0);

        player.coins = result.getInt(0);
        player.diamonds = result.getInt(1);
        player.food = result.getInt(2);
        player.keys = result.getInt(3);
        player.starpower = result.getInt(4);
        player.relics = result.getInt(5);
        player.shards = result.getInt(6);
        
        user.setProperty("player_object", player);
        user.setProperty("bbb_id", bbbId);
        user.setProperty("user_game_id", userGameId);
        user.setProperty("ip_address", actualIp);
        user.setProperty("client_lang", clientLang);
        user.setProperty("join_time", serverTime);
        
		SFSArray settings = MainExtension.client.game_settings;
		
		if (MainExtension.gameSettings.toJson().equals("{}")) {
			for (int i = 0; i < settings.size(); i++) {
			    SFSObject currentSetting = (SFSObject) settings.getSFSObject(i);

			    for (String key : currentSetting.getKeys()) {
			    	MainExtension.gameSettings.put(key, currentSetting.get(key));
			    }
			}
		}

		send("game_settings", MainExtension.gameSettings, user);
		
		user_count = getParentExtension().getParentZone().getUserCount();
		
		SFSObject msgResponse = new SFSObject(); 
		msgResponse.putUtfString("msg", "Welcome to the server!\n\n(" + user_count + "/" + Settings.max_user_count + ")\nSupport ID: "+userGameId); // session.getProperty("server_location")
		
        send("gs_display_generic_message", msgResponse, user);
        
		SFSObject response = new SFSObject();
		response.putLong("bbb_id", bbbId);
        
        send("gs_initialized", response, user);
        /*
        Util.PostRequest(
        	    "https://discord.com/api/webhooks/1388224087003889834/WR8a9JEcMCv9tmQGkXJPsoueXyX1tuoE6aYhr6yh3rvpHda95lNCkWzrFAPtOJx1S5H3",
        	    "{\"content\": \"new online is: " + user_count + "/" + Settings.max_user_count + "\"}"
        	);
        */
        }
        Settings.QUEUE++;
        MainExtension.sessionsSinceStart++;
	}
}
