package server;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class Logger {
	static String zoneName = Settings.get("zone");
	static SFSExtension ext = (SFSExtension) SmartFoxServer.getInstance().getZoneManager().getZoneByName(zoneName).getExtension();
	
	public static void log(Object params) {
		ext.trace(params.toString());
	}
}
