package server.ServerEventHandler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import server.MainExtension;
import server.Settings;
import server.Entities.Player;
import server.Tools.Util;

public class DisconnectHandler extends BaseServerEventHandler {

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		User user = (User) event.getParameter(SFSEventParam.USER);
		Settings.QUEUE--;
		
		Player player = (Player) user.getProperty("player_object");
		
		if (player == null || player.user_game_id == null) {
			return;
		}
		
		String userGameId = player.user_game_id;
		
		/*
		
		int user_count = getParentExtension().getParentZone().getUserCount();
		
        Util.PostRequest(
        	    "https://discord.com/api/webhooks/1388224087003889834/WR8a9JEcMCv9tmQGkXJPsoueXyX1tuoE6aYhr6yh3rvpHda95lNCkWzrFAPtOJx1S5H3",
        	    "{\"content\": \"new online is: " + user_count + "/" + Settings.max_user_count + "\"}"
        	);
		*/
		
		MainExtension.sqlHandler.sendCommand("UPDATE players SET coins = '" + player.coins + "' WHERE id = '" + userGameId + "';");
		MainExtension.sqlHandler.sendCommand("UPDATE players SET food = '" + player.food + "' WHERE id = '" + userGameId + "';");
		MainExtension.sqlHandler.sendCommand("UPDATE players SET diamonds = '" + player.diamonds + "' WHERE id = '" + userGameId + "';");
		MainExtension.sqlHandler.sendCommand("UPDATE players SET `keys` = '" + player.keys + "' WHERE id = '" + userGameId + "';");
		MainExtension.sqlHandler.sendCommand("UPDATE players SET shards = '" + player.shards + "' WHERE id = '" + userGameId + "';");
		MainExtension.sqlHandler.sendCommand("UPDATE players SET relics = '" + player.relics + "' WHERE id = '" + userGameId + "';");
		MainExtension.sqlHandler.sendCommand("UPDATE players SET starpower = '" + player.starpower + "' WHERE id = '" + userGameId + "';");
		
		user.removeProperty("player_object");
	}

}
