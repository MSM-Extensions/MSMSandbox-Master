package server.Entities;

import java.util.List;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class Monster extends Entity {
	public int beds;
	public String monster_class;
	public String genes;
	public SFSObject happiness;
	public int level;
	public SFSArray levels;
	public String levelup_island;
	public int monster_id;
	public SFSArray names;
	public String portrait_graphic;
	public String select_sound;
	public String spore_graphic;
	public int time_availability;
	public int time_to_fill_sec;
	public int xp;

	public Monster(SFSObject data) {
		super(data);
	}

}
