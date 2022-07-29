package dev.jacrispys.JavaBot.Utils;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SecretData {
    private static Yaml yaml = new Yaml();
    private static Map<String, Object> loginInfo;

    public static void initLoginInfo() throws IOException {
        yaml = new Yaml();
        loginInfo = yaml.load(generateSecretData());
    }

    protected static InputStream generateSecretData() throws IOException {
        if (SecretData.class.getClassLoader().getResourceAsStream("loginInfo.yml") == null) {
            File file = new File("src/main/resources/loginInfo.yml");
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            if (file.createNewFile()) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("DATA_BASE_PASS", " ");
                fileInfo.put("TOKEN", " ");
                fileInfo.put("DEV-TOKEN", " ");
                fileInfo.put("SPOTIFY_CLIENT_ID", " ");
                fileInfo.put("SPOTIFY_SECRET", " ");
                fileInfo.put("YOUTUBE_PSID", " ");
                fileInfo.put("YOUTUBE_PAPISID", " ");
                FileWriter writer = new FileWriter(file.getPath());
                fileInfo.keySet().forEach(key -> {
                    try {
                        writer.write(key + ": " + fileInfo.get(key) + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.flush();
                writer.close();
                return new FileInputStream(file);
            } else throw new FileNotFoundException("Could not create required config file!");

        } else return SecretData.class.getClassLoader().getResourceAsStream("loginInfo.yml");
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
