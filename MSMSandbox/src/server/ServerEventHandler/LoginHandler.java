package server.ServerEventHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import server.Logger;
import server.MainExtension;
import server.Util;

public class LoginHandler extends BaseServerEventHandler {
	
    public static String cleanTokenString(String input) {
        int index = input.indexOf("}");
        return (index != -1) ? input.substring(0, index + 1) : input;
    }

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSLoginException {
        ISession session = (ISession) event.getParameter(SFSEventParam.SESSION);
        SFSObject additionalData = (SFSObject) event.getParameter(SFSEventParam.LOGIN_IN_DATA);

        try {
            if (!additionalData.containsKey("token")) {
                Logger.log("Login Data does not contain Auth Token!");
                throw new Exception("ERROR: User attempting to login with invalid auth token");
            } else {
                try {
                    String token = cleanTokenString(Util.decrypt(additionalData.getUtfString("token"), MainExtension.encryptionVector, MainExtension.encryptionSecretKey));
                    Logger.log("Decrypted Token: " + token);

                    JSONObject jsonObject = new JSONObject(Util.jsonBeauty(token));
                    JSONArray gameIdsJson = jsonObject.getJSONArray("user_game_ids");

                    session.setProperty("user_game_id", gameIdsJson.get(0));
                    session.setProperty("username", jsonObject.get("username"));
                    session.setProperty("loginType", jsonObject.get("login_type"));

                    if (additionalData.containsKey("last_updated")) {
                        session.setProperty("last_updated", additionalData.getLong("last_updated"));
                    }

                    if (additionalData.containsKey("client_version")) {
                        session.setProperty("client_version", additionalData.getUtfString("client_version"));
                    }

                    if (additionalData.containsKey("last_update_version")) {
                        session.setProperty("last_update_version", additionalData.getUtfString("last_update_version"));
                    }

                    if (additionalData.containsKey("client_device")) {
                        session.setProperty("client_device", additionalData.getUtfString("client_device"));
                    } else {
                        session.setProperty("client_device", "");
                    }

                    if (additionalData.containsKey("client_os")) {
                        session.setProperty("client_os", additionalData.getUtfString("client_os"));
                    } else {
                        session.setProperty("client_os", "");
                    }

                    if (additionalData.containsKey("client_platform")) {
                        session.setProperty("client_platform", additionalData.getUtfString("client_platform"));
                    } else {
                        session.setProperty("client_platform", "ios");
                    }

                    if (additionalData.containsKey("client_subplatform")) {
                        session.setProperty("client_subplatform", additionalData.getUtfString("client_subplatform"));
                    } else {
                        session.setProperty("client_subplatform", "");
                    }

                    if (additionalData.containsKey("raw_device_id")) {
                        session.setProperty("raw_device_id", additionalData.getUtfString("raw_device_id"));
                    } else {
                        session.setProperty("raw_device_id", "");
                    }

                    if (additionalData.containsKey("client_lang")) {
                        session.setProperty("client_lang", additionalData.getUtfString("client_lang"));
                    } else {
                        session.setProperty("client_lang", "");
                    }

                    if (additionalData.containsKey("access_key")) {
                        session.setProperty("access_key", additionalData.getUtfString("access_key"));
                    }
                } catch (Exception e) {
                    Logger.log("Something happened while handling Login: " + e.toString());
                    throw new SFSLoginException("Error while processing login"+ e);
                }
            }
        } catch (Exception e) {
            trace("Something happened during login handling: " + e.toString());
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.GENERIC_ERROR);
            errData.addParameter("04");
            throw new SFSLoginException("Generic Error", errData);
        }
    }
}
