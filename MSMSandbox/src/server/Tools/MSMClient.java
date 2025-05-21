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
	
	public SFSObject response = new SFSObject();
	
	public SFSArray downloads = new SFSArray();
	
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
		
		if (cmd.equals("gs_initialized") && this.download_requests) {
			sfs.send(new ExtensionRequest("db_breeding", null));
		}
	}
}
