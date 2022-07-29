package dev.jacrispys.JavaBot.Utils;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SecretData {
    private static Yaml yaml = new Yaml();
    private static Map<String, Object> loginInfo;

    public static void initLoginInfo() {
        yaml = new Yaml();
        loginInfo = yaml.load(SecretData.class.getClassLoader().getResourceAsStream("loginInfo.yml"));
    }

    public static void generateSecretData() throws Exception {
        if (SecretData.class.getClassLoader().getResourceAsStream("loginInfo.yml") == null) {
            File file = new File("src/main/resources/loginInfo.yml");
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            if(file.createNewFile()) {
                try {
                    yaml = new Yaml();
                    Map<String, Object> fileInfo = new HashMap<>();

                    fileInfo.put("TOKEN", " ");
                    fileInfo.put("DEV-TOKEN", " ");
                    FileWriter writer = new FileWriter(file.getPath());
                    yaml.dump(fileInfo, writer);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }


    public static String getToken() {
        return (String) loginInfo.get("TOKEN");
    }

    public static String getToken(boolean dev) {
        return dev ? (String) loginInfo.get("TOKEN-DEV") : (String) loginInfo.get("TOKEN");
    }

    public static String getSpotifySecret() {
        return (String) loginInfo.get("SPOTIFY_SECRET");
    }

    public static String getSpotifyId() {
        return (String) loginInfo.get("SPOTIFY_CLIENT_ID");
    }

    public static String getDataBasePass() {
        return (String) loginInfo.get("DATA_BASE_PASS");
    }

    public static String getPSID() {
        return (String) loginInfo.get("YOUTUBE_PSID");
    }

    public static String getPAPISID() {
        return (String) loginInfo.get("YOUTUBE_PAPISID");
    }

    public static Object getCustomData(String key) {
        return loginInfo.get(key);
    }

}
