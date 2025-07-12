package server;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSDataWrapper;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import server.Entities.Player;
import server.Entities.PlayerEgg;
import server.Entities.PlayerIsland;
import server.Entities.PlayerIslandFactory;
import server.Entities.PlayerMonster;
import server.Entities.PlayerStructure;
import server.Entities.SFSObjects;
import server.Tools.MSMClient;
import server.Tools.Util;

@MultiHandler
public class GameStateHandler extends BaseClientRequestHandler {
	
	public static String[] quotes = {
		    "Y'know, I might be a terrible person!",
		    "Lets all hug and be friends together!",
		    "CHAOS WILL UNFOLD IF NONAGON'S SMILE FADES.",
		    "Hello! My name is Exclamation Mark. What's yours?",
		    "IT WOULD BE RUDE TO USE SOMEONE ELSE FOR YOUR SELFISH NEEDS.",
		    "A dangerous conspiracist is plotting against us all by warning us by a greater non-existent enemy to try and control us and what we do!",
		    "Yeesh, have a heart mate.",
		    "When taking care of a seed, you need to make sure it gets a lot of attention and love.",
		    "Is death really the answer to all of our problems?",
		    "Lets go pick grass off the ground!",
		    "Hey guys! I'm Exclamation Mark! Anybody like... hopscotch?",
		    "What a whiny critter!",
		    "WEAKLINGS! I'LL EAT IT!",
		    "I am so offended!",
		    "I'm feeling... mischievous.",
		    "Did somebody say 'Bowling Ball'?!",
		    "I HAVE COME TO WARN YOU. ANIMATIC IS APPROACHING AT LIGHT SPEEDS.",
		    "Oh, how I love space! So many stars to burn!",
		    "Praise my artistic prowess!",
		    "LIARS NEED TO BE PUNISHED!",
		    "I guess we shall be.",
		    "...And you're the living embodiment of the little pestering bug that keeps trying to bite me, despite being to insignificant to do anything in life!!",
		    "You're jealous I have better ideas than you.",
		    "I really like carrots.",
		    "Uh, Club. Crackers.",
		    "What have I done...",
		    "Ra rurahruh. Ru ra!",
		    "Yay, uppies!"
		};

	private static final Random random = new Random();

	public static String getRandomQuote() {
		return quotes[random.nextInt(quotes.length)];
	}

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
	
	public static long getFirstValidUserStructureId(long[] structureIds, Player player, String userGameId) {
	    for (long structureId : structureIds) {
	        String query = String.format(
	            "SELECT user_structure_id FROM player_structures WHERE user_island_id = '%s' AND structure_id = '%d' AND user_game_id = '%s' LIMIT 1;",
	            player.active_island, structureId, userGameId
	        );

	        JSONObject result = new JSONObject(MainExtension.sqlHandler.sendCommand(query));
	        JSONArray resArray = result.getJSONArray("result");

	        if (resArray.length() > 0 && resArray.getJSONArray(0).length() > 0) {
	            long userStructureId = resArray.getJSONArray(0).getLong(0);
	            if (userStructureId != 0) {
	                return userStructureId;
	            }
	        }
	    }

	    return 0;
	}
	
	public void playScratch(SFSObject params, User user, int active_island_type, String cmd) {
		SFSObject response = new SFSObject();
		if (params.getUtfString("type").equals("S")) {
	        response.putUtfString("msg", "Spin wheel isn't added yet!");

	        send("gs_display_generic_message", response, user);
		} else {
	        SFSObject ticket = new SFSObject();

	        Random rnd = new Random();
	        
	        int[][] monsters = {
	        	    {},
	        	    {188, 490, 632, 272, 466, 1112},
	        	    {2, 3, 6, 1, 10, 9}
	        	};

	        ticket.putInt("amount", monsters[active_island_type][new Random().nextInt(monsters[active_island_type].length)]);
	        ticket.putUtfString("prize", "monster");
	        ticket.putInt("id", 17);
	        ticket.putUtfString("type", "M");
	        ticket.putInt("is_top_prize", 1);

	        response.putBool("success", true);
	        response.putSFSObject("ticket", ticket);
			send(cmd, response, user);
		}
	}
	
	public SFSObject buyEntity(Player player, SFSObject params) {
		SFSObject response = new SFSObject();
		SFSObject priceObject = null;
		if (params.getInt("monster_id") != null) {
			priceObject = MainExtension.client.getPriceOfMonster(params.getInt("monster_id"));
		} else if (params.getInt("structure_id") != null) {
			priceObject = MainExtension.client.getPriceOfStructure(params.getInt("structure_id"));
		}
        if (!priceObject.getBool("ok")) {
        	response.putBool("ok", false);
	        response.putUtfString("msg", "An error occured while purchasing the entity:\n"+priceObject.getUtfString("error"));
        	return response;
        }
        
        int amountRemove = priceObject.getInt("cost");
        
        String currency = priceObject.getUtfString("currency");
        
        if (currency.equals("coins")) {
            player.removeCoins(amountRemove);
        } else if (currency.equals("diamonds")) {
            player.removeDiamonds(amountRemove);
        } else if (currency.equals("keys")) {
            player.removeKeys(amountRemove);
        } else if (currency.equals("eth_currency")) {
            player.removeShards(amountRemove);
        } else if (currency.equals("relics")) {
            player.removeRelics(amountRemove);
        } else if (currency.equals("starpower")) {
            player.removeStarpower(amountRemove);
        } else {
        	response.putBool("ok", false);
	        response.putUtfString("msg", "An error occured while purchasing the entity!\n\nDebug info:\n"+currency+"\nError 2");
	        
	        trace("Error occured: 2");
	        return response;
        }
        
        response.putBool("ok", true);
        
        return response;
	}
	
	private SFSObject bbbIdToFriend(long bbb_id) {
	    SFSObject friend = new SFSObject();

	    JSONArray userResult = new JSONObject(MainExtension.sqlHandler.sendCommand(
	        "SELECT user_game_id FROM users WHERE bbb_id = '" + bbb_id + "';"
	    )).getJSONArray("result");

	    if (userResult.length() == 0) {
	        trace("bbbIdToFriend failed: no user_game_id found for bbb_id " + bbb_id);
	        return null;
	    }

	    JSONArray firstRow = userResult.getJSONArray(0);
	    String userGameId = firstRow.getString(0);

	    JSONArray outerArray = new JSONObject(MainExtension.sqlHandler.sendCommand(
	        "SELECT nickname FROM players WHERE id = '" + userGameId + "';"
	    )).getJSONArray("result");

	    JSONArray innerArray = outerArray.getJSONArray(0);
	    String nickname = innerArray.length() > 0 ? innerArray.getString(0) : "???";

	    friend.putBool("is_favorite", false);
	    friend.putInt("lostBattles", 0);
	    friend.putInt("canPvp", 0);
	    friend.putInt("level", 100);
	    friend.putUtfString("pp_info", "0");
	    friend.putInt("litByFriend", 0);
	    friend.putUtfString("display_name", nickname != null ? nickname : "???");
	    friend.putInt("wonBattles", 0);
	    friend.putInt("prev_rank", 0);
	    friend.putInt("pp_type", 0);
	    friend.putInt("tier", -1);
	    friend.putLong("bbb_id", bbb_id);
	    friend.putLong("user_id", bbb_id);
	    friend.putInt("litByMe", 0);
	    friend.putInt("prev_tier", -1);
	    friend.putInt("battle_level", 1);
	    friend.putInt("rank", 1);
	    friend.putBool("has_unlit_torches", false);

	    return friend;
	}

	private SFSObject getFriends(User user) {
	    Player player = (Player) user.getProperty("player_object");
	    SFSObject friendsResponse = new SFSObject();
	    SFSObject globalBattleRankings = new SFSObject();

	    globalBattleRankings.putSFSArray("rankTable0", new SFSArray());
	    globalBattleRankings.putSFSArray("rankTable1", new SFSArray());

	    friendsResponse.putSFSObject("global_battle_rankings", globalBattleRankings);
	    friendsResponse.putBool("success", true);
	    friendsResponse.putSFSArray("requests", new SFSArray());
	    friendsResponse.putSFSArray("tribes", new SFSArray());
	    friendsResponse.putSFSArray("top_tribes", new SFSArray());

	    // Get friend pairs
	    JSONObject friendReq = new JSONObject(MainExtension.sqlHandler.sendCommand(
	        "SELECT user_1, user_2 FROM user_friends WHERE user_1 = '" + player.bbb_id + "' OR user_2 = '" + player.bbb_id + "';"
	    ));
	    
	    JSONArray friendRows = friendReq.getJSONArray("result");
	    List<Long> friendIds = new ArrayList<>();

	    for (int i = 0; i < friendRows.length(); i++) {
	        JSONArray row = friendRows.getJSONArray(i);
	        long user1 = row.getLong(0);
	        long user2 = row.getLong(1);
	        long friendBbbId = (user1 == player.bbb_id) ? user2 : user1;
	        friendIds.add(friendBbbId);
	    }

	    if (friendIds.isEmpty()) {
	        friendsResponse.putSFSArray("friends", new SFSArray());
	        return friendsResponse;
	    }

	    // Batch get user_game_ids
	    String idsInClause = friendIds.toString().replace("[", "(").replace("]", ")");
	    JSONObject userGameIdRes = new JSONObject(MainExtension.sqlHandler.sendCommand(
	        "SELECT bbb_id, user_game_id FROM users WHERE bbb_id IN " + idsInClause + ";"
	    ));

	    Map<Long, String> bbbToGameId = new HashMap<>();
	    JSONArray userGameRows = userGameIdRes.getJSONArray("result");
	    for (int i = 0; i < userGameRows.length(); i++) {
	        JSONArray row = userGameRows.getJSONArray(i);
	        bbbToGameId.put(row.getLong(0), row.getString(1));
	    }

	    // Batch get nicknames
	    String gameIdsInClause = bbbToGameId.values().stream()
	        .map(id -> "'" + id + "'")
	        .collect(Collectors.joining(",", "(", ")"));
	    JSONObject nickRes = new JSONObject(MainExtension.sqlHandler.sendCommand(
	        "SELECT id, nickname FROM players WHERE id IN " + gameIdsInClause + ";"
	    ));

	    Map<String, String> gameIdToNickname = new HashMap<>();
	    JSONArray nickRows = nickRes.getJSONArray("result");
	    for (int i = 0; i < nickRows.length(); i++) {
	        JSONArray row = nickRows.getJSONArray(i);
	        gameIdToNickname.put(row.getString(0), row.getString(1));
	    }

	    // Assemble friend data
	    SFSArray friendList = new SFSArray();
	    for (long bbbId : friendIds) {
	        String gameId = bbbToGameId.get(bbbId);
	        String nickname = (gameId != null) ? gameIdToNickname.getOrDefault(gameId, "???") : "???";

	        SFSObject friend = new SFSObject();
	        friend.putBool("is_favorite", false);
	        friend.putInt("lostBattles", 0);
	        friend.putInt("canPvp", 0);
	        friend.putInt("level", 100);
	        friend.putUtfString("pp_info", "0");
	        friend.putInt("litByFriend", 0);
	        friend.putUtfString("display_name", nickname);
	        friend.putInt("wonBattles", 0);
	        friend.putInt("prev_rank", 0);
	        friend.putInt("pp_type", 0);
	        friend.putInt("tier", -1);
	        friend.putLong("bbb_id", bbbId);
	        friend.putLong("user_id", bbbId);
	        friend.putInt("litByMe", 0);
	        friend.putInt("prev_tier", -1);
	        friend.putInt("battle_level", 1);
	        friend.putInt("rank", 1);
	        friend.putBool("has_unlit_torches", false);

	        friendList.addSFSObject(friend);
	    }

	    friendsResponse.putSFSArray("friends", friendList);
	    return friendsResponse;
	}

	public void handleClientRequest(User user, ISFSObject params) {
		//Long bbb_id = (Long) user.getProperty("bbb_id");
        Player player = (Player) user.getProperty("player_object");
        String userGameId = (String) user.getProperty("user_game_id");       
        SFSObject response = new SFSObject();
        Long serverTime = Util.getUnixTime();
        response.putLong("server_time", serverTime);
        Long lastUpdated = params.getLong("last_updated");
        
        if (lastUpdated == null || lastUpdated > serverTime) {
        	lastUpdated = 0L;
         }
        
        int user_count = getParentExtension().getParentZone().getUserCount();
        
        Zone zone = getParentExtension().getParentZone();
        
        SFSArray properties = new SFSArray();
        properties.addSFSObject(SFSObjects.properties);
       
        response.putLong("last_updated", lastUpdated);
        
        String cmd = params.getUtfString("__[[REQUEST_ID]]__");
        
        SFSObject response2 = new SFSObject();
		
        trace(cmd);
        
		switch (cmd) {
		// db
		case "db_gene":
			response.putSFSArray("genes_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("genes_data"));

		    send(cmd, response, user);
		    break;
		case "db_attuner_gene":
			response.putSFSArray("attuner_gene_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("attuner_gene_data"));

		    send(cmd, response, user);
			break;
			
		case "db_scratch_offs":
			response.putSFSArray("scratch_offs", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("scratch_offs"));

		    send(cmd, response, user);
			break;
		case "db_battle":
			response.putSFSArray("battle_campaign_data", new SFSArray());
			send(cmd, response, user);
			break;
		case "db_battle_levels":
			response.putSFSArray("battle_level_data", new SFSArray());
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
			response.putSFSArray("islands_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("islands_data"));
		    send(cmd, response, user);
			break;
		case "db_island_themes":
			response.putSFSArray("island_theme_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("island_theme_data"));//getSFSArrayQueryData("island_theme_data")
		    send(cmd, response, user);
			break;
		case "db_bakery_foods":
		    response.putSFSArray("bakery_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("bakery_data"));

		    send(cmd, response, user);
		    break;
		case "db_monster":
		    for (int i = 1; i <= 8; i++) {
		        SFSObject chunk = MainExtension.client.getChunkByCmdAndNum(cmd, i);
		        if (chunk == null) continue;
		        
		        SFSArray originalMonsters = (SFSArray) chunk.getSFSArray("monsters_data");
		        
		        SFSArray clonedMonsters = Util.cloneSFSArray(originalMonsters);

		        if (player.direct_place == 1) {
		            for (int j = 0; j < clonedMonsters.size(); j++) {
		                SFSObject monster = (SFSObject) clonedMonsters.getSFSObject(j);
		                monster.putUtfString("genes", "Q");
		            }
		        }
		        response.putSFSArray("monsters_data", clonedMonsters);

		        trace(i);
		        send(cmd, response, user);
		    }
		    break;

		case "db_structure":
			for (int i = 1; i <= 10; i++) {
				response.putSFSArray("structures_data", MainExtension.client.getChunkByCmdAndNum(cmd, i).getSFSArray("structures_data"));
				trace(i);
			    send(cmd, response, user);
			}
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
			response.putSFSArray("entity_alt_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("entity_alt_data"));
			
			send(cmd, response, user);
			break;
		case "db_store_v2":
            response.putSFSArray("store_item_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("store_item_data")); // getSFSArrayQueryData("store_items")
            response.putSFSArray("store_group_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("store_group_data")); // getSFSArrayQueryData("store_groups")
            response.putSFSArray("store_currency_data", new SFSArray()); //getSFSArrayQueryData("store_currencies")
            
            send(cmd, response, user);
            break;
		case "db_store_replacements":
			response.putSFSArray("store_replacement_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("store_replacement_data"));
			send(cmd, response, user);
			break;
		case "db_titansoul_levels":
			response.putSFSArray("titansoul_level_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("titansoul_level_data"));		
			send(cmd, response, user);
			break;
		case "db_loot":
			send(cmd, response, user);
			break;
		case "db_breeding":
			/*
			for (int i = 1; i <= 10; i++) {
				response.putSFSArray("breeding_data", MainExtension.client.getChunkByCmdAndNum(cmd, i).getSFSArray("breeding_data"));
				trace(i);
			    send(cmd, response, user);
			}
			*/
			send(cmd, response, user);
			break;
		case "db_polarity_amplifier_levels":
			send(cmd, response, user);
			break;
		case "db_daily_cumulative_login":
			response.putSFSArray("daily_cumulative_login_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("daily_cumulative_login_data")); // getSFSArrayQueryData("daily_cumulative_logins")
		    send(cmd, response, user);
			break;
		case "db_nucleus_reward":
			response.putSFSArray("nucleus_reward_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("nucleus_reward_data")); // getSFSArrayQueryData("flex_eggs")
		    send(cmd, response, user);
			break;
		case "db_flexeggdefs":
			response.putSFSArray("flex_egg_def_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("flex_egg_def_data")); // getSFSArrayQueryData("flex_eggs")
		    send(cmd, response, user);
			break;
		case "db_level":
			response.putSFSArray("level_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("level_data"));
		    send(cmd, response, user);
			break;
		case "db_ethereal_islet":
			response.putSFSArray("ethereal_islet_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("ethereal_islet_data"));
			send(cmd, response, user);
			break;
		// gs
		case "gs_collect_rewards":
			response.putSFSObject("properties", SFSObjects.properties);
			
			send(cmd, response, user);
			break;
		case "gs_flip_levels":
			response.putSFSArray("flip_levels", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("flip_levels"));
			send(cmd, response, user);
			break;
		case "gs_flip_boards":
			response.putSFSArray("flip_boards", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("flip_boards")); // getSFSArrayQueryData("flip_boards")
			send(cmd, response, user);
			break;
		case "gs_cant_breed":
			SFSDataWrapper wrapper = MainExtension.client.getChunkByCmdAndNum(cmd, 1).get("monsterIds");

			Object rawObject = wrapper.getObject();

			if (rawObject instanceof ArrayList<?>) {
			    ArrayList<?> rawList = (ArrayList<?>) rawObject;
			    ISFSArray monsterIdsArray = new SFSArray();

			    for (Object item : rawList) {
			        if (item instanceof Integer) {
			            monsterIdsArray.addInt((Integer) item);
			        } else {
			            trace("Non-integer in monsterIds: " + item);
			        }
			    }

			    response.putSFSArray("monsterIds", monsterIdsArray);
			    send(cmd, response, user);

			} else {
			    trace("Expected ArrayList but got: " + rawObject.getClass().getName());
			}

			break;
		case "gs_player_has_scratch_off":
			response.putBool("success", true);
			
			send(cmd, response, user);
			break;
		case "gs_update_island_tutorials":
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
			response.putSFSArray("rare_monster_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("rare_monster_data"));
			send(cmd, response, user);
			break;
		case "gs_epic_monster_data":
			response.putSFSArray("epic_monster_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("epic_monster_data"));
			send(cmd, response, user);
			break;
		case "gs_monster_island_2_island_data":
			response.putSFSArray("monster_island_2_island_data", MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSArray("monster_island_2_island_data"));
			send(cmd, response, user);
			break;
		case "gs_timed_events":
			SFSArray timedEvents = new SFSArray();
			
			if (MainExtension.timedEventsCache.toJson().equals("[]")) {
				SFSArray entityIds = MainExtension.client.getAllMonsterEntityIds();
				SFSArray entityStructureIds = MainExtension.client.getAllStructureEntityIds();

				for (int i = 0; i < entityStructureIds.size(); i++) {
				    entityIds.addInt((int) entityStructureIds.getElementAt(i));
				}
				
				long endDate = serverTime + (1000L * 60 * 60 * 24 * 365);
				
				for (int i = 0; i < entityIds.size(); i++) {
				    int entityId = entityIds.getInt(i);
				    SFSObject timedEntity = new SFSObject();
				    timedEntity.putLong("end_date", endDate);
				    timedEntity.putLong("last_updated", serverTime);
				    timedEntity.putUtfString("event_type", "EntityStoreAvailability");
				    timedEntity.putInt("event_id", 3);
				    
				    SFSArray timedEntityDataArray = new SFSArray();
				    
				    SFSObject timedEntityData = new SFSObject();
				    timedEntityData.putInt("entity", entityId);
				    
				    timedEntityDataArray.addSFSObject(timedEntityData);
				    
				    timedEntity.putSFSArray("data", timedEntityDataArray);
				    timedEntity.putLong("id", 200000 + i);
				    timedEntity.putLong("start_date", 1750460220);
				    
				    timedEvents.addSFSObject(timedEntity);
				}
			} else {
				timedEvents = MainExtension.timedEventsCache;
			}
			
			response.putSFSArray("timed_event_list", timedEvents);
			
			send(cmd, response, user);
			break;
		case "gs_set_last_timed_themes":
			response.putBool("success", true);
			
			send(cmd, response, user);
			break;
		// ingame
		case "gs_get_torchgifts":
			send(cmd, response, user);
			break;
		case "gs_light_torch":
			send(cmd, response, user);
			break;
		case "gs_breed_monsters":
			
			send(cmd, response, user);
			break;
		case "gs_process_unclaimed_codes":
			response.putBool("success", true);
			
			send(cmd, response, user);
			break;
		case "gs_refresh_tribe_requests":
			response.putBool("success", true);
			response.putSFSArray("invites", new SFSArray());
			
			send(cmd, response, user);
			break;
		case "gs_multi_neighbors":
			response.putBool("success", false);
	        //send("gs_multi_update_monster", response, user);
	        break;
		case "gs_buy_structure":
	        SFSObject successStructure = buyEntity(player, (SFSObject) params);
	        
	        if (!successStructure.getBool("ok")) {
		        response.putUtfString("msg", successStructure.getUtfString("msg"));

		        //send("gs_display_generic_message", response, user);
		        //break;
	        }
			long user_structure_id = PlayerIslandFactory.getNextUserStructureId(userGameId);
			MainExtension.sqlHandler.sendCommand(
				    "INSERT INTO player_structures (user_island_id, structure_id, user_structure_id, user_game_id, pos_x, pos_y, scale, flip, name, date_created) VALUES ('"
				    + player.active_island + "', '"
				    + params.getInt("structure_id") + "', '"
				    + user_structure_id + "', '"
				    + userGameId + "', '"
				    + params.getInt("pos_x") + "', '"
				    + params.getInt("pos_y") + "', '"
				    + params.getDouble("scale") + "', '"
				    + params.getInt("flip") + "', '"
				    + "Structure" + "', '"
				    + serverTime + "');"
				);
			
			trace("Buying structure for user_island_id: " + player.active_island);
			
			PlayerStructure structure = PlayerStructure.createNewStructure(serverTime, serverTime, player.active_island, (int) user_structure_id, params.getInt("structure_id"), params.getInt("pos_x"), params.getInt("pos_y"));
			structure.flip = params.getInt("flip");
			structure.scale = params.getDouble("scale");
			
			response.putBool("success", true);

			response.putSFSArray("properties",  player.getProperties());
			response.putBool("success", true);
			response.putSFSObject("user_structure", structure.toSFSObject());
			send(cmd, response, user);
			break;
		case "gs_sell_structure":
	        MainExtension.sqlHandler.sendCommand(
	        	    "DELETE FROM player_structures WHERE user_structure_id = '" + params.getLong("user_structure_id") + "' AND user_game_id = '" + userGameId + "';"
	        	);
	        
			response.putLong("user_structure_id", params.getLong("user_structure_id"));
			response.putBool("success", true);
			response.putSFSArray("properties", player.getProperties());
			
			send(cmd, response, user);
			break;
		case "gs_start_upgrade_structure":
			response.putLong("user_structure_id", params.getLong("user_structure_id"));
			SFSArray responseVars = new SFSArray();
			
			SFSObject property = new SFSObject();
			property.putInt("is_complete", 0);
			responseVars.addSFSObject(property);
			
			property = new SFSObject();
			property.putInt("is_upgrading", 1);
			responseVars.addSFSObject(property);
			
			property = new SFSObject();
			property.putLong("date_created", serverTime);
			responseVars.addSFSObject(property);
			
			property = new SFSObject();
			property.putLong("building_completed", serverTime);
			responseVars.addSFSObject(property);
			
			property = new SFSObject();
			property.putSFSArray("player_property_data", properties);
			responseVars.addSFSObject(property);
			
			response.putSFSArray("properties", responseVars);

			send("gs_update_structure", response, user);
			break;
		case "gs_finish_upgrade_structure":
			response.putLong("user_structure_id", params.getLong("user_structure_id"));
			response.putBool("success", true);
			response.putSFSObject("user_structure", PlayerStructure.createFromDatabase(params.getLong("user_structure_id"), userGameId).toSFSObject());
			response.putSFSArray("properties", properties);
			send(cmd, response, user);
			break;
		case "gs_move_structure":
			MainExtension.sqlHandler.sendCommand(
				    "UPDATE player_structures SET pos_x = '" + params.getInt("pos_x") +
				    "' WHERE user_structure_id = '" + params.getLong("user_structure_id") + "' AND user_game_id = '" + userGameId + "';"
				);
			MainExtension.sqlHandler.sendCommand(
				    "UPDATE player_structures SET pos_y = '" + params.getInt("pos_y") +
				    "' WHERE user_structure_id = '" + params.getLong("user_structure_id") + "' AND user_game_id = '" + userGameId + "';"
				);
			properties = new SFSArray();
			
			SFSObject prop = new SFSObject();
			
			prop.putInt("pos_x", params.getInt("pos_x"));
			properties.addSFSObject(prop);
			
			prop = new SFSObject();
			
			prop.putInt("pos_y", params.getInt("pos_y"));
			properties.addSFSObject(prop);
			
			response2.putSFSArray("properties", properties);
			response2.putLong("user_structure_id", params.getLong("user_structure_id"));
			response.putBool("success", true);
			send("gs_update_structure", response2, user);
			send(cmd, response, user);
			break;
		case "gs_flip_structure":
			response2.putSFSArray("properties", player.getProperties());
			response2.putLong("user_structure_id", params.getLong("user_structure_id"));
			response2.putInt("flip", params.getBool("flipped")?1:0);
			MainExtension.sqlHandler.sendCommand(
				    "UPDATE player_structures SET flip = '" + (params.getBool("flipped") ? 1 : 0) +
				    "' WHERE user_structure_id = '" + params.getLong("user_structure_id") + "' AND user_game_id = '" + userGameId + "';"
				);
			send("gs_update_structure", response2, user);
			response.putBool("success", true);
			send(cmd, response, user);
			break;
		case "collect_daily_cumulative_login_rewards":
			SFSObject dailyState = new SFSObject();
		    if (player.daily_cumulative_login_amt >= 32) {
		        player.daily_cumulative_login_calender++;
		        player.daily_cumulative_login_amt = 0;
		    } else {
		        player.daily_cumulative_login_amt++;
		    }
			
			dailyState.putInt("calendar_id", player.daily_cumulative_login_calender);
			dailyState.putInt("reward_idx", player.daily_cumulative_login_amt);
			dailyState.putLong("next_collect", serverTime);
			dailyState.putInt("total", 32);
			response.putBool("success", true);
			response.putSFSObject("state", dailyState);
			send(cmd, response, user);
			
			SFSObject update = new SFSObject();
			update.putSFSArray("timed_events", MainExtension.timedEventsCache);
			
			properties.addSFSObject(update);
			
			response2.putLong("server_time", serverTime);
			response2.putSFSArray("properties", properties);
			
			send("gs_update_properties", response2, user);
			break;
		case "update_awakener":
			response.putBool("success", true);
			response.putInt("awakened_state", params.getInt("awakened_state"));
			send(cmd, response, user);
			break;
		case "gs_buy_egg":
			trace(params.getDump());
	        long structure_id = 0L;
	        
	        if (params.containsKey("nursery_id")) {
	        	structure_id = params.getLong("nursery_id");
	        } else if (params.containsKey("structure_id")) {
	        	structure_id = params.getLong("structure_id");
	        }
	        
	        if (structure_id == 0) {
	            structure_id = getFirstValidUserStructureId(new long[] {1, 239, 286, 857, 925, 535, 602}, player, userGameId);
	        }
	        
	        if (structure_id == 0) {
	            response.putUtfString("msg", "No nursery found on your island. Don't cry to me, use \"Buy Egg\" in your nursery.");
	            send("gs_display_generic_message", response, user);
	            break;
	        }
	        
	        SFSObject successEgg = buyEntity(player, (SFSObject) params);
	        
	        if (!successEgg.getBool("ok")) {
		        response.putUtfString("msg", successEgg.getUtfString("msg"));

		        send("gs_display_generic_message", response, user);
		        break;
	        }
	        
	        int egg_id;

	        synchronized (MainExtension.eggLock) {
	            JSONArray resultArray = new JSONObject(MainExtension.sqlHandler.sendCommand(
	                    "SELECT MAX(user_egg_id) FROM player_eggs WHERE user_island_id = '"+player.active_island+"';"
	                )).getJSONArray("result").getJSONArray(0);

	            if (resultArray.isNull(0)) {
	                egg_id = 1;
	            } else {
	            	egg_id = resultArray.getInt(0) + 1;
	            }

	            MainExtension.sqlHandler.sendCommand("INSERT INTO player_eggs (user_egg_id, monster_id, user_game_id, user_island_id) VALUES ('" + egg_id + "', '" + params.getInt("monster_id") + "', '" + userGameId + "', '"+player.active_island+"');");
	        }
			
			SFSObject egg = PlayerEgg.createNewEgg(player.active_island, structure_id, params.getInt("monster_id"), serverTime, egg_id).toSFSObject();
            
            response.putSFSArray("properties",  player.getProperties());
            response.putSFSObject("user_egg", egg);
            response.putBool("success", true);
            response.putBool("remove_buyback", false);
            trace(response.getDump());
            send(cmd, response, user);
			break;
		case "gs_hatch_egg":
		    Long userEggId = params.getLong("user_egg_id");
		    
		    JSONObject eggQuery = new JSONObject(MainExtension.sqlHandler.sendCommand(
		        "SELECT monster_id FROM player_eggs WHERE user_egg_id = '" + userEggId + "' AND user_island_id = '"+player.active_island+"';")
		    );

		    int monster_id;
		    JSONArray resultArray = eggQuery.getJSONArray("result");
		    
		    boolean directPlace = false;
		    
		    if (userEggId <= 0) {
		        response.putUtfString("msg", "Invalid egg ID: " + userEggId);
		        send("gs_display_generic_message", response, user);
		        break;
		    }
		    
		    if (resultArray.length() == 0) {
		    	SFSObject genes = MainExtension.client.getGenesOfMonster((int) userEggId.longValue());
		    	if (!genes.getBool("ok")) {
			        response.putUtfString("msg", "An error occured purchasing the monster: "+genes.getUtfString("error"));

			        send("gs_display_generic_message", response, user);
		        	break;
		    	}
		    	
		    	directPlace = true;
		    	
		    	monster_id = (int) userEggId.longValue();
		    } else {
		    	monster_id = resultArray.getJSONArray(0).getInt(0);
		    }
			
			synchronized (MainExtension.eggLock) {		
				String query = "SELECT IFNULL(MAX(user_monster_id), -1) + 1 FROM player_monsters WHERE user_game_id = '" + userGameId + "';";
				String result = MainExtension.sqlHandler.sendCommand(query);
				long user_monster_id = new JSONObject(result).getJSONArray("result").getJSONArray(0).getLong(0);
				
				String monsterName = "Made by @riotlove_official on YouTube";
	
				PlayerMonster monster = PlayerMonster.createNewMonster(
					    player.active_island,
					    monster_id,
					    params.getInt("pos_x"),
					    params.getInt("pos_y"),
					    params.getInt("flip"),
					    user_monster_id,
					    monsterName
					);
				
				MainExtension.sqlHandler.sendCommand("INSERT INTO player_monsters (user_island_id, monster_id, user_monster_id, user_game_id, pos_x, pos_y, muted, flip, name, date_created) VALUES ('"
					    + player.active_island + "', '"
					    + monster_id + "', '"
					    + user_monster_id + "', '"
					    + userGameId + "', '"
					    + params.getInt("pos_x") + "', '"
					    + params.getInt("pos_y") + "', '"
					    + 0 + "', '"
					    + params.getInt("flip") + "', '"
					    + monsterName + "', '"
					    + serverTime + "');");
	
	            response.putSFSArray("properties",  player.getProperties());
	            
	            response.putLong("user_egg_id", params.getLong("user_egg_id"));
	            response.putLong("island", player.active_island);
	            response.putSFSObject("monster", monster.toSFSObject());
	            response.putBool("create_in_storage", false);
	            response.putBool("success", true);
	            response.putBool("directPlace", directPlace);
			}
			
            send(cmd, response, user);
			break;
		case "gs_sell_egg":			
	        SFSObject priceObject2 = MainExtension.client.getPriceOfMonster(new JSONObject(MainExtension.sqlHandler.sendCommand(
			        "SELECT monster_id FROM player_eggs WHERE user_egg_id = '" + params.getLong("user_egg_id") + "' AND user_island_id = '"+player.active_island+"';")
			    ).getJSONArray("result").getJSONArray(0).getInt(0), "coins");
	        
	        if (!priceObject2.getBool("ok")) {
		        response.putUtfString("msg", "An error occured while selling the egg:\n"+priceObject2.getUtfString("error"));

		        send("gs_display_generic_message", response, user);
		        trace("Error occured: 1");
	        	break;
	        }
	        
	        MainExtension.sqlHandler.sendCommand("DELETE FROM player_eggs WHERE user_egg_id = '" + params.getLong("user_egg_id") + "' AND user_island_id = '"+player.active_island+"';");
	        
	        int amountToAdd = (int) Math.round(priceObject2.getInt("cost") * 0.75);
	        
	        player.removeCoins(-amountToAdd);
			
			response.putLong("user_egg_id", params.getLong("user_egg_id"));
			response.putBool("success", true);
			response.putSFSArray("properties",  player.getProperties());
			send(cmd, response, user);
			break;
		case "gs_viewed_egg":
			response2.putLong("island_id", player.active_island);
			
			JSONObject queryResult = new JSONObject(MainExtension.sqlHandler.sendCommand(
				    "SELECT monster_id FROM player_eggs WHERE user_egg_id = '" + params.getLong("user_egg_id") + "' AND user_island_id = '"+player.active_island+"';"
				));

			resultArray = queryResult.getJSONArray("result");

			if (resultArray.length() == 0) {
				response2.putUtfString("monsters_sold", "[]");
			} else {
				int monsterId = resultArray.getJSONArray(0).getInt(0);
				response2.putUtfString("monsters_sold", "[" + monsterId + "]");
			}
				
			send("gs_update_sold_monsters", response2, user);
            response.putBool("success", true);
            send(cmd, response, user);
			break;
		case "gs_mega_monster_message":
			response2.putSFSArray("properties",  player.getProperties());
			response2.putLong("user_monster_id", params.getLong("user_monster_id"));
			response2.putSFSObject("megamonster", SFSObject.newFromJsonData("{\"permamega\":true,\"currently_mega\":"+(params.containsKey("mega_enable")?params.getBool("mega_enable"):true?"true":"false")+"}"));
			response.putBool("success", true);
			MainExtension.sqlHandler.sendCommand(
					"UPDATE player_monsters SET big = '" + (params.containsKey("mega_enable") && params.getBool("mega_enable") ? 1 : 0) +
					"' WHERE user_monster_id = '" + params.getLong("user_monster_id") + "' AND user_game_id = '" + userGameId + "';"
					);
			send("gs_update_monster", response2, user);
			send(cmd, response, user);
			break;
		case "gs_flip_monster":
			response2.putLong("user_monster_id", params.getLong("user_monster_id"));
			response2.putInt("flip", params.getBool("flipped")?1:0);
			MainExtension.sqlHandler.sendCommand(
				    "UPDATE player_monsters SET flip = '" + (params.getBool("flipped") ? 1 : 0) +
				    "' WHERE user_monster_id = '" + params.getLong("user_monster_id") + "' AND user_game_id = '" + userGameId + "';"
				);
			send("gs_update_monster", response2, user);
			response.putBool("success", true);
			send(cmd, response, user);
			break;
		case "gs_move_monster":
			MainExtension.sqlHandler.sendCommand(
				    "UPDATE player_monsters SET pos_x = '" + params.getInt("pos_x") + 
				    "', pos_y = '" + params.getInt("pos_y") + 
				    "' WHERE user_monster_id = '" + params.getLong("user_monster_id") + 
				    "' AND user_game_id = '" + userGameId + "';"
				);
			//MainExtension.sqlHandler.sendCommand(
			//	    "UPDATE player_monsters SET volume = '" + params.getDouble("volume") +
			//	    "' WHERE user_monster_id = '" + params.getLong("user_monster_id") + "';"
			//	);
			response2.putLong("user_monster_id", params.getLong("user_monster_id"));
			response2.putInt("pos_x", params.getInt("pos_x"));
			response2.putInt("pos_y", params.getInt("pos_y"));
			response2.putDouble("volume", params.getDouble("volume"));
			response.putBool("success", true);
			send("gs_update_monster", response2, user);
			send(cmd, response, user);
			break;
		case "gs_sell_monster":
	        SFSObject priceObject3 = MainExtension.client.getPriceOfMonster(new JSONObject(MainExtension.sqlHandler.sendCommand(
	        		"SELECT monster_id FROM player_monsters WHERE user_monster_id = '" + params.getLong("user_monster_id") + "' AND user_game_id = '" + userGameId + "';")
			    ).getJSONArray("result").getJSONArray(0).getInt(0), "coins");
	        
	        if (!priceObject3.getBool("ok")) {
		        response.putUtfString("msg", "An error occured while selling the egg:\n"+priceObject3.getUtfString("error"));

		        send("gs_display_generic_message", response, user);
		        trace("Error occured: 1");
	        	break;
	        }
	        
	        MainExtension.sqlHandler.sendCommand(
	        	    "DELETE FROM player_monsters WHERE user_monster_id = '" + params.getLong("user_monster_id") + "' AND user_game_id = '" + userGameId + "';"
	        	);
	        
	        player.removeCoins(-(int) Math.round(priceObject3.getInt("cost") * 0.75));
			response2.putSFSArray("properties",  player.getProperties());
			response.putLong("user_monster_id", params.getLong("user_monster_id"));
			response.putBool("success", true);
			send(cmd, response, user);
			break;
		case "gs_flip_minigame_cost":
		    response.putBool("success", true);
		    response.putInt("diamond_cost", 2);
		    response.putInt("coin_cost", 200000);
		    send(cmd, response, user);
		    break;
		case "gs_purchase_flip_mini_game":
			player.removeCoins(200000);
			response.putSFSArray("properties",  player.getProperties());
			response.putBool("success", true);
			response.putLong("flipGameTime", serverTime + 60);
			response.putInt("level", 1);
			response.putInt("level_id", 1);
			response.putSFSObject("ingame_reward", new SFSObject());
			response.putSFSArray("scaled_endgame_rewards", new SFSArray());
			
			send(cmd, response, user);
			break;
		case "gs_purchase_scratch_off":
			if (params.getUtfString("type").equals("S")) {
		        response.putUtfString("msg", "Spin wheel isn't added yet!");

		        send("gs_display_generic_message", response, user);
				break;
			}
			break;
		case "gs_play_scratch_off":
			if (params.getUtfString("type").equals("S")) {
		        response.putUtfString("msg", "Spin wheel isn't added yet!");

		        send("gs_display_generic_message", response, user);
				break;
			}
            SFSObject ticket = new SFSObject();
            
            int[][] monsters = {
            	    {},
            	    {2,3,4,5},
            	    {2, 3, 6, 1, 10, 9}
            	};
            
            int active_island_type = new JSONObject(MainExtension.sqlHandler.sendCommand(
    		        "SELECT island_id FROM player_islands WHERE user_island_id = '" + player.active_island + "';")
    		    ).getJSONArray("result").getJSONArray(0).getInt(0);
            int monsterId = monsters[active_island_type][new Random().nextInt(monsters[active_island_type].length)];
            trace(monsterId);
            ticket.putInt("amount", monsterId);
            ticket.putUtfString("prize", "monster");
            ticket.putInt("id", 17);
            ticket.putUtfString("type", "M");
            ticket.putInt("is_top_prize", 1);

            response.putBool("success", true);
            response.putSFSObject("ticket", ticket);
			send(cmd, response, user);
			break;
		case "gs_collect_flip_level":
			response.putSFSArray("properties",  player.getProperties());
			response.putBool("success", true);
			player.flipgame_level++;
			response.putInt("level", player.flipgame_level);
			response.putInt("level_id", player.flipgame_level);
			response.putSFSObject("ingame_reward", new SFSObject());
			send(cmd, response, user);
			break;
		case "gs_collect_flip_mini_game":
			response.putSFSArray("properties",  player.getProperties());
			response.putBool("success", true);
			response.putLong("flipGameTime", serverTime + 60);
			player.flipgame_level = 1;
			send(cmd, response, user);
			break;
		case "gs_get_ranked_island_data":
		    int rankIndex = params.getInt("weekly_rank") - 1;
		    boolean composer = params.containsKey("composer") && params.getBool("composer");

		    String sql = "SELECT user_island_id, user_game_id, island_id FROM player_islands WHERE likes > dislikes ORDER BY (likes - dislikes) DESC, likes DESC LIMIT 1 OFFSET " + rankIndex;
		    JSONObject result = new JSONObject(MainExtension.sqlHandler.sendCommand(sql));
		    JSONArray data = result.getJSONArray("result");

		    if (data.length() == 0) {
		        response.putBool("success", false);
		        response.putUtfString("msg", "No ranked island found at that index.");
		        trace("No ranked island found.");
		        send(cmd, response, user);
		        break;
		    }

		    JSONArray row = data.getJSONArray(0);
		    long rankedIslandId = row.getLong(0);
		    String targetUserGameId = row.getString(1);
		    int islandId = row.getInt(2);
		    
		    int islandTypeId = MainExtension.client.getIslandTypeById(islandId);

		    Player friendPlayer = new Player(1, targetUserGameId, targetUserGameId);
		    
		    PlayerIsland island = PlayerIsland.createNewIsland(1, islandId, islandTypeId, rankedIslandId);
		    
    	    PlayerIsland.addPlayerMonsters(island, rankedIslandId, targetUserGameId);
    	    PlayerIsland.addPlayerStructures(island, rankedIslandId, targetUserGameId);
    	    
    	    friendPlayer.addIsland(island);
		    
		    friendPlayer.active_island = rankedIslandId;
		    
		    trace(friendPlayer.toSFSObject().toJson());

		    response.putLong("ranked_island_id", rankedIslandId);
		    response.putLong("user_island_id", rankedIslandId);
		    response.putSFSObject("friend_object", friendPlayer.toSFSObject());
		    response.putInt("weekly_rank", rankIndex + 1);
		    response.putLong("num_ranked_islands", 10);
		    response.putBool("island_rated", false);
		    response.putBool("success", true);

		    send(cmd, response, user);
		    break;
		case "gs_get_random_visit_data":
			data = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT user_island_id, user_game_id, island_id FROM player_islands ORDER BY RAND() LIMIT 1")).getJSONArray("result");

			if (data.length() == 0) {
			    response.putBool("success", false);
			    send(cmd, response, user);
			    break;
			}

			row = data.getJSONArray(0);
			rankedIslandId = row.getLong(0);
			targetUserGameId = row.getString(1);
			islandId = row.getInt(2);

			islandTypeId = MainExtension.client.getIslandTypeById(islandId);
		
			friendPlayer = new Player(1, targetUserGameId, targetUserGameId);

			island = PlayerIsland.createNewIsland(1, islandId, islandTypeId, rankedIslandId);

			PlayerIsland.addPlayerMonsters(island, rankedIslandId, targetUserGameId);
			PlayerIsland.addPlayerStructures(island, rankedIslandId, targetUserGameId);

			friendPlayer.addIsland(island);
			friendPlayer.active_island = rankedIslandId;

			response.putLong("user_island", rankedIslandId);
			response.putSFSObject("friend_object", friendPlayer.toSFSObject());
			response.putBool("island_rated", false);
			response.putBool("success", true);
			
			send(cmd, response, user);
			break;
		case "gs_get_friend_visit_data":
		    long targetBbbId = params.getLong("user_id");

		    JSONArray userResult = new JSONObject(MainExtension.sqlHandler.sendCommand(
		        "SELECT user_game_id FROM users WHERE bbb_id = '" + targetBbbId + "';"
		    )).getJSONArray("result").getJSONArray(0);
		    
		    targetUserGameId = userResult.getString(0);
		    
		    resultArray = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT * FROM player_islands WHERE user_game_id = '" + targetUserGameId + "';")).getJSONArray("result");
		    
		    friendPlayer = new Player((int) targetBbbId, targetUserGameId, targetUserGameId);
		    
        	for (int i = 0; i < resultArray.length(); i++) {
        	    row = resultArray.getJSONArray(i);
        	    islandId = row.getInt(3);
        	    long user_island_id = row.getInt(0);
        	    
        	    island = PlayerIsland.createNewIsland((int) targetBbbId, islandId, MainExtension.client.getIslandTypeById(islandId), user_island_id);
        	    
        	    PlayerIsland.addPlayerMonsters(island, user_island_id, targetUserGameId);
        	    PlayerIsland.addPlayerStructures(island, user_island_id, targetUserGameId);
        	    
        	    friendPlayer.addIsland(island);
        	}
		    
		    JSONArray activeIslandRow = new JSONObject(MainExtension.sqlHandler.sendCommand(
		        "SELECT active_island FROM players WHERE id = '" + targetUserGameId + "';"
		    )).getJSONArray("result").getJSONArray(0);

		    rankedIslandId = activeIslandRow.getLong(0);
		    
		    friendPlayer.active_island = rankedIslandId;

		    response.putSFSObject("friend_object", friendPlayer.toSFSObject());
		    response.putBool("success", true);
		    send(cmd, response, user);
		    break;
		case "gs_change_island":
            response.putBool("success", true);
            response.putLong("user_island_id", params.getLong("user_island_id"));
            if (params.containsKey("user_structure_focus")) {
                response.putLong("user_structure_focus", params.getLong("user_structure_focus"));
            }

            if (params.containsKey("user_monster_focus")) {
            	response.putLong("user_monster_focus", params.getLong("user_monster_focus"));
            }
            
            player.active_island = params.getLong("user_island_id");
             
            MainExtension.sqlHandler.sendCommand("UPDATE players SET active_island = '" + params.getLong("user_island_id") + "' WHERE id = '" + userGameId + "';");
            send(cmd, response, user);
			break;
		case "gs_buy_island":
			if (Util.isIslandBroken(params.getInt("island_id")) && player.limited == true) {
		        response.putUtfString("msg", "This island isn't supported.");
		        response.putBool("success", false);
		        send("gs_display_generic_message", response, user);
				break;
			}
			response.putBool("success", true);
			response.putBool("no_change_island", false);
			int island_type = MainExtension.client.getIslandTypeById(params.getInt("island_id"));
			long user_island_id =  PlayerIslandFactory.getNextUserIslandId();
			
			PlayerIsland newIsland = PlayerIsland.createNewIsland(player.bbb_id, params.getInt("island_id"), island_type, user_island_id);
			PlayerIslandFactory.createInitialStructures(newIsland, userGameId);
			
			response.putSFSObject("user_island", newIsland.toSFSObject());
			response.putSFSArray("properties",  player.getProperties());
	        response.putSFSArray("tracks", new SFSArray());
	        response.putSFSArray("songs", new SFSArray());
	        
			MainExtension.sqlHandler.sendCommand("INSERT INTO player_islands (user_game_id, island_id) VALUES ('" + userGameId + "', '" + params.getInt("island_id") + "');");
			send(cmd, response, user);
			break;
		case "gs_mute_monster":
	        int muted = new JSONObject(MainExtension.sqlHandler.sendCommand(
    		        "SELECT muted FROM player_monsters WHERE user_monster_id = '" + params.getLong("user_monster_id") + "' AND user_game_id = '"+ userGameId + "';")
    		    ).getJSONArray("result").getJSONArray(0).getInt(0);
	        
	        muted = (muted == 1) ? 0 : 1;
	        
	        MainExtension.sqlHandler.sendCommand("UPDATE player_monsters SET muted = '" + muted + "' WHERE user_monster_id = '" + params.getLong("user_monster_id") + "' AND user_game_id = '"+ userGameId +"';");
	        
	        response.putBool("success", true);
	        response.putLong("user_monster_id", params.getLong("user_monster_id"));
	        response.putInt("muted", muted);
	        SFSObject responseUpdate = new SFSObject();
	        responseUpdate.putLong("user_monster_id", params.getLong("user_monster_id"));
	        responseUpdate.putInt("muted", muted);
	        send(cmd, response, user);
	        send("gs_update_monster", responseUpdate, user);
            send(cmd, response, user);
			break;
		case "gs_get_code":
		    String[] args = params.getUtfString("code").toLowerCase().substring(2).split(" ");
		    
		    switch (args[0]) {
		    case "users":
			    List<User> users = (List<User>) zone.getUserList();
			    String userNames = users.stream()
			            .map(User::getName)
			            .collect(Collectors.joining(", "));

		        response.putUtfString("msg", userNames);
		        
		    	break;
		    case "online":
		    	response.putUtfString("msg", "(" + user_count + "/" + Settings.max_user_count + ")");
		    	break;
		    case "message":
		        for (User u : zone.getUserList()) {
		            ISFSObject msg = new SFSObject();
		            msg.putUtfString("msg", String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
		            send("gs_display_generic_message", msg, u);
		        }
			    response.putUtfString("msg", "Sent!");
			    break;
		    case "info":
		    	long uptime = Util.getUnixTime() - MainExtension.startTime;
		    	long gameTime = Util.getUnixTime() - (long) user.getProperty("join_time");

		    	long h = uptime / 3600, m = (uptime % 3600) / 60, s = uptime % 60;
		    	long h2 = gameTime / 3600, m2 = (gameTime % 3600) / 60, s2 = gameTime % 60;
		    	response.putUtfString("msg", 
		    			"Version: 1.2.4" +
		    			"\nOnline: (" + user_count + "/" + Settings.max_user_count + ")\n" +
		    			 String.format("Up time: %d hours, %d minutes, %d seconds", h, m, s) +
		    			 "\n" +
		    			 String.format("Your time in game: %d hours, %d minutes, %d seconds", h2, m2, s2) +
		    			 "\nAccounts: " + new JSONObject(MainExtension.sqlHandler.sendCommand(
		    					    "SELECT COUNT(*) FROM players;"
		    					 )).getJSONArray("result").getJSONArray(0).getInt(0) +
		    			 "\n Sessions since server start: " + MainExtension.sessionsSinceStart
		    	);
		    	break;
		    case "account":
		    	response.putUtfString("msg",
		    			"Account ID: " + userGameId +
		    			"\nBBB ID: " + player.bbb_id + 
		    			"\nDate Created: " + Util.formatUnixTime(player.date_created) +
		    			"\nVIP status: " + "None"
		    			);
		    	break;
		    case "help":
		        response.putUtfString("msg", 
		            "info - Get info about the server" +
		            "\naccount - View details about your account" +
		            "\nrefill - Instantly refill your currency balances" + 
		            "\ndirectplace - Toggle direct monster placement" +
		            "\nhelp2 - View the next page of commands"
		        );
		        break;

		    case "help2":
		        response.putUtfString("msg", 
		            "bypasslimits - Bypass restrictions to access unfinished or broken features" +
		            "\nlogin - Get login credentials for your account" +
		            "\nclone - clone an account from original MSM"
		        );
		        break;
		    case "login":
		    	JSONArray accountInfo = new JSONObject(MainExtension.sqlHandler.sendCommand(
					    "SELECT username, password FROM users WHERE user_game_id = '" + player.user_game_id + "';"
					 )).getJSONArray("result").getJSONArray(0);
		    	String username = accountInfo.getString(0);
		    	String password = accountInfo.getString(1);
		    	
		        response.putUtfString("msg", "Your login info:\nUsername: " + username + "\nPassword: " + password);     
		    	break;
		    case "clone":
		    	response.putUtfString("msg", "This will take a while depending on how large the account is...");     
		    	send("gs_display_generic_message", response, user);
		    	
		    	long bbbId;
				try {
					bbbId = (long) NumberFormat.getInstance().parse(args[1]);
				} catch (ParseException e) {
					trace(e.toString());
					break;
				}
				
		    	MSMClient client = new MSMClient("yfjdv5psbwxz", "3774dj5c96tpb8h3tgjt", "anon", "4.8.2", MainExtension.allowedVersions.getUtfString("4.8.2"), true);
		    	
		        try {
		            SFSObject auth = client.auth();
		            trace("Auth response: " + auth.toJson());

		            if (auth.getBool("ok")) {
		                SFSObject pregameSetup = client.pregameSetup();
		                trace("Pregame Setup response: " + pregameSetup.toJson());

		                if (pregameSetup.getBool("ok")) {
		                    client.connectToServer();
		                } else {
		                    trace("Pregame Setup failed: " + pregameSetup.getUtfString("message"));
		                }
		            } else {
		                trace("Auth failed: " + auth.getUtfString("message"));
		            }
		        } catch (Exception e) {
		            trace("Error in clone: " + e.toString());
		        }
		        
		        Util.sleep(2000);
		    	
		        SFSObject a = new SFSObject();
		        a.putLong("friend_id", bbbId);
		        
		        long friendId = 0;
		    	
		    	client.sendExtensionRequest("gs_add_friend", a);
		    	
		    	client.sendExtensionRequest("gs_get_friends", new SFSObject());
		    	
		    	Util.sleep(1000);
		    	
		        SFSArray friends = (SFSArray) MainExtension.client.friends.getSFSArray("friends");
		        
		        trace(friends.toJson());
		        
		        for (int i=0; i < friends.size(); i++) {
		            SFSObject friendx = (SFSObject) friends.getSFSObject(i);

		            if (friendx.getLong("bbb_id")==bbbId) {
		            	friendId = friendx.getInt("user_id");
		                trace("DONE");
		                break;
		            }
		        }
		        
		        a = new SFSObject();
		        a.putLong("user_id", friendId);
		        
		        client.sendExtensionRequest("gs_get_friend_visit_data", a);
		        
		        Util.sleep(4000);
		        
		        trace(client.friend.toJson());
		        
		        response.putUtfString("msg", "Success! Relogin for it to take affect.");     
		        break;
		    case "refill":
		    	player.coins = 100000000;
		    	player.diamonds = 100000000;
		    	player.keys = 100000000;
		    	player.shards = 100000000;
		    	player.food = 100000000;
		    	player.starpower = 100000000;
		    	player.relics = 100000000;
		    	
		    	response.putUtfString("msg", "Success! Relogin for it to take affect.");
		    	break;
		    case "directplace":
		    	MainExtension.sqlHandler.sendCommand("UPDATE players SET direct_place = '" + (player.direct_place == 0 ? 1 : 0) + "' WHERE id = '" + userGameId + "';");
		    	player.direct_place = (player.direct_place == 0 ? 1 : 0);
		    	response.putUtfString("msg", (player.direct_place == 1 ? "Direct place toggled on!" : "Direct place toggled off!") + " Relogin for it to take affect.");
		    	break;
		    case "bypasslimits":
		    	player.limited = false;
		    	response.putUtfString("msg", "Success!");
		    	break;
		    // ADMIN COMMANDS
		    case "bbs_4":
		        response.putUtfString("msg", "Table cleared!");
		        MainExtension.sqlHandler.sendCommand("DELETE FROM " + args[1] + ";");
		        break;
		    case "bbs_5":
		    	for (User u : zone.getUserList()) {
	                SFSObject response3 = new SFSObject();
	                response3.putBool("force_logout", true);
	                response3.putUtfString("msg", String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
	                send("gs_display_generic_message", response3, u);
	            }
		    	
		    	break;
		    default:
		        response.putUtfString("msg", "Unknown command. Type 'help' for a list of commands.");
		        break;
		    }
		    
	        send("gs_display_generic_message", response, user);
		    break;
		case "gs_rate_island":
			String column = params.getBool("liked") ? "likes" : "dislikes";
			MainExtension.sqlHandler.sendCommand(
			    "UPDATE player_islands SET " + column + " = " + column + " + 1 WHERE user_island_id = '" + params.getLong("friend_island_id") + "';"
			);
			response.putBool("success", true);
			send(cmd, response, user);
			break;
		case "gs_save_island_warp_speed":
			response.putBool("success", true);
			send(cmd, response, user);
			break;
		case "gs_get_island_rank":
			response.putBool("success", true);
			response.putInt("rank", 0);
			response.putLong("island_id", player.active_island);
			send(cmd, response, user);
			break;		
		case "gs_add_friend":
			long friendBbbId = params.getLong("friend_id");
			if (player.bbb_id == friendBbbId) {
				   response.putBool("success", false);
				   response.putUtfString("error_msg", "FRIEND_ERROR_USER_NOT_FOUND");
				   send(cmd, response, user);
				   break;
			}
			
			JSONArray friend = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT * FROM users WHERE bbb_id='"+friendBbbId+"';")).getJSONArray("result");
			
			if (friend.length() == 0) {
				   response.putBool("success", false);
				   response.putUtfString("error_msg", "FRIEND_ERROR_USER_NOT_FOUND");
				   send(cmd, response, user);
				   break;
			}
			
			friend = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT * FROM user_friends WHERE (user_1='" + player.bbb_id + "' AND user_2='" + friendBbbId + "') OR (user_2='" + player.bbb_id + "' AND user_1='" + friendBbbId + "');")).getJSONArray("result");
			
			if (friend.length() == 0) {
				MainExtension.sqlHandler.sendCommand(
						"INSERT INTO user_friends (user_1, user_2) VALUES ('"
						+ player.bbb_id + "', '"
						+ friendBbbId + "');"
				);
			} else {
				response.putBool("success", false);
				response.putUtfString("error_msg", "FRIEND_ERROR_ALREADY_EXISTS");
				send(cmd, response, user);
				break;
			}
			
			response.putBool("success", true);
			response.putLong("friend_id", friendBbbId);
			send(cmd, response, user);
			break;
		case "gs_remove_friend":
			friendBbbId = params.getLong("friend_id");
			if (player.bbb_id == friendBbbId) {
				   response.putBool("success", false);
				   response.putUtfString("error_msg", "FRIEND_ERROR_USER_NOT_FOUND");
				   send(cmd, response, user);
				   break;
			}
			
			friend = new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT * FROM user_friends WHERE (user_1='" + player.bbb_id + "' AND user_2='" + friendBbbId + "') OR (user_2='" + player.bbb_id + "' AND user_1='" + friendBbbId + "');")).getJSONArray("result");
			
			if (friend.length() > 0) {
				MainExtension.sqlHandler.sendCommand("DELETE FROM user_friends WHERE (user_1='" + player.bbb_id + "' AND user_2='" + friendBbbId + "') OR (user_2='" + player.bbb_id + "' AND user_1='" + friendBbbId + "');");
				response.putBool("success", true);
			} else {
				response.putBool("success", false);
			}
			
			send(cmd, response, user);
			break;
		case "gs_get_friends":
			send(cmd, getFriends(user), user);
			break;
		// misc
		case "keep_alive":
			send("keep_alive", new SFSObject(), user);
			break;
		// player
		case "gs_quest":
			response.putSFSArray("result", new SFSArray());
			//response.putInt("event_id", 0);
			
			send(cmd, response, user);
			break;
		case "gs_player":
	        if (user.containsProperty("client_version")) {
	        	//player.last_client_version = (String) user.getProperty("client_version");
	        }
	        
	        send("client_keep_alive", new SFSObject(), user);
			
			SFSObject returnObj = new SFSObject();
		    
		    returnObj.putSFSObject("player_object", player.toSFSObject()); // MainExtension.client.getChunkByCmdAndNum(cmd, 1).getSFSObject("player_object")
		    
			send(cmd, returnObj, user);
			
			SFSObject msgResponse = new SFSObject(); 
			msgResponse.putUtfString("msg", "Please support us on Boosty.\n boosty.to/msm-extensions");
			
	        send("gs_display_generic_message", msgResponse, user);
	        
	        send("gs_get_friends", getFriends(user), user);
			break;
		case "gs_set_displayname":
			response.putBool("success", true);
			response.putUtfString("displayName", params.getUtfString("newName"));
			MainExtension.sqlHandler.sendCommand("UPDATE players SET nickname = '" + params.getUtfString("newName") + "' WHERE id = '" + userGameId + "';");
			send(cmd, response, user);
			break;
		case "gs_set_avatar":
			String ppInfo = params.getUtfString("pp_info");
			
			response.putBool("success", true);
			response.putUtfString("pp_info", ppInfo);
			response.putInt("pp_type", 0);
			
			send(cmd, response, user);
			break;
		case "gs_set_moniker":
			int moniker = params.getInt("moniker_id");
			
			response.putBool("success", true);
			response.putInt("id", moniker);
			
			send(cmd, response, user);
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
	        response.putUtfString("msg", cmd+" is not added!");

	        send("gs_display_generic_message", response, user);
			break;
		}
	}
}
