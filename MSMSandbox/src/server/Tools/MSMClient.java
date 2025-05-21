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

public class MSMClient extends SmartFox {
	private String username, password, login_type, client_version, access_key, access_token, user_game_id, server_ip, content_url;
	
	private boolean download_requests;
	
	public SFSArray downloads = new SFSArray();
	
	public SFSArray game_settings = new SFSArray();
	
	SmartFox sfs;
	
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
		return response;
	}
	
	public SFSObject pregameSetup() {
		SFSObject response = new SFSObject();
		Map<String, String> pregameSetupHeaders = new HashMap<>();
		pregameSetupHeaders.put("Authorization", this.access_token);
		
		JSONObject pregameSetupRequest = new JSONObject(Util.PostRequest("https://msmpc.bbbgame.net/pregame_setup.php/?g=27&access_key="+this.access_key+"&client_version=4.8.2&platform=pc", pregameSetupHeaders));
		
		if (!pregameSetupRequest.getBoolean("ok")) {
			response.putBool("ok", false);
			response.putUtfString("message", pregameSetupRequest.getString("message"));
			return response;
		}
		
		this.server_ip = pregameSetupRequest.getString("serverIp");
		this.content_url = pregameSetupRequest.getString("contentUrl");
		
		response.putBool("ok", true);
		response.putUtfString("ip", this.server_ip);
		
		return response;
	}
	
	public void connectToServer() {
        ConfigData config = new ConfigData();
        config.setHost(this.server_ip);
        config.setPort(9933);
        config.setZone("MySingingMonsters");
        config.setDebug(false);
        
        sfs = new SmartFox();
        
        sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::onExtensionResponse);
        sfs.connect(this.server_ip, 9933);
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
		
	}
	
	private void onExtensionResponse(BaseEvent event) {
		SFSObject params = (SFSObject) event.getArguments().get("params");
		String cmd = (String) event.getArguments().get("cmd");
		
		SFSObject downloaded_request = new SFSObject();
		
		downloaded_request.putUtfString("cmd", cmd);
		downloaded_request.putSFSObject("params", params);
		
		downloads.addSFSObject(downloaded_request);
		
		if (this.download_requests) {
			if (cmd.equals("gs_initialized")) {
				sfs.send(new ExtensionRequest("db_genes", null));
				sfs.send(new ExtensionRequest("db_monster", null));
				sfs.send(new ExtensionRequest("db_structure", null));
				sfs.send(new ExtensionRequest("db_island_v2", null));
				sfs.send(new ExtensionRequest("db_island_themes", null));
				sfs.send(new ExtensionRequest("db_costumes", null));
				sfs.send(new ExtensionRequest("db_store_v2", null));
				sfs.send(new ExtensionRequest("db_scratch_offs", null));
				sfs.send(new ExtensionRequest("db_level", null));
				sfs.send(new ExtensionRequest("gs_cant_breed", null));
			}
			
			if (cmd.equals("game_settings")) {
				game_settings.addSFSObject(params);
			}	
		}
	}
	
	public int getNumberOfChunksForCmd(String cmd) {
	    for (int i = 0; i < downloads.size(); i++) {
	        SFSObject obj = (SFSObject) downloads.getSFSObject(i);

	        if (cmd.equals(obj.getUtfString("cmd"))) {
	            SFSObject params = (SFSObject) obj.getSFSObject("params");
	            
	            if (params.containsKey("chunks")) {
	                return params.getInt("chunks");
	            } else {
	                return 1;
	            }
	        }
	    }
	    
	    return 0;
	}
	
	public SFSObject getChunkByCmdAndNum(String cmd, int chunkNum) {
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
	    
	    return null;
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

}
