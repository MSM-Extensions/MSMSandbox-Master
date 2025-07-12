package server.Tools;

import org.json.JSONObject;

import server.MainExtension;

public class SQLHandler {
	private String password;

	public SQLHandler(String password) {
		this.password = password;
	}
	
	public String sendCommand(String command) {
		JSONObject requestJson = new JSONObject();
        requestJson.put("password", this.password);
        requestJson.put("sql_command", command);

        String JSON = Util.PostRequest("https://riotlove.pythonanywhere.com/admin/exec_sql", requestJson.toString());
		return JSON;
	}
}
