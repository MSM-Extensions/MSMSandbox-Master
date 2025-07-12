package server.Entities;

import java.awt.Point;

import org.json.JSONObject;

import server.MainExtension;
import server.Tools.Util;

public class PlayerIslandFactory {
    private static int BASIC_SYNTHESIZER = 857;
    private static int BASIC_BREEDING = 2;
    private static int BASIC_ATTUNER = 856;
    private static int BASIC_CRUCIBLE = 711;
    private static int BASIC_NURSERY = 1;
    private static int BATTLE_NURSERY = 535;
   	private static int BATTLE_GYM = 533;
    private static int BATTLE_HOTEL = 534;
    private static int BASIC_BAKERY = 32;
    
    private static Point BATTLE_GYM_POS = new Point(21, 3);
    private static Point BATTLE_HOTEL_POS = new Point(29, 9);

	private static Point CASTLE_POS = new Point(29, 9);
	private static Point DEFAULT_NURSERY_POS = new Point(35, 17);
	private static Point DEFAULT_BREEDING_POS = new Point(21, 3);
    
    private static final int[] DEFAULT_OBSTACLE_POSITIONS_X = {
    	    14, 5, 9, 22, 7, 6, 16, 5, 7, 11, 13, 10, 20, 15, 12, 16, 22, 11, 26, 24, 18, 23, 3, 16, 2, 8, 14, 28, 6, 16, 13, 2, 22, 19, 11, 13, 28, 29, 22, 19, 6, 14, 26, 11, 12, 17, 21, 10, 7,
    	    17, 1, 15, 9, 19, 25, 14, 21, 19, 10, 9
    };

    private static final int[] DEFAULT_OBSTACLE_POSITIONS_Y = {
    		12, 26, 21, 32, 17, 14, 27, 23, 27, 9, 21, 28, 35, 17, 31, 22, 26, 17, 31, 33, 29, 28, 20, 34, 23, 32, 33, 22, 31, 10, 23, 18, 23, 12, 13, 29, 20, 24, 9, 32, 21, 10, 22, 20, 34, 13,
    		25, 24, 9, 36, 20, 36, 9, 37, 21, 26, 29, 22, 14, 34
    };
    
    public static int getNextUserStructureId(String userGameId) {
    	return new JSONObject(MainExtension.sqlHandler.sendCommand("SELECT COUNT(*) FROM player_structures WHERE user_game_id = '"+userGameId+"';")).getJSONArray("result").getJSONArray(0).getInt(0) + 1;
    }
    
    public static int getNextUserIslandId() {
        String sql = "SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'player_islands'";
        String response = MainExtension.sqlHandler.sendCommand(sql);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("result").getJSONArray(0).getInt(0);
    }
    
    private static void addStructureToIsland(PlayerIsland island, String userGameId, int structureTypeId, int posX, int posY, long serverTime) {
        long userStructureId = getNextUserStructureId(userGameId);

        island.addStructure(serverTime, serverTime, (int) userStructureId, structureTypeId, posX, posY, 0, 1.0);

        String sql = "INSERT INTO player_structures (user_island_id, structure_id, user_structure_id, user_game_id, pos_x, pos_y, scale, flip, name, date_created) VALUES ('"
                + island.user_island_id + "', '"
                + structureTypeId + "', '"
                + userStructureId + "', '"
                + userGameId + "', '"
                + posX + "', '"
                + posY + "', '"
                + 1.0 + "', '"
                + 0 + "', '"
                + "Structure" + "', '"
                + serverTime + "');";
        MainExtension.sqlHandler.sendCommand(sql);
    }

    
    public static void createInitialStructures(PlayerIsland island, String userGameId) {
        long serverTime = Util.getUnixTime();

        if (island.isWublinIsland() || island.isCelestialIsland() || island.isNexusIsland()) {
            return;
        }

        if (island.isBattleIsland()) {
            addStructureToIsland(island, userGameId, BATTLE_HOTEL, BATTLE_HOTEL_POS.x, BATTLE_HOTEL_POS.y, serverTime);
            addStructureToIsland(island, userGameId, BATTLE_GYM, BATTLE_GYM_POS.x, BATTLE_GYM_POS.y, serverTime);
            addStructureToIsland(island, userGameId, BATTLE_NURSERY, DEFAULT_NURSERY_POS.x, DEFAULT_NURSERY_POS.y, serverTime);
        } else {
            if (!(island.isComposerIsland() || island.isGoldIsland() || island.isTribalIsland() || island.isWorkshopIsland())) {
                addStructureToIsland(island, userGameId, BASIC_NURSERY, DEFAULT_NURSERY_POS.x, DEFAULT_NURSERY_POS.y, serverTime);
                if (!island.isAmberIsland()) {
                	addStructureToIsland(island, userGameId, BASIC_BREEDING, DEFAULT_BREEDING_POS.x, DEFAULT_BREEDING_POS.y, serverTime);
                }
            }
            
            if (island.isWorkshopIsland()) {
                addStructureToIsland(island, userGameId, BASIC_SYNTHESIZER, DEFAULT_NURSERY_POS.x, DEFAULT_NURSERY_POS.y, serverTime);
                addStructureToIsland(island, userGameId, BASIC_ATTUNER, DEFAULT_BREEDING_POS.x, DEFAULT_BREEDING_POS.y, serverTime);
            }
        }
    }
}
