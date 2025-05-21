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

		this.build_time = data.getLong("build_time");
		this.cost_coins = data.getInt("cost_coins");
		this.cost_diamonds = data.getInt("cost_diamonds");
		this.cost_eth_currency = data.getInt("cost_eth_currency");
		this.cost_keys = data.getInt("cost_keys");
		this.cost_medals = data.getInt("cost_medals");
		this.cost_relics = data.getInt("cost_relics");
		this.cost_sale = data.getInt("cost_sale");
		this.cost_starpower = data.getInt("cost_starpower");
		this.graphic = (SFSObject) data.getSFSObject("graphic");
		this.description = data.getUtfString("description");
		this.entity_id = data.getInt("entity_id");
		this.entity_type = data.getUtfString("entity_type");
		this.common_name = data.getUtfString("common_name");
		this.min_server_version = data.getUtfString("min_server_version");
		this.movable = data.getInt("movable");
		this.name = data.getUtfString("name");
		this.premium = data.getInt("premium");
		this.requirements = (SFSArray) data.getSFSArray("requirements");
		this.size_x = data.getInt("size_x");
		this.size_y = data.getInt("size_y");
		this.view_in_market = data.getInt("view_in_market");
		this.view_in_starmarket = data.getInt("view_in_starmarket");
		this.y_offset = data.getInt("y_offset");
	}

}
