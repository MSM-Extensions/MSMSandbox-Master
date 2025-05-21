package server.Entities;

import com.smartfoxserver.v2.entities.data.SFSObject;

import server.Tools.Util;

public class SFSObjects {
	public static final SFSObject properties = new SFSObject();
	
	void init() {
		properties.putInt("coins_actual", 999999999);
		properties.putInt("diamonds_actual", 999999999);
		properties.putInt("food_actual", 999999999);
		properties.putInt("ethereal_currency_actual", 999999999);
		properties.putInt("keys_actual", 999999999);
		properties.putInt("relics_actual", 999999999);
		properties.putInt("egg_wildcards_actual", 999999999);
		properties.putInt("starpower_actual", 999999999);
		properties.putInt("xp", 1988000000);
		properties.putInt("level", 100);
		properties.putUtfString("daily_bonus_type", "relics");
		properties.putInt("daily_bonus_amount", 1);
		properties.putBool("has_free_ad_scratch", true);
		properties.putInt("daily_relic_purchase_count", 0);
		properties.putInt("relic_diamond_cost", 0);
		properties.putLong("next_relic_reset", Util.getUnixTime() + 86400);
		properties.putInt("premium", 1);
		properties.putInt("earned_starpower", 0);
		properties.putInt("speed_up_credit", 8);
		properties.putInt("battle_xp", 1988000000);
		properties.putInt("battle_level", 100);
		properties.putInt("medals", 999999999);
	}
}
