package server.Entities;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.v2.entities.data.SFSObject;

import server.MainExtension;
import server.Tools.Util;

public class PlayerStructure {
	public long user_island_id;
	public long date_created;
	public double scale = 1;
	public long last_collection = Util.getUnixTime();
	public int structure_id;
	public int pos_y;
	public int is_upgrading = 0;
	public int user_structure_id;
	public int pos_x;
	public int in_warehouse = 0;
	public int is_complete = 1;
	public long building_completed;
	public int muted = 0;
	public int flip = 0;
	
	public static PlayerStructure createNewStructure(long date_created, long building_complete, long user_island_id, int user_structure_id, int structure_id, int pos_x, int pos_y) {
		PlayerStructure structure = new PlayerStructure();
		structure.user_island_id = user_island_id;
		structure.date_created = date_created;
		structure.structure_id = structure_id;
		structure.pos_y = pos_y;
		structure.user_structure_id = user_structure_id;
		structure.pos_x = pos_x;
		structure.building_completed = building_complete;
		
		return structure;
	}
	
	public SFSObject toSFSObject() {
		SFSObject object = new SFSObject();
		
		object.putInt("island", (int) this.user_island_id);
        object.putLong("date_created", this.date_created);
        object.putInt("scale", (int) this.scale);
        object.putLong("last_collection", this.last_collection);
        object.putInt("structure", this.structure_id);
        object.putInt("pos_y", this.pos_y);
        object.putInt("is_upgrading", this.is_upgrading);
        object.putInt("user_structure_id", this.user_structure_id);
        object.putInt("pos_x", this.pos_x);
        object.putInt("in_warehouse", this.in_warehouse);
        object.putInt("is_complete", this.is_complete);
        object.putLong("building_completed", this.building_completed);
        object.putInt("muted", this.muted);
        object.putInt("flip", this.flip);
		
		return object;
	}
	
    public static PlayerStructure createFromDatabase(long userStructureId, String userGameId) {
        String query = String.format(
            "SELECT structure_id, user_island_id, pos_x, pos_y, scale, flip, date_created " +
            "FROM player_structures WHERE user_structure_id = '%d' AND user_game_id = '%s' LIMIT 1;",
            userStructureId, userGameId
        );

        JSONObject result = new JSONObject(MainExtension.sqlHandler.sendCommand(query));
        JSONArray resArray = result.getJSONArray("result");

        if (resArray.length() == 0) {
            return null;
        }

        JSONArray row = resArray.getJSONArray(0);

        int structureId = row.getInt(0);
        long userIslandId = row.getLong(1);
        int posX = row.getInt(2);
        int posY = row.getInt(3);
        double scale = row.getDouble(4);
        int flip = row.getInt(5);
        long dateCreated = row.getLong(6);

        PlayerStructure structure = PlayerStructure.createNewStructure(
            dateCreated, dateCreated, userIslandId, (int) userStructureId, structureId, posX, posY
        );

        structure.scale = scale;
        structure.flip = flip;

        return structure;
    }
}
