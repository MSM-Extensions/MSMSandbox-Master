// GameServer-legacy/GameServer/tools/updater.py reimplementation
// https://github.com/ZewMSM/GameServer-legacy/blob/master/GameServer/tools/updater.py

package server.Tools;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MSMClient extends SmartFox {
	private String username, password, login_type, client_version, access_key, access_token, user_game_id, content_url;
	
	private boolean download_requests, useSSL;
	
	public SFSArray downloads = new SFSArray();
	
	public SFSArray game_settings = new SFSArray();
	
	public SFSObject friends = new SFSObject();
	
	public SFSObject friend = new SFSObject();
	
	public String server_ip;
	
	SmartFox sfs;
	
	private ScheduledExecutorService keepAliveScheduler;
	
	public MSMClient(String username, String password, String login_type, String client_version, String access_key, boolean download_requests) {
		this.username = username;
		this.password = password;
		this.login_type = login_type;
		this.client_version = client_version;
		this.access_key = access_key;
		this.download_requests = download_requests;
	}
	
	public SFSObject auth() {
		SFSObject response = new SFSObject();
		JSONObject tokenRequest = new JSONObject(Util.PostRequest("https://auth.bbbgame.net/auth/api/token/?g=27&u="+this.username+"&p="+this.password+"&t="+this.login_type));
		
		if (!tokenRequest.getBoolean("ok")) {
			response.putBool("ok", false);
			response.putUtfString("message", tokenRequest.getString("message"));
			return response;
		}
		
		this.access_token = tokenRequest.getString("access_token");
		JSONArray idArray = tokenRequest.getJSONArray("user_game_id");
		this.user_game_id = idArray.getString(0);
		response.putBool("ok", true);
		response.putUtfString("message", tokenRequest.toString());
		return response;
	}
	
	private String parseServerHost(String rawServerIp) {
	    if (rawServerIp == null || rawServerIp.isEmpty()) {
	        return rawServerIp;
	    }
	    
	    this.useSSL = rawServerIp.toLowerCase().contains("ssl");
	    
	    String[] parts = rawServerIp.split("\\|");
	    return parts[parts.length - 1];
	}
	
	public SFSObject pregameSetup() {
	    SFSObject response = new SFSObject();
	    Map<String, String> pregameSetupHeaders = new HashMap<>();
	    pregameSetupHeaders.put("Authorization", this.access_token);

	    JSONObject pregameSetupRequest = new JSONObject(Util.PostRequest(
	        "https://msmpc.bbbgame.net/pregame_setup.php",
	        "g=27&access_key=" + this.access_key +
	        "&tcs=1&advertiser_id=&auth_version=2.0.0&client_version=" + this.client_version +
	        "&device_id=9333940c-3a04-44d4-b6b7-4efc2f192854&device_model=PCDevice&device_vendor=wintel&lang=en&os_version=10.0.19045&package=&platform=pc",
	        pregameSetupHeaders));

	    if (!pregameSetupRequest.getBoolean("ok")) {
	        response.putBool("ok", false);
	        response.putUtfString("message", pregameSetupRequest.toString());
	        return response;
	    }

	    String rawServerIp = pregameSetupRequest.getString("serverIp");
	    this.server_ip = parseServerHost(rawServerIp);

	    this.content_url = pregameSetupRequest.getString("contentUrl");

	    response.putBool("ok", true);
	    response.putUtfString("ip", this.server_ip);
	    response.putUtfString("content_url", this.content_url);

	    return response;
	}

	
	public int getIslandTypeById(int islandId) {
	    SFSObject islandData = getParamsByCmd("db_island_v2");
	    
	    if (islandData == null || !islandData.containsKey("islands_data")) {
	        return -1;
	    }

	    SFSArray islands = (SFSArray) islandData.getSFSArray("islands_data");
	    for (int i = 0; i < islands.size(); i++) {
	        SFSObject island = (SFSObject) islands.getSFSObject(i);
	        if (island.containsKey("island_id") && island.getInt("island_id") == islandId) {
	            return island.getInt("island_type");
	        }
	    }

	    return -1;
	}

	
	public void connectToServer() {
		sfs = new SmartFox();
        
        sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::onExtensionResponse);
        sfs.connect(this.server_ip, this.useSSL ? 9934 : 9933);
	}
	
	private void onConnection(BaseEvent event) {
		SFSObject loginObject = new SFSObject();
        loginObject.putUtfString("access_key", this.access_key);
        loginObject.putUtfString("token", this.access_token);
        loginObject.putUtfString("client_version", this.client_version);

        LoginRequest loginRequest = new LoginRequest(this.user_game_id, null, "MySingingMonsters", loginObject);
        sfs.send(loginRequest);
	}
	
	private void onLogin(BaseEvent event) {
	    if (keepAliveScheduler == null || keepAliveScheduler.isShutdown()) {
	        keepAliveScheduler = Executors.newSingleThreadScheduledExecutor();
	        keepAliveScheduler.scheduleAtFixedRate(() -> {
	            sfs.send(new ExtensionRequest("keep_alive", null));
	        }, 0, 10, TimeUnit.SECONDS);
	    }
	}
	
	public void sendExtensionRequest(String cmd, SFSObject params) {
		sfs.send(new ExtensionRequest(cmd, params));
	}
	
	private void onExtensionResponse(BaseEvent event) {
		SFSObject params = (SFSObject) event.getArguments().get("params");
		String cmd = (String) event.getArguments().get("cmd");
		
	    if ("db_island_v2".equals(cmd) && params.containsKey("islands_data")) {
	     
	        SFSArray islands = (SFSArray) params.getSFSArray("islands_data");
	        for (int i = 0; i < islands.size(); i++) {
	            SFSObject island = (SFSObject) islands.getSFSObject(i);
	            if (island.getInt("island_type") == 6 || island.getInt("island_type") == 25) {
	            	island.putInt("island_type", 10);
	            }
	        }
	    }  
	    /*
	    if ("db_structure".equals(cmd) && params.containsKey("structures_data")) {
		     
	        SFSArray islands = (SFSArray) params.getSFSArray("structures_data");
	        for (int i = 0; i < islands.size(); i++) {
	            SFSObject island = (SFSObject) islands.getSFSObject(i);
	            island.putInt("view_in_market", 1);
	            island.putInt("view_in_starmarket", 1);
	            
	            island.putUtfString("allowed_on_island", "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32]");
	        }
	    }  
		*/
		SFSObject downloaded_request = new SFSObject();
		
		downloaded_request.putUtfString("cmd", cmd);
		downloaded_request.putSFSObject("params", params);
		
		downloads.addSFSObject(downloaded_request);
		
		if (this.download_requests) {
			switch (cmd) {
			case "gs_initialized":
				sfs.send(new ExtensionRequest("db_gene", null));
				sfs.send(new ExtensionRequest("db_attuner_gene", null));
				sfs.send(new ExtensionRequest("db_monster", null));
				sfs.send(new ExtensionRequest("db_structure", null));
				sfs.send(new ExtensionRequest("db_island_v2", null));
				sfs.send(new ExtensionRequest("db_island_themes", null));
				sfs.send(new ExtensionRequest("db_costumes", null));
				sfs.send(new ExtensionRequest("db_store_v2", null));
				sfs.send(new ExtensionRequest("db_scratch_offs", null));
				sfs.send(new ExtensionRequest("db_level", null));
				sfs.send(new ExtensionRequest("db_bakery_foods", null));
				sfs.send(new ExtensionRequest("db_breeding", null));
				sfs.send(new ExtensionRequest("db_polarity_amplifier_levels", null));
				sfs.send(new ExtensionRequest("db_ethereal_islet", null));
				sfs.send(new ExtensionRequest("db_entity_alt_costs", null));
				sfs.send(new ExtensionRequest("db_nucleus_reward", null));
				sfs.send(new ExtensionRequest("db_flexeggdefs", null));
				sfs.send(new ExtensionRequest("db_daily_cumulative_login", null));
				sfs.send(new ExtensionRequest("db_store_replacements", null));
				sfs.send(new ExtensionRequest("db_titansoul_levels", null));
				sfs.send(new ExtensionRequest("gs_cant_breed", null));
				sfs.send(new ExtensionRequest("gs_flip_boards", null));
				sfs.send(new ExtensionRequest("gs_flip_levels", null));
				sfs.send(new ExtensionRequest("gs_cant_breed", null));
				sfs.send(new ExtensionRequest("gs_monster_island_2_island_data", null));
				sfs.send(new ExtensionRequest("gs_epic_monster_data", null));
				sfs.send(new ExtensionRequest("gs_rare_monster_data", null));
				sfs.send(new ExtensionRequest("gs_player", null));
				break;
			case "game_settings":
				game_settings.addSFSObject(params);
				break;
	        case "gs_get_friends":
	            friends = params;
	            break;
	        case "gs_get_friend_visit_data":
	            friend = params;
	            break;
		}
		}
	}
	
    public int getNumberOfChunksForCmd(String cmd) {
        for (int i = 0; i < downloads.size(); i++) {
            SFSObject obj = (SFSObject) downloads.getSFSObject(i);
            if (cmd.equals(obj.getUtfString("cmd"))) {
                SFSObject params = (SFSObject) obj.getSFSObject("params");
                if (params.containsKey("numChunks")) {
                    return params.getInt("numChunks");
                } else {
                    return 1;
                }
            }
        }
        return 0;
    }
	
	public SFSObject getChunkByCmdAndNum(String cmd, int chunkNum) {
	    if (downloads == null || downloads.size() == 0) {
	        return new SFSObject();
	    }

	    for (int i = 0; i < downloads.size(); i++) {
	        SFSObject obj = (SFSObject) downloads.getSFSObject(i);
	        
	        String storedCmd = obj.getUtfString("cmd");
	        if (!storedCmd.equals(cmd)) {
	            continue;
	        }
	        
	        SFSObject params = (SFSObject) obj.getSFSObject("params");
	        
	        if (params.containsKey("chunk")) {
	            int storedChunkNum = params.getInt("chunk");
	            if (storedChunkNum == chunkNum) {
	                return params;
	            }
	        } else {
	            if (chunkNum == 0 || chunkNum == 1) {
	                return params;
	            }
	        }
	    }
	    
	    return new SFSObject();
	}
	
	public SFSObject getParamsByCmd(String searchCmd) {
	    for (int i = 0; i < downloads.size(); i++) {
	        SFSObject obj = (SFSObject) downloads.getSFSObject(i);
	        String cmd = obj.getUtfString("cmd");

	        if (cmd.equals(searchCmd)) {
	            return (SFSObject) obj.getSFSObject("params");
	        }
	    }
	    return null;
	}
	
    public SFSObject getPriceOfMonster(int monsterId) {
        String[] costFields = {
            "cost_diamonds",
            "cost_keys",
            "cost_relics",
            "cost_starpower",
            "cost_medals",
            "cost_coins",
            "cost_eth_currency"
        };

        SFSObject response = new SFSObject();
        int totalChunks = getNumberOfChunksForCmd("db_monster");

        for (int chunkNum = 1; chunkNum <= totalChunks; chunkNum++) {
            SFSObject chunk = getChunkByCmdAndNum("db_monster", chunkNum);
            if (chunk == null || !chunk.containsKey("monsters_data")) {
                continue;
            }

            SFSArray monsters = (SFSArray) chunk.getSFSArray("monsters_data");
            for (int i = 0; i < monsters.size(); i++) {
                SFSObject mon = (SFSObject) monsters.getSFSObject(i);
                if (mon.containsKey("monster_id") && mon.getInt("monster_id") == monsterId) {
                    for (String key : costFields) {
                        if (mon.containsKey(key)) {
                            int cost = mon.getInt(key);
                            if (cost > 0) {
                                response.putBool("ok", true);
                                String currency = key.replaceFirst("^cost_", "");
                                response.putUtfString("currency", currency);
                                response.putInt("cost", cost);
                                return response;
                            }
                        }
                    }
                    response.putBool("ok", false);
                    response.putUtfString("error",
                        "Monster " + monsterId + " has no non-zero cost fields");
                    return response;
                }
            }
        }

        response.putBool("ok", false);
        response.putUtfString("error",
            "Monster " + monsterId + " not found in db_monster");
        return response;
    }
    
    public SFSObject getPriceOfStructure(int monsterId) {
        String[] costFields = {
            "cost_diamonds",
            "cost_keys",
            "cost_relics",
            "cost_starpower",
            "cost_medals",
            "cost_coins",
            "cost_eth_currency"
        };

        SFSObject response = new SFSObject();
        int totalChunks = getNumberOfChunksForCmd("db_structure");

        for (int chunkNum = 1; chunkNum <= totalChunks; chunkNum++) {
            SFSObject chunk = getChunkByCmdAndNum("db_structure", chunkNum);
            if (chunk == null || !chunk.containsKey("structures_data")) {
                continue;
            }

            SFSArray monsters = (SFSArray) chunk.getSFSArray("structures_data");
            for (int i = 0; i < monsters.size(); i++) {
                SFSObject mon = (SFSObject) monsters.getSFSObject(i);
                if (mon.containsKey("structure_id") && mon.getInt("structure_id") == monsterId) {
                    for (String key : costFields) {
                        if (mon.containsKey(key)) {
                            int cost = mon.getInt(key);
                            if (cost > 0) {
                                response.putBool("ok", true);
                                String currency = key.replaceFirst("^cost_", "");
                                response.putUtfString("currency", currency);
                                response.putInt("cost", cost);
                                return response;
                            }
                        }
                    }
                    response.putBool("ok", false);
                    response.putUtfString("error",
                        "Structure " + monsterId + " has no non-zero cost fields");
                    return response;
                }
            }
        }

        response.putBool("ok", false);
        response.putUtfString("error",
            "Structure " + monsterId + " not found");
        return response;
    }
    
    public SFSObject getPriceOfMonster(int monsterId, String currency) {
        String key = "cost_" + currency;

        SFSObject response = new SFSObject();
        int totalChunks = getNumberOfChunksForCmd("db_monster");

        for (int chunkNum = 1; chunkNum <= totalChunks; chunkNum++) {
            SFSObject chunk = getChunkByCmdAndNum("db_monster", chunkNum);
            if (chunk == null || !chunk.containsKey("monsters_data")) {
                continue;
            }

            SFSArray monsters = (SFSArray) chunk.getSFSArray("monsters_data");
            for (int i = 0; i < monsters.size(); i++) {
                SFSObject mon = (SFSObject) monsters.getSFSObject(i);
                if (mon.containsKey("monster_id") && mon.getInt("monster_id") == monsterId) {
                    if (mon.containsKey(key)) {
                        int cost = mon.getInt(key);
                        response.putBool("ok", true);
                        response.putUtfString("currency", currency);
                        response.putInt("cost", cost);
                    } else {
                        response.putBool("ok", false);
                        response.putUtfString("error", 
                            "Currency '" + currency + "' not found for monster " + monsterId);
                    }
                    return response;
                }
            }
        }

        response.putBool("ok", false);
        response.putUtfString("error", 
            "Monster " + monsterId + " not found in db_monster");
        return response;
    }
    
    public SFSObject getPriceOfStructure(int structureId, String currency) {
        String key = "cost_" + currency;

        SFSObject response = new SFSObject();
        int totalChunks = getNumberOfChunksForCmd("db_structure");

        for (int chunkNum = 1; chunkNum <= totalChunks; chunkNum++) {
            SFSObject chunk = getChunkByCmdAndNum("db_structure", chunkNum);
            if (chunk == null || !chunk.containsKey("structures_data")) {
                continue;
            }

            SFSArray structures = (SFSArray) chunk.getSFSArray("structures_data");
            for (int i = 0; i < structures.size(); i++) {
                SFSObject struc = (SFSObject) structures.getSFSObject(i);
                if (struc.containsKey("structure_id") && struc.getInt("structure_id") == structureId) {
                    if (struc.containsKey(key)) {
                        int cost = struc.getInt(key);
                        response.putBool("ok", true);
                        response.putUtfString("currency", currency);
                        response.putInt("cost", cost);
                    } else {
                        response.putBool("ok", false);
                        response.putUtfString("error", 
                            "Currency '" + currency + "' not found for structure " + structureId);
                    }
                    return response;
                }
            }
        }

        response.putBool("ok", false);
        response.putUtfString("error", 
            "Structure " + structureId + " not found in db_structure");
        return response;
    }
    
    public SFSArray getAllMonsterEntityIds() {
        SFSArray result = new SFSArray();
        int totalChunks = getNumberOfChunksForCmd("db_monster");

        for (int chunkNum = 1; chunkNum <= totalChunks; chunkNum++) {
            SFSObject chunk = getChunkByCmdAndNum("db_monster", chunkNum);
            if (chunk == null || !chunk.containsKey("monsters_data")) {
                continue;
            }

            SFSArray monsters = (SFSArray) chunk.getSFSArray("monsters_data");
            for (int i = 0; i < monsters.size(); i++) {
                SFSObject mon = (SFSObject) monsters.getSFSObject(i);
                if (mon.containsKey("entity_id")) {
                    result.addInt(mon.getInt("entity_id"));
                }
            }
        }

        return result;
    }
    
    public SFSArray getAllStructureEntityIds() {
        SFSArray result = new SFSArray();
        int totalChunks = getNumberOfChunksForCmd("db_structure");

        for (int chunkNum = 1; chunkNum <= totalChunks; chunkNum++) {
            SFSObject chunk = getChunkByCmdAndNum("db_structure", chunkNum);
            if (chunk == null || !chunk.containsKey("structures_data")) {
                continue;
            }

            SFSArray structures = (SFSArray) chunk.getSFSArray("structures_data");
            for (int i = 0; i < structures.size(); i++) {
                SFSObject struc = (SFSObject) structures.getSFSObject(i);
                if (struc.containsKey("entity_id")) {
                    result.addInt(struc.getInt("entity_id"));
                }
            }
        }

        return result;
    }
    
    public SFSObject getGenesOfMonster(int monsterId) {
        SFSObject response = new SFSObject();

        int totalChunks = getNumberOfChunksForCmd("db_monster");

        for (int chunkNum = 1; chunkNum <= totalChunks; chunkNum++) {
            SFSObject chunk = getChunkByCmdAndNum("db_monster", chunkNum);
            if (chunk == null || !chunk.containsKey("monsters_data")) {
                continue;
            }

            SFSArray monsters = (SFSArray) chunk.getSFSArray("monsters_data");
            for (int i = 0; i < monsters.size(); i++) {
                SFSObject mon = (SFSObject) monsters.getSFSObject(i);

                if (mon.containsKey("monster_id") && mon.getInt("monster_id") == monsterId) {
                    if (mon.containsKey("genes")) {
                        String genes = mon.getUtfString("genes");
                        response.putBool("ok", true);
                        response.putUtfString("genes", genes);
                        return response;
                    } else {
                        response.putBool("ok", false);
                        response.putUtfString("error", "Monster " + monsterId + " does not have a 'genes' field.");
                        return response;
                    }
                }
            }
        }

        response.putBool("ok", false);
        response.putUtfString("error", "Monster " + monsterId + " not found in db_monster.");
        return response;
    }

}
