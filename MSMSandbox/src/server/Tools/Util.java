package server.Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

import server.MainExtension;
import server.Settings;

public class Util {
    
	static MainExtension ext;
	
	public static String jsonBeauty(String inputJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        try {
            JsonElement jsonElement = JsonParser.parseString(inputJson);
            return gson.toJson(jsonElement);
        } catch (JsonSyntaxException e) {
        	ext.trace("Error parsing JSON: " + e.getMessage());
            return inputJson;
        }
    }
    
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getUnixTime() {
        return Instant.now().getEpochSecond();
    }
        
    public static void WriteFile(File file, String text, boolean append) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            ext.trace("Writefile error: "+e.toString());
        }
    }
    
    public static String ReadFile(File file) {
        try{
            BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            
            br.close();
            
            return everything;
        } catch (IOException e) {
        	ext.trace("IO Exception during readfile: "+ e);
            return null;
        }
    }
    
    public static void putSFSToJson(File file, SFSObject sfsObject){
        WriteFile(file, jsonBeauty(sfsObject.toJson()), false);
    }
    
    public static SFSObject getSFSFromJson(File file){
        String fileName = file.getName();

        String data = ReadFile(file);

        if (data == null) {
        	ext.trace(fileName + "'s data is null!");
            return null;
        }

        SFSObject resp = (SFSObject) SFSObject.newFromJsonData(data);

        return resp;
    }
    
    @SuppressWarnings("resource")
	public static void copyFile(File sourceFile, File destFile) {
        try  {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static String encrypt(String message, String initialVector, String secretKey) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        keyBytes = java.util.Arrays.copyOf(keyBytes, 16);

        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(initialVector.getBytes(StandardCharsets.UTF_8));
        
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public static String decrypt(String encryptedMessage, String initialVector, String secretKey) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        keyBytes = java.util.Arrays.copyOf(keyBytes, 16);
        
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(initialVector.getBytes(StandardCharsets.UTF_8));
        
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        
        byte[] decodedEncryptedMessage = Base64.getDecoder().decode(encryptedMessage);
        byte[] decrypted = cipher.doFinal(decodedEncryptedMessage);
        
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    public static String PostRequest(String urlString, String requestBody, Map<String, String> headers) {
        try {
            @SuppressWarnings("deprecation")
			URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Encoding", "deflate, gzip");

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody);
                writer.flush();
            }

            InputStream inputStream = connection.getInputStream();
            String encoding = connection.getContentEncoding();
            if (encoding != null) {
                if (encoding.equalsIgnoreCase("gzip")) {
                    inputStream = new GZIPInputStream(inputStream);
                } else if (encoding.equalsIgnoreCase("deflate")) {
                    inputStream = new InflaterInputStream(inputStream);
                }
            }

            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String inputLine;
                while ((inputLine = bufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            String responseStr = response.toString().trim();
            if (responseStr.isEmpty()) {
                return "{}";
            } else {
                JSONObject jsonResponse = new JSONObject(responseStr);
                return jsonResponse.toString();
            }


        } catch (IOException e) {
            throw new RuntimeException("Error occurred while connecting to server: " + e.getMessage(), e);
        }
    }

	public static String PostRequest(String urlString, String requestBody) {
		return PostRequest(urlString, requestBody, new HashMap<>());
	}
	
	public static String PostRequest(String urlString) {
		return PostRequest(urlString, "", new HashMap<>());
	}
	
	public static String PostRequest(String urlString, Map<String, String> headers) {
		return PostRequest(urlString, "", headers);
	}
	
    public static String convertBidToFriendCode(String bbbIdInput) {
        if (bbbIdInput == null || !bbbIdInput.matches("\\d+")) {
            return "invalid BBB ID.";
        }

        long bid = Long.parseLong(bbbIdInput);

        int ohoValue = (int) ((Math.floorDiv(bid * 11, 14) % 14) + 61);
        int hohValue = (int) (((bid * 11) % 14) + 61);

        char yoy = intToChar(hohValue);
        char yay = intToChar(ohoValue);

        return bid + String.valueOf(yay) + yoy;
    }

    public static char intToChar(int value) {
        return (char) value;
    }
    
    public static String ipToCountry(String ip) {
        try {
            String json = PostRequest(ip);
            JSONObject obj = new JSONObject(json);
            return obj.getString("country");
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
    
    public static boolean isIslandBroken(int islandId) {
        for (int num : Settings.brokenIslands) {
            if (num == islandId) {
                return true;
            }
        }
        return false;
    }
    
    public static String formatUnixTime(long unixTime) {
        LocalDateTime dateTime = Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        int day = dateTime.getDayOfMonth();
        String suffix = getDaySuffix(day);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d',' yyyy 'at' h:mm:ss a");
        String formatted = dateTime.format(formatter);

        return formatted.replaceFirst(String.valueOf(day), day + suffix);
    }

    private static String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        int lastDigit = day % 10;
        if (lastDigit == 1) {
            return "st";
        } else if (lastDigit == 2) {
            return "nd";
        } else if (lastDigit == 3) {
            return "rd";
        } else {
            return "th";
        }
    }
    
    public static SFSObject cloneSFSObject(SFSObject original) {
        if (original == null) return null;
        byte[] binaryData = original.toBinary();
        return SFSObject.newFromBinaryData(binaryData);
    }

    public static SFSArray cloneSFSArray(SFSArray original) {
        if (original == null) return null;
        byte[] binaryData = original.toBinary();
        return SFSArray.newFromBinaryData(binaryData);
    }
}
