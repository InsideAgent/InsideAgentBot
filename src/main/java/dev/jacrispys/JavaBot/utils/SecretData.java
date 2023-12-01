package dev.jacrispys.JavaBot.utils;

import com.sun.jna.platform.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resources;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.protobuf.CodedOutputStream.DEFAULT_BUFFER_SIZE;

/**
 * Yaml loader for environment variables from loginInfo file
 */
public class SecretData {
    private static Yaml yaml = new Yaml();
    private static Map<String, Object> loginInfo;

    private static final Logger logger = LoggerFactory.getLogger(SecretData.class);

    public static void initLoginInfo() throws IOException {
        yaml = new Yaml();
        loginInfo = yaml.load(generateSecretData());
        logger.info("{} - Reloading login info configuration file!", SecretData.class.getSimpleName());
    }

    private static String getClassPath()  {
        try {
            return Paths.get(SecretData.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    protected static InputStream generateSecretData() throws IOException {
        String path = getClassPath() + File.separator + "config" + File.separator + "loginInfo.yml";
        logger.info(path);
        File f = new File(path);
        if (!f.exists()) {
            logger.info("{} - Login info file not found! Generating a new file.", SecretData.class.getSimpleName());
            File file = new File(path);
            if(!file.getParentFile().mkdirs()) logger.error("Did not create directories!");
            if (file.createNewFile()) {
                Map<String, Object> fileInfo = getDefaultConfig();
                FileWriter writer = new FileWriter(file.getPath());
                fileInfo.keySet().forEach(key -> {
                    try {
                        writer.write(key + ": " + fileInfo.get(key) + "\n");
                    } catch (IOException e) {
                        logger.error("{} - Error occurred while writing to data file. \n" + e.getMessage(), SecretData.class.getSimpleName());
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
            String oldValue = loginInfo.get(key).toString();
            loginInfo.put(key, value);
            File file = new File(getClassPath() + File.separator + "config" + File.separator + "loginInfo.yml");
            FileWriter writer = new FileWriter(file);
            loginInfo.keySet().forEach(keys -> {
                try {
                    writer.write(keys + ": " + loginInfo.get(keys) + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.flush();
            writer.close();
            io = new FileInputStream(file);
            loginInfo = yaml.load(io);
            logger.info("{} - Yaml value overridden! Key: " + key + ", Old Value: " + oldValue + ", New Value: " + value, SecretData.class.getSimpleName());
            return true;
        } catch (IOException ex) {
            logger.error("{} - Error occurred while accessing data file. \n" + ex.getMessage(), SecretData.class.getSimpleName());
            return false;
        }
    }
}
