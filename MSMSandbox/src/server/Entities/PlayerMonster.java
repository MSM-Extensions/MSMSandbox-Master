package server.Entities;

import com.smartfoxserver.v2.entities.data.SFSObject;

import server.Tools.Util;

public class PlayerMonster {
	public int level = 21;
	public long island_id;
	public long last_feeding;
	public int in_hotel = 0;
	public long last_collection;
	public long monster_id;
	public int pos_x;
	public int pos_y;
	public double volume = 1.0;
	public int happiness = 100;
	public String name = "Made by @riotlove_official on YouTube";
	public int muted = 0;
	public int flip = 0;
	public long user_monster_id;
	public int big = 0;
	
	public static PlayerMonster createNewMonster(long island_id, long monster_id, int pos_x, int pos_y, int flip, long user_monster_id, String name) {
		long unix = Util.getUnixTime();
		PlayerMonster monster = new PlayerMonster();
		monster.island_id = island_id;
		monster.last_feeding = unix;
		monster.last_collection = unix;
		monster.monster_id = monster_id;
		monster.pos_x = pos_x;
		monster.pos_y = pos_y;
		monster.flip = flip;
		monster.user_monster_id = user_monster_id;
		monster.name = 	name;
		return monster;
	}
	
	public SFSObject toSFSObject() {
		SFSObject object = new SFSObject();
		
		object.putInt("level", 21);
        object.putLong("island", this.island_id);
        object.putLong("last_feeding", this.last_feeding);
        object.putInt("in_hotel", 0);
        object.putLong("last_collection", this.last_collection);
        object.putLong("monster", this.monster_id);
        object.putInt("pos_y", this.pos_y);
        object.putDouble("volume", 1.0d);
        object.putInt("pos_x", this.pos_x);
        object.putInt("times_fed", 0);
        object.putInt("happiness", 100);
        object.putUtfString("name", this.name);
        object.putInt("muted", this.muted);
        object.putInt("flip", flip);
        object.putLong("user_monster_id", this.user_monster_id);
        
        object.putSFSObject("megamonster", SFSObject.newFromJsonData("{\"permamega\":true,\"currently_mega\":"+ ((big == 1) ? true : false) + "}"));
        
		return object;
	}
}
