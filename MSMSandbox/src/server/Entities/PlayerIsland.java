package server.Entities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

import server.MainExtension;
import server.Tools.Util;

public class PlayerIsland {
	static final Point castlePosition = new Point(29, 9);
	static final Point nurseryPosition = new Point(35, 17);
	
    public SFSArray eggs = new SFSArray();
    public int island_id;
    public int likes = 0;
    public int dislikes = 0;
    public long date_created;
    public double warp_speed = 1.0;
    public SFSArray structures = new SFSArray();
    public SFSArray monsters = new SFSArray();
    public SFSArray last_baked = new SFSArray();
    public SFSArray baking = new SFSArray();
    public SFSArray breeding = new SFSArray();
    public SFSArray torches = new SFSArray();
    public SFSObject tiles = new SFSObject();
    public int num_torches = 0;
    public int island_type;
    public long user_island_id;
    public int last_player_level;
    public boolean light_torch_flag;
    public int user_id;
    public List<PlayerStructure> playerStructures = new ArrayList<>();
    public List<PlayerMonster> playerMonsters = new ArrayList<>();
    
    public static PlayerIsland createNewIsland(int bbb_id, int island_id, int island_type, long user_island_id) {
    	PlayerIsland island = new PlayerIsland();
    	island.island_id = island_id;
    	island.island_type = island_type;
    	island.user_id = bbb_id;
    	island.user_island_id = user_island_id;
    	
    	return island;
    }
    
	public void addMonster(int monster_id, int pos_x, int pos_y, long user_monster_id, int muted, int flip, String name, int big) {    	
		PlayerMonster monster = PlayerMonster.createNewMonster(this.user_island_id, monster_id, pos_x, pos_y, flip, user_monster_id, name);
		monster.muted = muted;
		monster.big = big;
		playerMonsters.add(monster);
	}
	
	public void addStructure(long date_created, long building_complete, int user_structure_id, int structure_id, int pos_x, int pos_y, int flip, double scale) {
		PlayerStructure structure = PlayerStructure.createNewStructure(date_created, building_complete, this.user_island_id, user_structure_id, structure_id, pos_x, pos_y);
		structure.flip = flip;
		structure.scale = scale;
		playerStructures.add(structure);
	}
	
    public SFSObject toSFSObject() {
    	long unix = Util.getUnixTime();
	    SFSObject island = new SFSObject();
	    island.putSFSArray("eggs", new SFSArray());
	    island.putInt("island", this.island_id);
	    island.putLong("date_created", unix);
	    island.putDouble("warp_speed", 1.0d);
	    
	    SFSArray structures = new SFSArray();
	    
        for (PlayerStructure structure : playerStructures) {
        	structures.addSFSObject(structure.toSFSObject());
        }
        
	    island.putSFSArray("structures", structures);
	    island.putInt("dislikes", this.dislikes);
	    
        SFSArray monsters = new SFSArray();
        
        for (PlayerMonster monster : playerMonsters) {
        	monsters.addSFSObject(monster.toSFSObject());
        }
	    
	    island.putSFSArray("monsters", monsters);
	    island.putInt("num_torches", 0);
	    island.putInt("type", this.island_type);
	    island.putSFSArray("last_baked", new SFSArray());
	    island.putSFSObject("tiles", new SFSObject());
	    island.putSFSArray("baking", new SFSArray());
	    island.putUtfString("costumes_owned", "[]");
	    island.putLong("user_island_id", this.user_island_id);
	    island.putInt("last_player_level", 99);
	    island.putBool("light_torch_flag", false);
	    
        SFSObject costumes_data = new SFSObject();
        costumes_data.putSFSArray("costumes", new SFSArray());
        
        island.putSFSObject("costume_data", costumes_data);
        
        island.putSFSArray("torches", new SFSArray());
        island.putSFSArray("fuzer", new SFSArray());
        island.putLong("user", this.user_id);
        island.putInt("likes", this.likes);
        island.putSFSArray("breeding", new SFSArray());
    	
    	return island;
    }
    
    public boolean isGoldIsland() {
        return this.island_id == 6;
    }

    public boolean isShugaIsland() {
        return this.island_id == 8;
    }

    public boolean isTribalIsland() {
        return this.island_id == 9;
    }

    public boolean isWublinIsland() {
        return this.island_id == 10;
    }

    public boolean isCelestialIsland() {
        return this.island_id == 12;
    }

    public boolean isAmberIsland() {
        return this.island_id == 22;
    }

    public boolean isComposerIsland() {
        return this.island_id == 11;
    }

    public boolean isBattleIsland() {
        return this.island_id == 20;
    }

    public boolean isSeasonalIsland() {
        return this.island_id == 21;
    }

    public boolean isMythIsland() {
        return this.island_id == 23;
    }

    public boolean isWorkshopIsland() {
        return this.island_id == 24;
    }

    public boolean isNexusIsland() {
        return this.island_id == 25;
    }

    public boolean isBoxIsland() {
        return isAmberIsland() || isWublinIsland() || isCelestialIsland();
    }

    public boolean isEtherealIsland() {
        return this.island_id == 7 || this.island_id == 19 || this.island_id == 24;
    }
    
    public static void addPlayerMonsters(PlayerIsland island, long user_island_id, String userGameId) {
    	SFSExtension ext = (SFSExtension) SmartFoxServer.getInstance().getZoneManager().getZoneByName("MySingingMonsters").getExtension();
    	
        JSONArray monstersArray = new JSONObject(
            MainExtension.sqlHandler.sendCommand(
                "SELECT * FROM player_monsters WHERE user_island_id = " + user_island_id + " AND user_game_id = '" + userGameId + "'"
            )
        ).getJSONArray("result");

        for (int j = 0; j < monstersArray.length(); j++) {
            JSONArray monsterRow = monstersArray.getJSONArray(j);

            int monsterId = monsterRow.getInt(1);
            int userMonsterId = monsterRow.getInt(2);
            int posX = monsterRow.getInt(4);
            int posY = monsterRow.getInt(5);
            int muted = monsterRow.getInt(6);
            int flip = monsterRow.getInt(7);
            String name = monsterRow.getString(8);
            int big = monsterRow.getInt(9);

            island.addMonster(monsterId, posX, posY, userMonsterId, muted, flip, name, big);
        }
    }

    public static void addPlayerStructures(PlayerIsland island, long user_island_id, String userGameId) {
        JSONArray structuresArray = new JSONObject(
            MainExtension.sqlHandler.sendCommand(
                "SELECT * FROM player_structures WHERE user_island_id = " + user_island_id + " AND user_game_id = '" + userGameId + "'"
            )
        ).getJSONArray("result");

        for (int j = 0; j < structuresArray.length(); j++) {
            JSONArray structureRow = structuresArray.getJSONArray(j);

            int structureId = structureRow.getInt(1);
            int userStructureId = structureRow.getInt(2);
            int posX = structureRow.getInt(4);
            int posY = structureRow.getInt(5);
            double scale = structureRow.getInt(6);
            int flip = structureRow.getInt(7);
            int dateCreated = structureRow.isNull(9) ? 0 : structureRow.getInt(9);

            island.addStructure(dateCreated, dateCreated, userStructureId, structureId, posX, posY, flip, scale);
        }
    }
}
