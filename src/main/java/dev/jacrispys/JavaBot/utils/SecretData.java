package dev.jacrispys.JavaBot.utils;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yaml loader for environment variables from loginInfo file
 */
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

        } else return SecretData.class.getClassLoader().getResourceAsStream("loginInfo.yml");
    }

    @NotNull
    private static Map<String, Object> getDefaultConfig() {
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
        fileInfo.put("TEST_DATA", "Hello, World!");
        fileInfo.put("SUPER_USERS", List.of(731364923120025705L, 327167869236543490L));
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
        return loginInfo.getOrDefault(key, null);
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

    @SuppressWarnings("unchecked")
    public static List<Long> getSuperUsers() {
        return (List<Long>) loginInfo.get("SUPER_USERS");
    }

    public static boolean setCustomData(String key, Object value) {
        try {
            InputStream io;
            File file = new File("src/main/resources/loginInfo.yml");
            loginInfo.put(key, value);
            FileWriter writer = new FileWriter(file.getPath());
            loginInfo.keySet().forEach(keys -> {
                try {
                    writer.write(keys + ": " + loginInfo.get(keys) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.flush();
            writer.close();
            io = new FileInputStream(file);
            loginInfo = yaml.load(io);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
