package server.Entities;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import server.Tools.Util;

public class Entity {
	public Long date_created;
	public Long last_changed;
	public Long build_time;
	public SFSObject data;
	public int cost_coins;
	public int cost_diamonds;
	public int cost_eth_currency;
	public int cost_keys;
	public int cost_medals;
	public int cost_relics;
	public int cost_sale;
	public int cost_starpower;
	public SFSObject graphic;
	public String description;
	public int entity_id;
	public String entity_type;
	public String common_name;
	public String min_server_version;
	public int movable;
	public String name;
	public int premium;
	public SFSArray requirements;
	public int size_x;
	public int size_y;
	public int view_in_market;
	public int view_in_starmarket;
	public int y_offset;
	
	public Entity(SFSObject data) {
		this.date_created = Util.getUnixTime();
		this.last_changed = this.date_created;
		this.data = data;
	}
}
