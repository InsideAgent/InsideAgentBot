package dev.jacrispys.JavaBot.utils;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Yaml loader for environment variables from loginInfo file
 */
public class SecretData {
    private static Yaml yaml = new Yaml();
    private static Map<String, Object> loginInfo;

    private static final String DEFAULT_PATH_DIR = "src/main/resources/loginInfo.yml";

    public static void initLoginInfo(String path) throws IOException {
        yaml = new Yaml();
        loginInfo = yaml.load(generateSecretData(path));
    }

    public static void initLoginInfo() throws IOException {
        initLoginInfo(DEFAULT_PATH_DIR);
    }

    private static InputStream generateSecretData(String path) throws IOException {
        File fileExists = new File(path);
        if (!fileExists.exists()) {
            File file = new File(path);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            if (file.createNewFile()) {
                Map<String, Object> fileInfo = getDefaultConfig();
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

        } else return new FileInputStream(path);
    }

    @SafeVarargs
    @NotNull
    private static Map<String, Object> getDefaultConfig(Map.Entry<String, Object>... entry) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("DATA_BASE_PASS", " ");
        fileInfo.put("TOKEN", " ");
        fileInfo.put("APPLE_TOKEN", " ");
        fileInfo.put("DEV-TOKEN", " ");
        fileInfo.put("SPOTIFY_CLIENT_ID", " ");
        fileInfo.put("SPOTIFY_SECRET", " ");
        fileInfo.put("YOUTUBE_PSID", " ");
        fileInfo.put("YOUTUBE_PAPISID", " ");
        fileInfo.put("DB_HOST", "localhost");
        fileInfo.put("BOT_CLIENT_ID", " ");
        fileInfo.put("BOT_CLIENT_SECRET", " ");
        for (Map.Entry<String, Object> entryArgs : entry) {
            fileInfo.put(entryArgs.getKey(), entryArgs.getValue());
        }
        return fileInfo;
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

    public static String getYtEmail() {
        return (String) loginInfo.get("YOUTUBE_EMAIL");
    }

    public static String getYtPass() {
        return (String) loginInfo.get("YOUTUBE_PASS");
    }

    public static Object getCustomData(String key) {
        return loginInfo.get(key);
    }
    public static String getDBHost() {
        return (String) loginInfo.get("DB_HOST");
    }

    public static String getAppleToken() {
        return (String) loginInfo.get("APPLE_TOKEN");
    }

    public static long getDiscordId(boolean dev) {
        if (dev) {
            return (long) loginInfo.get("DEV-BOT_CLIENT_ID");
        }
        return (long) loginInfo.get("BOT_CLIENT_ID");
    }

    public static long getDiscordId() {
        return (long) loginInfo.get("BOT_CLIENT_ID");
    }

    public static String getDiscordSecret(boolean dev) {
        if (dev) {
            return (String) loginInfo.get("DEV-BOT_CLIENT_SECRET");
        }
        return (String) loginInfo.get("BOT_CLIENT_SECRET");
    }

    public static String getDiscordSecret() {
        return (String) loginInfo.get("BOT_CLIENT_SECRET");
    }
}
