package dev.jacrispys.JavaBot.Utils;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class SecretData {
    private static final Yaml yaml = new Yaml();
    private static final Map<String, Object> loginInfo = yaml.load(SecretData.class.getClassLoader().getResourceAsStream("loginInfo.yml"));


    public static String getToken() {
        return (String) loginInfo.get("TOKEN");
    }

    public static String getToken(boolean dev) { return dev ? (String) loginInfo.get("TOKEN-DEV") : (String) loginInfo.get("TOKEN"); }

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
