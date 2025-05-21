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
		
		this.beds = data.getInt("beds");
	    this.monster_class = data.getUtfString("class");
	    this.genes = data.getUtfString("genes");
	    this.happiness = (SFSObject) data.getSFSObject("happiness");
	    this.level = data.getInt("level");
	    this.levels = (SFSArray) data.getSFSArray("levels");
	    this.levelup_island = data.getUtfString("levelup_island");
	    this.monster_id = data.getInt("monster_id");
	    this.names = (SFSArray) data.getSFSArray("names");
	    this.portrait_graphic = data.getUtfString("portrait_graphic");
	    this.select_sound = data.getUtfString("select_sound");
	    this.spore_graphic = data.getUtfString("spore_graphic");
	    this.time_availability = data.getInt("time_availability");
	    this.time_to_fill_sec = data.getInt("time_to_fill_sec");
	    this.xp = data.getInt("xp");
	}

}
