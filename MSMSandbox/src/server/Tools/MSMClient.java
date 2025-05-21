// GameServer-legacy/GameServer/tools/updater.py reimplementation
// https://github.com/ZewMSM/GameServer-legacy/blob/master/GameServer/tools/updater.py

package server.Tools;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.smartfoxserver.v2.entities.data.SFSObject;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

public class MSMClient extends SmartFox {
	private String username, password, login_type, client_version, access_key, access_token, user_game_id, server_ip, content_url;
	
	public MSMClient(String username, String password, String login_type, String client_version, String access_key) {
		this.username = username;
		this.password = password;
		this.login_type = login_type;
		this.client_version = client_version;
		this.access_key = access_key;
		
		JSONObject tokenRequestJson = new JSONObject();
		tokenRequestJson.put("g", 27);
		tokenRequestJson.put("u", this.username);
		tokenRequestJson.put("p", this.password);
		tokenRequestJson.put("t", this.login_type);
		tokenRequestJson.put("client_version", this.client_version);
		tokenRequestJson.put("access_key", this.access_key);
		tokenRequestJson.put("platform", "pc");
		
		JSONObject tokenRequest = new JSONObject(Util.PostRequest("https://auth.bbbgame.net/auth/api/token", tokenRequestJson.toString()));
		
		if (!tokenRequest.getBoolean("ok")) {
			Util.ext.trace("MSMClient: "+tokenRequest.getString("message"));
			return;
		}
		
		this.access_token = tokenRequest.getString("access_token");
		this.user_game_id = tokenRequest.getString("user_game_id");

		Map<String, String> pregameSetupHeaders = new HashMap<>();
		pregameSetupHeaders.put("Authorization", this.access_token);
		
		JSONObject pregameSetupRequest = new JSONObject(Util.PostRequest("https://msmpc.bbbgame.net/pregame_setup.php", tokenRequestJson.toString(), pregameSetupHeaders));
		
		if (!pregameSetupRequest.getBoolean("ok")) {
			Util.ext.trace("MSMClient: "+tokenRequest.getString("message"));
			return;
		}
		
		this.server_ip = tokenRequest.getString("serverIp");
		this.content_url = tokenRequest.getString("contentUrl");
	}
	
	private void Update() {
        ConfigData config = new ConfigData();
        config.setHost(this.server_ip);
        config.setPort(9933);
        config.setZone("MySingingMonsters");
        config.setDebug(false);
        
        this.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        this.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::onExtensionResponse);
        this.connect(config);
	}
	
	private void onConnection(BaseEvent event) {
		SFSObject loginObject = new SFSObject();
        loginObject.putUtfString("access_key", this.access_key);
        loginObject.putUtfString("token", this.access_token);
        loginObject.putUtfString("client_version", this.client_version);

        LoginRequest loginRequest = new LoginRequest(username, "", "MySingingMonsters", loginObject);
        this.send(loginRequest);
	}
	
	private void onExtensionResponse(BaseEvent event) {
		
	}
	
	private void download(String cmd) {
		this.download(cmd, new SFSObject());
	}
	
	private void download(String cmd, SFSObject params) {
		
	}
}
