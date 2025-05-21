package server.Entities;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import server.Settings;
import server.Tools.Util;

public class Player {
	public String client_platform = "pc";
	public int relics = 0;
	public boolean is_admin = false;
	public int bbb_id;
	public int keys = 0;
	public int diamonds_spent = 0;
	public String last_client_version = "0.0.0";
	public int diamonds = 0;
	public int level = 1;
	public int last_login = 0;
	public String display_name = "New Player";
	public PlayerIsland[] islands;
	public int food = 0;
	public int coins = 0;
	public int shards = 0;
	public int starpower = 0;
	public int date_created = 0;
	public long active_island = 1;
	public int xp = 0;
	
	public Player(int bbb_id, String display_name) {
		this.bbb_id = bbb_id;
		this.display_name = display_name;
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
	    playerObject.putInt("egg_wildcards", 999999999);
	    playerObject.putInt("keys", this.keys);
	    playerObject.putInt("relics", this.relics);
	    playerObject.putInt("starpower", this.starpower);
	    playerObject.putInt("ethereal_currency", this.shards);
	    playerObject.putInt("total_starpower_collected", 0);
	    playerObject.putBool("has_promo", false);
	    
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
	    
	    playerObject.putInt("date_created", this.date_created);
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
}