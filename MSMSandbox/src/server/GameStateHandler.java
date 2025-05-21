package server;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import server.Entities.Player;
import server.Entities.SFSObjects;
import server.Tools.Util;

@MultiHandler
public class GameStateHandler extends BaseClientRequestHandler {
	
	public void multiSend(String cmd, SFSArray array, User user, String dbName) {
		SFSObject params = new SFSObject();
		int maxEntries = 99; // 99 is what msm uses
		
		if (array != null) {
			int chunk = 1;
			int numChunks = (array.size() + maxEntries - 1) / maxEntries;
			
			trace("Multi Sending "+cmd+" with "+numChunks+" chunks");
			
	        SFSArray chunkedData = new SFSArray();

	        for(int i = 0; i < array.size(); ++i) {
	           chunkedData.addSFSObject(array.getSFSObject(i));
	           if (chunkedData.size() == maxEntries) {
	               params.putInt("chunk", chunk);
	               params.putInt("numChunks", numChunks);
	               params.putSFSArray(dbName, chunkedData);
	               this.send(cmd, params, user);
	               trace("Sending chunk "+chunk);
	               chunkedData = new SFSArray();
	               ++chunk;
	            }
	         }
	        
            if (chunkedData.size() > 0) {
                params.putInt("chunk", chunk);
                params.putInt("numChunks", numChunks);
                params.putSFSArray(dbName, chunkedData);
                this.send(cmd, params, user);
             }
		}
	}
	
	public SFSArray getSFSArrayQueryData(String db_name) {
	    JSONObject requestJson = new JSONObject();
	    requestJson.put("db_name", db_name);

	    String JSON = Util.PostRequest(MainExtension.DBUrl + "/query/static_db_json/", requestJson.toString());
	    JSONObject json = new JSONObject(JSON);
	    
	    JSONArray dataArray = json.getJSONArray("data");

	    SFSArray extractedArray = SFSArray.newFromJsonData(dataArray.toString());
	    
		return extractedArray;
	}

	public void handleClientRequest(User user, ISFSObject params) {
		//Long bbb_id = (Long) user.getProperty("bbb_id");
        Player player = (Player) user.getProperty("player_object");
        
        SFSObject response = new SFSObject();
        Long serverTime = Util.getUnixTime();
        response.putLong("server_time", serverTime);
        Long lastUpdated = params.getLong("last_updated");
        
        if (lastUpdated == null || lastUpdated > serverTime) {
        	lastUpdated = 0L;
         }
       
        response.putLong("last_updated", lastUpdated);
        
        String cmd = params.getUtfString("__[[REQUEST_ID]]__");
		
        trace(cmd);

		switch (cmd) {
		// db
		case "db_gene":
		    response.putSFSArray("genes_data", getSFSArrayQueryData("genes"));

		    send(cmd, response, user);
		    break;
		case "db_attuner_gene":
		    //response.putSFSArray("attuner_gene_data", getSFSArrayQueryData("attuner_genes"));
			response.putSFSArray("attuner_gene_data", Util.getSFSFromJson(new File("C:\\Users\\Dell\\Downloads\\response\\merged_db_attuner_gene.json")).getSFSArray("attuner_gene_data"));

		    send(cmd, response, user);
			break;
			
		case "db_scratch_offs":
			response.putSFSArray("scratch_offs", getSFSArrayQueryData("scratch_offers"));

		    send(cmd, response, user);
			break;
		case "db_battle":
			response.putSFSArray("battle_campaign_data", new SFSArray());
			send(cmd, response, user);
			break;
		case "db_battle_monster_actions":
			response.putSFSArray("battle_monster_actions_data", new SFSArray());
			
			send(cmd, response, user);
			break;
		case "db_battle_monster_stats":
			response.putSFSArray("battle_monster_stats_data", new SFSArray());

		    send(cmd, response, user);
			break;
		case "db_battle_monster_training":
			response.putSFSArray("battle_monster_training_data", new SFSArray());

		    send(cmd, response, user);
			break;
		case "db_battle_music":
			response.putSFSArray("battle_music_data", new SFSArray());
		    send(cmd, response, user);
			break;
		case "db_island_v2":
			SFSObject islands = MainExtension.client.getChunkByCmdAndNum(cmd, 1);
			//SFSObject islands = Util.getSFSFromJson(new File("D:\\Programs\\SmartFoxServer_2X\\SFS2X\\ServerData\\db_files\\island_v2\\db_island_v2_1.json"));
			
			response.putSFSArray("islands_data", islands.getSFSArray("islands_data"));
		    send(cmd, response, user);
			break;
		case "db_island_themes":
			response.putSFSArray("island_theme_data", new SFSArray()); //getSFSArrayQueryData("island_theme_data")
		    send(cmd, response, user);
			break;
		case "db_bakery_foods":
		    response.putSFSArray("bakery_data", getSFSArrayQueryData("bakery_foods"));

		    send(cmd, response, user);
		    break;
		case "db_monster":
			for (int i = 1; i <= 8; i++) {
				response.putSFSArray("monsters_data", MainExtension.client.getChunkByCmdAndNum(cmd, i).getSFSArray("monsters_data"));
				trace(i);
			    send(cmd, response, user);
			}
			//multiSend(cmd, getSFSArrayQueryData("monsters"), user, "monsters_data");
			break;
		case "db_structure":
			for (int i = 1; i <= 10; i++) {
				response.putSFSArray("structures_data", MainExtension.client.getChunkByCmdAndNum(cmd, i).getSFSArray("structures_data"));
				trace(i);
			    send(cmd, response, user);
			}
			//multiSend(cmd, getSFSArrayQueryData("structures"), user, "structures_data");
			break;
		case "db_costumes":
			for (int i = 1; i <= 5; i++) {
				response.putSFSArray("costume_data", MainExtension.client.getChunkByCmdAndNum(cmd, i).getSFSArray("costume_data"));
				trace(i);
			    send(cmd, response, user);
			}
	    	send(cmd, response, user);
	    	break;
		case "db_entity_alt_costs":
			response.putSFSArray("entity_alt_data",  getSFSArrayQueryData("entity_alt_costs"));
			
			send(cmd, response, user);
			break;
		case "db_store_v2":
            response.putSFSArray("store_item_data", new SFSArray()); // getSFSArrayQueryData("store_items")
            response.putSFSArray("store_group_data", new SFSArray()); // getSFSArrayQueryData("store_groups")
            response.putSFSArray("store_currency_data", new SFSArray()); //getSFSArrayQueryData("store_currencies")
            
            send(cmd, response, user);
            break;
		case "db_store_replacements":
			response.putSFSArray("store_replacement_data", getSFSArrayQueryData("store_replacements"));
			send(cmd, response, user);
			break;
		case "db_titansoul_levels":
			response.putSFSArray("titansoul_level_data", getSFSArrayQueryData("tital_soul_levels"));		
			send(cmd, response, user);
			break;
		case "db_loot":
			send(cmd, response, user);
			break;
		case "db_daily_cumulative_login":
			response.putSFSArray("daily_cumulative_login_data", new SFSArray()); // getSFSArrayQueryData("daily_cumulative_logins")
		    send(cmd, response, user);
			break;
		case "db_nucleus_reward":
			response.putSFSArray("nucleus_reward_data", new SFSArray()); // getSFSArrayQueryData("flex_eggs")
		    send(cmd, response, user);
			break;
		case "db_flexeggdefs":
			response.putSFSArray("flex_egg_def_data", new SFSArray()); // getSFSArrayQueryData("flex_eggs")
		    send(cmd, response, user);
			break;
		case "db_level":
			response.putSFSArray("level_data", getSFSArrayQueryData("levels"));
		    send(cmd, response, user);
			break;
		case "db_ethereal_islet":
			response.putSFSArray("ethereal_islet_data", SFSArray.newFromJsonData("[{\"primary_gene\":\"G\",\"target_cost_3_gene\":3000,\"primordial\":856,\"target_both_cost\":10000,\"island_id\":26,\"dish_harmonizing_cost_per_gene\":2000,\"dish_harmonizing_time_per_gene\":7200,\"target_cost_2_gene\":2000,\"dish_harmonizing_base_time\":72000,\"missing_gene\":\"M\",\"last_changed\":1739289489000}]"));
			send(cmd, response, user);
			break;
		// gs
		case "gs_collect_rewards":
			response.putSFSObject("properties", SFSObjects.properties);
			
			send(cmd, response, user);
			break;
		case "gs_flip_levels":
			response.putSFSArray("flip_levels", getSFSArrayQueryData("flip_levels"));
			send(cmd, response, user);
			break;
		case "gs_flip_boards":
			response.putSFSArray("flip_boards", new SFSArray()); // getSFSArrayQueryData("flip_boards")
			send(cmd, response, user);
			break;
		case "gs_cant_breed":
			response.putSFSArray("monsterIds", new SFSArray());
	        send(cmd, response, user);
			break;
		case "gs_player_has_scratch_off":
			response.putBool("success", true);
			
			send(cmd, response, user);
			break;
		case "gs_process_unclaimed_purchases":
			response.putBool("success", false);
			send(cmd, response, user);
			
			//send("gs_update_properties", response, user);
			break;
		case "gs_handle_facebook_help_instances":
			send(cmd, params, user);
			break;
		case "gs_get_messages":
			response.putBool("success", false);
			
			send(cmd, response, user);
			break;
		case "gs_rare_monster_data":
			response.putSFSArray("rare_monster_data", getSFSArrayQueryData("rare_monster_data"));
			send(cmd, response, user);
			break;
		case "gs_epic_monster_data":
			response.putSFSArray("epic_monster_data", getSFSArrayQueryData("epic_monster_data"));
			send(cmd, response, user);
			break;
		case "gs_monster_island_2_island_data":
			response.putSFSArray("monster_island_2_island_data", getSFSArrayQueryData("monster_home_data"));
			send(cmd, response, user);
			break;
		case "gs_timed_events":
			// TODO
			response.putSFSArray("timed_event_list", new SFSArray());
			
			send(cmd, response, user);
			break;
		case "gs_set_last_timed_themes":
			response.putBool("success", true);
			
			send(cmd, response, user);
			break;
		// ingame
			
		// misc
		case "keep_alive":
			send("keep_alive", new SFSObject(), user);
			break;
		// player
		case "gs_quest":
			response.putSFSArray("result", new SFSArray());
			response.putInt("event_id", 0);
			
			send(cmd, response, user);
			break;
		case "gs_player":
	        if (user.containsProperty("client_version")) {
	        	//player.last_client_version = (String) user.getProperty("client_version");
	        }
	        
	        send("client_keep_alive", new SFSObject(), user);
			
			SFSObject returnObj = new SFSObject();
		    
		    returnObj.putSFSObject("player_object", player.toSFSObject());
		    
			//send(cmd, Util.getSFSFromJson(new File("D:/MSMSandbox/Helpful/default_player_data.json")), user);
			send(cmd, returnObj, user);
			
	        ISFSObject friendsResponse = new SFSObject();
	        ISFSObject globalBattleRankings = new SFSObject();
	        
	        globalBattleRankings.putSFSArray("rankTable0", new SFSArray());
	        globalBattleRankings.putSFSArray("rankTable1", new SFSArray());
	        
	        friendsResponse.putSFSObject("global_battle_rankings", globalBattleRankings);
	        
	        friendsResponse.putBool("success", true);
	        friendsResponse.putSFSArray("requests", new SFSArray());
	        friendsResponse.putSFSArray("tribes", new SFSArray());
	        friendsResponse.putSFSArray("top_tribes", new SFSArray());
	        friendsResponse.putSFSArray("friends", new SFSArray());
	        
	        trace(returnObj.toJson());
	        
	        send("gs_get_friends", friendsResponse, user);
			break;
		default: 
			trace(cmd+" is not implemented!");
			/*
			if (cmd.startsWith("db")) {
				send(cmd, response, user);
			} else if (cmd.startsWith("gs")) {
				send(cmd, new SFSObject(), user);
			}
			*/
			send(cmd, response, user);
	        //response.putUtfString("msg", cmd+" is not added!");

	        //send("gs_display_generic_message", response, user);
			break;
		}
	}
}
