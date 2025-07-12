package server.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import server.MainExtension;
import server.Settings;
import server.Tools.Util;

public class Player {
	public String client_platform = "pc";
	public int relics = 99999999;
	public boolean is_admin = false;
	public int bbb_id;
	public int keys = 99999999;
	public int diamonds_spent = 0;
	public String last_client_version = "0.0.0";
	public String user_game_id;
	public int diamonds = 99999999;
	public int level = 99;
	public int last_login = 0;
	public String display_name = "New Player";
	public List<PlayerIsland> playerIslands = new ArrayList<>();
	public int food = 99999999;
	public int coins = 999999999;
	public int shards = 99999999;
	public int egg_wildcards = 99999999;
	public int starpower = 99999999;
	public long date_created = 0;
	public long active_island = 1;
	public int xp = 99999999;
	
	public int flipgame_level = 1;
	
	public int direct_place = 0;
	public boolean limited = true;
	
	public int daily_cumulative_login_amt = 0;
	public int daily_cumulative_login_calender = 1;
	
	public Player(int bbb_id, String user_game_id, String display_name) {
		this.bbb_id = bbb_id;
		this.user_game_id = user_game_id;
		this.display_name = display_name;
		
		this.coins = 100000000;
		this.diamonds = 1000000;
	}
	
	public void addIsland(PlayerIsland island) {
		playerIslands.add(island);
	}
	
	public int removeCoins(int amount) {
		this.coins -= amount;
		
		return this.coins;
	}
	
    public int removeDiamonds(int amount) {
        this.diamonds -= amount;
        return this.diamonds;
    }

    public int removeFood(int amount) {
        this.food -= amount;
        return this.food;
    }

    public int removeKeys(int amount) {
        this.keys -= amount;
        return this.keys;
    }

    public int removeShards(int amount) {
        this.shards -= amount;
        return this.shards;
    }

    public int removeStarpower(int amount) {
        this.starpower -= amount;
        return this.starpower;
    }

    public int removeRelics(int amount) {
        this.relics -= amount;
        return this.relics;
    }
	
    public SFSArray getProperties() {
        SFSArray propertiesArray = new SFSArray();
        SFSObject prop;

        prop = new SFSObject();
        prop.putInt("coins_actual", this.coins);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("diamonds_actual", this.diamonds);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("food_actual", this.food);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("ethereal_currency_actual", this.shards);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("keys_actual", this.keys);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("relics_actual", this.relics);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("egg_wildcards_actual", this.egg_wildcards);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("starpower_actual", this.starpower);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("xp", this.xp);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("level", this.level);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putUtfString("daily_bonus_type", "relics");
        //propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("daily_bonus_amount", 1);
        //propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putBool("has_free_ad_scratch", true);
        //propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("daily_relic_purchase_count", 0);
        //propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("relic_diamond_cost", 0);
        //propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putLong("next_relic_reset", Util.getUnixTime() + 86400);
        //propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("premium", 1);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("earned_starpower", 0);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("speed_up_credit", 8);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("battle_xp", 1988000000);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("battle_level", 100);
        propertiesArray.addSFSObject(prop);

        prop = new SFSObject();
        prop.putInt("medals", 999999999);
        propertiesArray.addSFSObject(prop);

        return propertiesArray;
    }
	
	public SFSObject toSFSObject() {
		SFSObject playerObject = new SFSObject();
		
		playerObject.putLong("active_island", this.active_island);
		playerObject.putInt("level", this.level);
	    playerObject.putInt("xp", this.xp);
	    playerObject.putUtfString("display_name", this.display_name);
	    playerObject.putBool("premium", true);
	    
	    playerObject.putInt("battle_level", 1);
	    playerObject.putInt("prev_rank", 0);
	    playerObject.putInt("prev_tier", -1);
	    playerObject.putBool("is_admin", this.is_admin);
	    playerObject.putInt("friend_gift", 0);
	    playerObject.putUtfString("country", "UK");
	    playerObject.putUtfString("client_platform", this.client_platform);
	    
	    playerObject.putInt("currency_scratch_time", 0);
	    playerObject.putInt("cached_reward_day", 1);
	    playerObject.putInt("daily_bonus_amount", 0);
	    playerObject.putUtfString("daily_bonus_type", "none");
	    playerObject.putInt("reward_day", 1);
	    playerObject.putInt("rewards_total", 0);
	    playerObject.putInt("next_daily_login", 0);
	    playerObject.putInt("daily_cumulative_login_calendar_id", 1);
	    playerObject.putInt("daily_cumulative_login_next_collect", 0);
	    playerObject.putInt("daily_cumulative_login_reward_idx", 0);
	    playerObject.putInt("daily_cumulative_login_total", 0);
	    playerObject.putInt("daily_relic_purchase_count", 0); 
	    
	    playerObject.putInt("diamonds_spent", this.diamonds_spent);
	    playerObject.putInt("egg_wildcards", this.egg_wildcards);
	    playerObject.putInt("keys", this.keys);
	    playerObject.putInt("relics", this.relics);
	    playerObject.putInt("starpower", this.starpower);
	    playerObject.putInt("ethereal_currency", this.shards);
	    playerObject.putInt("total_starpower_collected", 0);
	    playerObject.putBool("has_promo", false);
	    
        SFSArray islands = new SFSArray();
        
        for (PlayerIsland island : playerIslands) {
        	islands.addSFSObject(island.toSFSObject());
        }
        
	    playerObject.putSFSArray("islands", islands);
	    
	    playerObject.putBool("has_free_ad_scratch", false);
	    playerObject.putBool("has_scratch_off_m", false);
	    playerObject.putBool("has_scratch_off_s", false);
	    playerObject.putInt("flip_game_time", -1);
	    playerObject.putInt("monster_scratch_time", 0);
	    playerObject.putInt("next_relic_reset", 0);
	    playerObject.putUtfString("extra_ad_params", "");
	    playerObject.putInt("email_invite_reward", 0);
	    playerObject.putInt("fb_invite_reward", 0);
	    playerObject.putInt("twitter_invite_reward", 0);
	    playerObject.putBool("third_party_ads", false);
	    playerObject.putBool("third_party_video_ads", false);
	    playerObject.putInt("last_fb_post_reward", 0);
	    
	    playerObject.putBool("new_mail", false);
	    playerObject.putInt("relic_diamond_cost", 1);
	    playerObject.putInt("speed_up_credit", 8);
	    playerObject.putInt("last_collect_all", 0);
	    playerObject.putInt("last_relic_purchase", 0);
	    playerObject.putBool("show_welcomeback", false);
	    playerObject.putInt("referral", 0);
	    playerObject.putInt("purchases_amount", 0);
	    playerObject.putInt("purchases_total", 0);
	    playerObject.putInt("last_login", this.last_login);
	    playerObject.putUtfString("last_client_version", this.last_client_version);
	    
	    playerObject.putLong("date_created", this.date_created);
	    playerObject.putInt("bbb_id", this.bbb_id);
	    playerObject.putInt("user", 0);
	    playerObject.putInt("user_id", this.bbb_id);
	    
	    playerObject.putInt("coins_actual", this.coins);
	    playerObject.putInt("diamonds_actual", this.diamonds);
	    playerObject.putInt("food_actual", this.food);
	    playerObject.putInt("keys_actual", this.keys);
	    playerObject.putInt("ethereal_currency_actual", this.shards);
	    playerObject.putInt("starpower_actual", this.starpower);
	    playerObject.putInt("relics_actual", this.relics);
	    playerObject.putInt("egg_wildcards_actual", 999999999);
	    
	    SFSObject battleInfo = new SFSObject();
	    battleInfo.putInt("level", 1);
	    battleInfo.putInt("xp", 0);
	    battleInfo.putInt("max_training_level", 10);
	    battleInfo.putInt("medals", 0);
	    battleInfo.putInt("user_id", 0);
	    battleInfo.putUtfString("loadout", "{\"slot2\": 0, \"slot1\": 0, \"slot0\": 0}");
	    battleInfo.putUtfString("loadout_versus", "{\"slot2\": 0, \"slot1\": 0, \"slot0\": 0}");
	    playerObject.putSFSObject("battle", battleInfo);

	    SFSObject costumes = new SFSObject();
	    costumes.putSFSArray("items", new SFSArray());
	    costumes.putSFSArray("unlocked", new SFSArray());
	    playerObject.putSFSObject("costumes", costumes);

	    SFSObject dailyCumulativeLogin = new SFSObject();
	    dailyCumulativeLogin.putInt("calendar_id", 1);
	    dailyCumulativeLogin.putInt("reward_idx", 0);
	    dailyCumulativeLogin.putInt("total", 0);
	    playerObject.putSFSObject("daily_cumulative_login", dailyCumulativeLogin);
	    
	    playerObject.putInt("coins", this.coins);
	    playerObject.putInt("diamonds", this.diamonds);
	    playerObject.putInt("food", this.food);
	    playerObject.putInt("keys", this.keys);
	    playerObject.putSFSArray("achievements", new SFSArray());
	    playerObject.putSFSArray("mailbox", new SFSArray());
	    
	    SFSArray playerGroups = new SFSArray();
	    playerGroups.addInt(40);
	    playerObject.putSFSArray("player_groups", playerGroups);
	    
	    SFSObject battleLoadout = new SFSObject();
	    battleLoadout.putInt("slot0", 0);
	    battleLoadout.putInt("slot1", 0);
	    battleLoadout.putInt("slot2", 0);
	    playerObject.putSFSObject("battle_loadout", battleLoadout);
	   
	    SFSObject avatar = new SFSObject();
	    avatar.putUtfString("pp_info", "0");
	    avatar.putInt("pp_type", 0);
	    playerObject.putSFSObject("avatar", avatar);
		
		return playerObject;
	}
	
	public PlayerIsland getCurrentIsland() {
	    JSONArray data = new JSONObject(MainExtension.sqlHandler.sendCommand(
		        "SELECT island_id FROM player_islands WHERE user_island_id = '" + this.active_island + "';"
		    )).getJSONArray("result").getJSONArray(0);
	    
		PlayerIsland island = PlayerIsland.createNewIsland(this.bbb_id, data.getInt(0), MainExtension.client.getIslandTypeById(data.getInt(0)), this.active_island);
	    PlayerIsland.addPlayerMonsters(island, data.getInt(0), this.user_game_id);
	    PlayerIsland.addPlayerStructures(island, data.getInt(0), this.user_game_id);
		
		return island;
	}
}