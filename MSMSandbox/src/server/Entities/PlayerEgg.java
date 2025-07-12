package server.Entities;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import server.Tools.Util;

public class PlayerEgg {
	public long island_id;
	public long structure_id;
	public int monster_id;
	public long laid_on;
	public long hatch_on;
	public long user_egg_id;
	
	public static PlayerEgg createNewEgg(long island_id, long structure_id, int monster_id, long hatch_on, long user_egg_id) {
		PlayerEgg egg = new PlayerEgg();
		
		egg.island_id = island_id;
		egg.structure_id = structure_id;
		egg.monster_id = monster_id;
		egg.laid_on = Util.getUnixTime();
		egg.hatch_on = hatch_on;
		egg.user_egg_id = user_egg_id;
		
		return egg;
	}
	
	public SFSObject toSFSObject() {
		SFSObject object = new SFSObject();
		
		object.putLong("island", this.island_id);
		object.putLong("structure", this.structure_id);
		object.putLong("user_egg_id", this.user_egg_id);
		object.putInt("monster", this.monster_id);
		object.putLong("laid_on", this.laid_on);
		object.putLong("hatches_on", this.hatch_on);
		object.putInt("book_value", 250);
		
		SFSObject costume = new SFSObject();
        costume.putSFSArray("p", new SFSArray());
        costume.putInt("eq", 0);
        
        object.putSFSObject("costume", costume);
		
		return object;
	}
}
