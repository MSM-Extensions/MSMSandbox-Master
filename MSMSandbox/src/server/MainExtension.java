package server;

import com.smartfoxserver.v2.extensions.SFSExtension;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.smartfoxserver.v2.core.SFSEvent;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.data.SFSObject;

import server.ServerEventHandler.DisconnectHandler;
import server.ServerEventHandler.JoinZoneHandler;
import server.ServerEventHandler.LoginHandler;
import server.Tools.MSMClient;
import server.Tools.Util;

public class MainExtension extends SFSExtension {
    public static String encryptionVector = Settings.get("encryption_vector");
    public static String encryptionSecretKey = Settings.get("encryption_secret_key");
    
    //public static SFSDBManager dbm;
    
    public static String DBUrl;
    
    public static MSMClient client;
    
    @Override
    public void init() {
        Long startTime = Util.getUnixTime();
        /*
         try {
            String testString = Settings.get("encryption_test_string", true, "n");
            String encrypted = Util.encrypt(testString, encryptionVector, encryptionVector);
            
            String decrypted = Util.decrypt(encrypted, encryptionVector, encryptionVector);
            
            trace(encrypted+"\n"+decrypted);
            
            if (!decrypted.equals(testString)) {
                throw new Exception("Decrypted string is not equal to test string!");
            }
         } catch (Exception e) {
            trace("Something bad happened while testing encryption: "+e.toString());
        }
        */
        
        JSONObject DBUrlRequest = new JSONObject(Util.PostRequest("https://riotlove.pythonanywhere.com/db_server/", ""));
        
        DBUrl = (String) DBUrlRequest.get("url");
        
        addEventHandler(SFSEventType.USER_LOGIN, LoginHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ZONE, JoinZoneHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT, DisconnectHandler.class);
        
        addRequestHandler("keep_alive", GameStateHandler.class);
        addRequestHandler("db_monster", GameStateHandler.class);
        addRequestHandler("db_entity_alt_costs", GameStateHandler.class);
        addRequestHandler("db_versions", GameStateHandler.class);
        addRequestHandler("db_gene", GameStateHandler.class);
        addRequestHandler("db_bakery_foods", GameStateHandler.class);
        addRequestHandler("db_titansoul_levels", GameStateHandler.class);
        addRequestHandler("db_nucleus", GameStateHandler.class);
        addRequestHandler("db_nucleus_reward", GameStateHandler.class);
        addRequestHandler("db_structure", GameStateHandler.class);
        addRequestHandler("db_island", GameStateHandler.class);
        addRequestHandler("db_island_v2", GameStateHandler.class);
        addRequestHandler("db_island_themes", GameStateHandler.class);
        addRequestHandler("db_ethereal_islet", GameStateHandler.class);
        addRequestHandler("db_level", GameStateHandler.class);
        addRequestHandler("db_store", GameStateHandler.class);
        addRequestHandler("db_store_v2", GameStateHandler.class);
        addRequestHandler("db_store_replacements", GameStateHandler.class);
        addRequestHandler("db_scratch_offs", GameStateHandler.class);
        addRequestHandler("db_flexeggdefs", GameStateHandler.class);
        addRequestHandler("gs_quest", GameStateHandler.class);
        addRequestHandler("db_loot", GameStateHandler.class);
        addRequestHandler("gs_store_replacements", GameStateHandler.class);
        addRequestHandler("gs_timed_events", GameStateHandler.class);
        addRequestHandler("gs_rare_monster_data", GameStateHandler.class);
        addRequestHandler("gs_epic_monster_data", GameStateHandler.class);
        addRequestHandler("gs_monster_island_2_island_data", GameStateHandler.class);
        addRequestHandler("gs_flip_levels", GameStateHandler.class);
        addRequestHandler("gs_flip_boards", GameStateHandler.class);
        addRequestHandler("test_types", GameStateHandler.class);
        addRequestHandler("gs_dipster_data", GameStateHandler.class);
        addRequestHandler("gs_entity_alt_cost_data", GameStateHandler.class);
        addRequestHandler("gs_cant_breed", GameStateHandler.class);
        addRequestHandler("gs_player", GameStateHandler.class);
        addRequestHandler("gs_request_next_relic_reset", GameStateHandler.class);
        addRequestHandler("gs_set_displayname", GameStateHandler.class);
        addRequestHandler("gs_set_tribename", GameStateHandler.class);
        addRequestHandler("gs_set_islandname", GameStateHandler.class);
        addRequestHandler("gs_refresh_tribe_requests", GameStateHandler.class);
        addRequestHandler("gs_get_code", GameStateHandler.class);
        addRequestHandler("gs_transfer_code", GameStateHandler.class);
        addRequestHandler("gs_set_moniker", GameStateHandler.class);
        addRequestHandler("gs_set_last_timed_theme", GameStateHandler.class);
        addRequestHandler("gs_update_island_tutorials", GameStateHandler.class);
        addRequestHandler("gs_buy_island", GameStateHandler.class);
        addRequestHandler("gs_change_island", GameStateHandler.class);
        addRequestHandler("gs_place_on_gold_island", GameStateHandler.class);
        addRequestHandler("gs_save_island_warp_speed", GameStateHandler.class);
        addRequestHandler("gs_place_on_tribal", GameStateHandler.class);
        addRequestHandler("gs_cancel_tribe_request", GameStateHandler.class);
        addRequestHandler("gs_send_tribe_request", GameStateHandler.class);
        addRequestHandler("gs_send_tribe_invite", GameStateHandler.class);
        addRequestHandler("gs_join_tribe", GameStateHandler.class);
        addRequestHandler("gs_leave_tribe_request", GameStateHandler.class);
        addRequestHandler("gs_kick_tribe_request", GameStateHandler.class);
        addRequestHandler("gs_cancel_tribe_invite", GameStateHandler.class);
        addRequestHandler("gs_decline_all_tribal_invites", GameStateHandler.class);
        addRequestHandler("gs_get_random_tribes", GameStateHandler.class);
        addRequestHandler("gs_activate_island_theme", GameStateHandler.class);
        addRequestHandler("gs_buy_egg", GameStateHandler.class);
        addRequestHandler("gs_sell_egg", GameStateHandler.class);
        addRequestHandler("gs_hatch_egg", GameStateHandler.class);
        addRequestHandler("gs_speed_up_hatching", GameStateHandler.class);
        addRequestHandler("gs_box_add_egg", GameStateHandler.class);
        addRequestHandler("gs_box_add_monster", GameStateHandler.class);
        addRequestHandler("gs_box_activate_monster", GameStateHandler.class);
        addRequestHandler("gs_attempt_early_box_activate", GameStateHandler.class);
        addRequestHandler("gs_box_purchase_fill_cost", GameStateHandler.class);
        addRequestHandler("gs_box_purchase_fill", GameStateHandler.class);
        addRequestHandler("gs_start_amber_evolve", GameStateHandler.class);
        addRequestHandler("gs_finish_amber_evolve", GameStateHandler.class);
        addRequestHandler("gs_speedup_amber_evolve", GameStateHandler.class);
        addRequestHandler("gs_collect_cruc_heat", GameStateHandler.class);
        addRequestHandler("gs_purchase_evolve_unlock", GameStateHandler.class);
        addRequestHandler("gs_purchase_evo_powerup_unlock", GameStateHandler.class);
        addRequestHandler("gs_viewed_cruc_monst", GameStateHandler.class);
        addRequestHandler("gs_viewed_cruc_unlock", GameStateHandler.class);
        addRequestHandler("gs_send_ethereal_monster", GameStateHandler.class);
        addRequestHandler("gs_send_monster_home", GameStateHandler.class);
        addRequestHandler("gs_move_monster", GameStateHandler.class);
        addRequestHandler("gs_feed_monster", GameStateHandler.class);
        addRequestHandler("gs_sell_monster", GameStateHandler.class);
        addRequestHandler("gs_mute_monster", GameStateHandler.class);
        addRequestHandler("gs_flip_monster", GameStateHandler.class);
        addRequestHandler("gs_collect_monster", GameStateHandler.class);
        addRequestHandler("gs_test_collect_monster", GameStateHandler.class);
        addRequestHandler("gs_name_monster", GameStateHandler.class);
        addRequestHandler("gs_multi_neighbors", GameStateHandler.class);
        addRequestHandler("gs_store_monster", GameStateHandler.class);
        addRequestHandler("gs_unstore_monster", GameStateHandler.class);
        addRequestHandler("gs_tribal_feed_monster", GameStateHandler.class);
        addRequestHandler("gs_breed_monsters", GameStateHandler.class);
        addRequestHandler("gs_finish_breeding", GameStateHandler.class);
        addRequestHandler("gs_speed_up_breeding", GameStateHandler.class);
        addRequestHandler("gs_unlock_breeding_structure", GameStateHandler.class);
        addRequestHandler("gs_buy_structure", GameStateHandler.class);
        addRequestHandler("gs_move_structure", GameStateHandler.class);
        addRequestHandler("gs_sell_structure", GameStateHandler.class);
        addRequestHandler("gs_flip_structure", GameStateHandler.class);
        addRequestHandler("gs_mute_structure", GameStateHandler.class);
        addRequestHandler("gs_start_upgrade_structure", GameStateHandler.class);
        addRequestHandler("gs_finish_upgrade_structure", GameStateHandler.class);
        addRequestHandler("gs_finish_structure", GameStateHandler.class);
        addRequestHandler("gs_collect_structure", GameStateHandler.class);
        addRequestHandler("gs_speed_up_structure", GameStateHandler.class);
        addRequestHandler("gs_store_decoration", GameStateHandler.class);
        addRequestHandler("gs_unstore_decoration", GameStateHandler.class);
        addRequestHandler("gs_store_buddy", GameStateHandler.class);
        addRequestHandler("gs_unstore_buddy", GameStateHandler.class);
        addRequestHandler("gs_mega_monster_message", GameStateHandler.class);
        addRequestHandler("gs_light_torch", GameStateHandler.class);
        addRequestHandler("gs_get_torchgifts", GameStateHandler.class);
        addRequestHandler("gs_collect_torchgift", GameStateHandler.class);
        addRequestHandler("gs_additional_friend_torch_data", GameStateHandler.class);
        addRequestHandler("gs_set_light_torch_flag", GameStateHandler.class);
        addRequestHandler("gs_visit_specific_friend_island", GameStateHandler.class);
        addRequestHandler("gs_set_fav_friend", GameStateHandler.class);
        addRequestHandler("gs_start_baking", GameStateHandler.class);
        addRequestHandler("gs_finish_baking", GameStateHandler.class);
        addRequestHandler("gs_speed_up_baking", GameStateHandler.class);
        addRequestHandler("gs_start_rebake", GameStateHandler.class);
        addRequestHandler("gs_start_fuzing", GameStateHandler.class);
        addRequestHandler("gs_finish_fuzing", GameStateHandler.class);
        addRequestHandler("gs_speed_up_fuzing", GameStateHandler.class);
        addRequestHandler("gs_collect_from_mine", GameStateHandler.class);
        addRequestHandler("gs_start_obstacle", GameStateHandler.class);
        addRequestHandler("gs_clear_obstacle", GameStateHandler.class);
        addRequestHandler("gs_clear_obstacle_speed_up", GameStateHandler.class);
        addRequestHandler("gs_quest_event", GameStateHandler.class);
        addRequestHandler("gs_quest_read", GameStateHandler.class);
        addRequestHandler("gs_quests_read", GameStateHandler.class);
        addRequestHandler("gs_quest_collect", GameStateHandler.class);
        addRequestHandler("gs_update_achievement_status", GameStateHandler.class);
        addRequestHandler("gs_get_friends", GameStateHandler.class);
        addRequestHandler("gs_get_messages", GameStateHandler.class);
        addRequestHandler("gs_delete_message", GameStateHandler.class);
        addRequestHandler("gs_get_friend_visit_data", GameStateHandler.class);
        addRequestHandler("gs_get_random_visit_data", GameStateHandler.class);
        addRequestHandler("gs_get_tribal_island_data", GameStateHandler.class);
        addRequestHandler("gs_get_ranked_island_data", GameStateHandler.class);
        addRequestHandler("gs_get_island_rank", GameStateHandler.class);
        addRequestHandler("gs_rate_island", GameStateHandler.class);
        addRequestHandler("gs_report_user", GameStateHandler.class);
        addRequestHandler("gs_collect_invite_reward", GameStateHandler.class);
        addRequestHandler("gs_collect_rewards", GameStateHandler.class);
        addRequestHandler("gs_process_unclaimed_purchases", GameStateHandler.class);
        addRequestHandler("gs_currency_conversion", GameStateHandler.class);
        addRequestHandler("gs_currency_coins2eth_conversion", GameStateHandler.class);
        addRequestHandler("gs_currency_diamonds2eth_conversion", GameStateHandler.class);
        addRequestHandler("gs_currency_eth2diamonds_conversion", GameStateHandler.class);
        addRequestHandler("gs_currency_generic_conversion", GameStateHandler.class);
        addRequestHandler("gs_collect_facebook_reward", GameStateHandler.class);
        addRequestHandler("gs_referral_request", GameStateHandler.class);
        addRequestHandler("gs_collect_daily_currency_pack", GameStateHandler.class);
        addRequestHandler("gs_refresh_daily_currency_pack", GameStateHandler.class);
        addRequestHandler("gs_player_has_scratch_off", GameStateHandler.class);
        addRequestHandler("gs_play_scratch_off", GameStateHandler.class);
        addRequestHandler("gs_purchase_scratch_off", GameStateHandler.class);
        addRequestHandler("gs_collect_scratch_off", GameStateHandler.class);
        addRequestHandler("gs_get_memory_game_numbers", GameStateHandler.class);
        addRequestHandler("gs_memory_minigame_current_cost", GameStateHandler.class);
        addRequestHandler("gs_flip_minigame_cost", GameStateHandler.class);
        addRequestHandler("gs_purchase_memory_mini_game", GameStateHandler.class);
        addRequestHandler("gs_collect_memory_mini_game", GameStateHandler.class);
        addRequestHandler("gs_collect_flip_level", GameStateHandler.class);
        addRequestHandler("gs_collect_flip_mini_game", GameStateHandler.class);
        addRequestHandler("gs_sticker", GameStateHandler.class);
        addRequestHandler("gs_purchase_flip_mini_game", GameStateHandler.class);
        addRequestHandler("gs_delete_composer_template", GameStateHandler.class);
        addRequestHandler("gs_save_composer_template", GameStateHandler.class);
        addRequestHandler("gs_save_composer_track", GameStateHandler.class);
        addRequestHandler("gs_delete_mail", GameStateHandler.class);
        addRequestHandler("gs_offer_viewed", GameStateHandler.class);
        addRequestHandler("gs_offer_completed", GameStateHandler.class);
        addRequestHandler("gs_promos", GameStateHandler.class);
        addRequestHandler("gs_paywall_updated", GameStateHandler.class);
        addRequestHandler("gs_app_link", GameStateHandler.class);
        addRequestHandler("gs_send_facebook_help", GameStateHandler.class);
        addRequestHandler("gs_handle_facebook_help_instances", GameStateHandler.class);
        addRequestHandler("gs_request_facebook_help_permissions", GameStateHandler.class);
        addRequestHandler("gs_purchase_buyback", GameStateHandler.class);
        addRequestHandler("gs_admin_purchase_buyback", GameStateHandler.class);
        addRequestHandler("gs_collect_daily_reward", GameStateHandler.class);
        addRequestHandler("gs_daily_login_buyback", GameStateHandler.class);
        addRequestHandler("gs_give_me_shit", GameStateHandler.class);
        addRequestHandler("gs_add_friend", GameStateHandler.class);
        addRequestHandler("gs_remove_friend", GameStateHandler.class);
        addRequestHandler("gs_sync_friends", GameStateHandler.class);
        addRequestHandler("gs_delete_account", GameStateHandler.class);
        addRequestHandler("db_battle", GameStateHandler.class);
        addRequestHandler("db_battle_levels", GameStateHandler.class);
        addRequestHandler("db_battle_monster_training", GameStateHandler.class);
        addRequestHandler("db_battle_monster_actions", GameStateHandler.class);
        addRequestHandler("db_battle_monster_stats", GameStateHandler.class);
        addRequestHandler("db_battle_music", GameStateHandler.class);
        addRequestHandler("db_breeding", GameStateHandler.class);
        addRequestHandler("db_polarity_amplifier_levels", GameStateHandler.class);
        addRequestHandler("battle_teleport", GameStateHandler.class);
        addRequestHandler("battle_start", GameStateHandler.class);
        addRequestHandler("battle_start_versus", GameStateHandler.class);
        addRequestHandler("battle_start_friend", GameStateHandler.class);
        addRequestHandler("battle_finish", GameStateHandler.class);
        addRequestHandler("battle_start_training", GameStateHandler.class);
        addRequestHandler("battle_finish_training", GameStateHandler.class);
        addRequestHandler("battle_purchase_campaign_reward", GameStateHandler.class);
        addRequestHandler("battle_set_music", GameStateHandler.class);
        addRequestHandler("battle_refresh_versus_attempts", GameStateHandler.class);
        addRequestHandler("battle_claim_versus_rewards", GameStateHandler.class);
        addRequestHandler("update_viewed_campaigns", GameStateHandler.class);
        addRequestHandler("admin_battle_campaign_reset", GameStateHandler.class);
        addRequestHandler("gs_set_avatar", GameStateHandler.class);
        addRequestHandler("db_costumes", GameStateHandler.class);
        addRequestHandler("purchase_costume", GameStateHandler.class);
        addRequestHandler("equip_costume", GameStateHandler.class);
        addRequestHandler("update_awakener", GameStateHandler.class);
        addRequestHandler("gs_viewed_egg", GameStateHandler.class);
        addRequestHandler("db_daily_cumulative_login", GameStateHandler.class);
        addRequestHandler("collect_daily_cumulative_login_rewards", GameStateHandler.class);
        addRequestHandler("gs_start_attuning", GameStateHandler.class);
        addRequestHandler("gs_finish_attuning", GameStateHandler.class);
        addRequestHandler("gs_speedup_attuning", GameStateHandler.class);
        addRequestHandler("db_attuner_gene", GameStateHandler.class);
        addRequestHandler("gs_start_synthesizing", GameStateHandler.class);
        addRequestHandler("gs_speedup_synthesizing", GameStateHandler.class);
        addRequestHandler("gs_collect_synthesizing_failure", GameStateHandler.class);
        
        JSONObject tokenRequestJson = new JSONObject();
		
		JSONObject tokenRequest = new JSONObject(Util.PostRequest("https://auth.bbbgame.net/auth/api/anon_account/?g=27", tokenRequestJson.toString()));
		
		trace(tokenRequest.toString());
        
        client = new MSMClient(tokenRequest.getString("username"), tokenRequest.getString("password"), "anon", "4.8.2", "70ba5d5d-d903-4587-93d6-655c4814844f", true);
        
        SFSObject auth = client.auth();
        
        trace("Cacheing dbs");
        
        if (auth.getBool("ok")) {
        	SFSObject pregameSetup = client.pregameSetup();
        	if (pregameSetup.getBool("ok")) {
        		client.connectToServer();
        	} else {
        		trace("Pregame Setup failed: "+pregameSetup.getUtfString("message"));
        	}
        } else {
        	trace("Auth failed: "+auth.getUtfString("message"));
        }
        
        trace("Cacheing complete! MSM Sandbox initialized");
    }

}
