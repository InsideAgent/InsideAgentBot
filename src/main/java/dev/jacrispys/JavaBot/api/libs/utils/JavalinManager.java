package dev.jacrispys.JavaBot.api.libs.utils;

import dev.jacrispys.JavaBot.commands.UnclassifiedSlashCommands;
import dev.jacrispys.JavaBot.utils.SecretData;
import dev.jacrispys.JavaBot.utils.mysql.SqlInstanceManager;
import io.javalin.Javalin;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

/**
 * Web-based event listener to exchange Code's through Discord 0Auth
 * <br> Link to API site: <a href="https://bot.insideagent.pro">bot.insideagent.pro</a>
 */
public class JavalinManager {

    private Javalin app;
    private final long CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String API_ENDPOINT = "https://discord.com/api/oauth2/token";
    private final String REDIRECT_URI = "https://bot.insideagent.pro";

    private final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(JavalinManager.class);

    public JavalinManager(int port) {
        initJavalin(port);
        this.CLIENT_ID = SecretData.getDiscordId(false);
        this.CLIENT_SECRET = SecretData.getDiscordSecret(false);
    }


    /**
     * Initializes Javalin server to listen for queries
     * @param port port to start the server on
     */
    protected void initJavalin(int port) {
        app = Javalin.create().start(port);
        app.get("/", ctx -> {
            String query = ctx.queryParam("code");
            if(query != null) {
                if(!exchangeCode(query)) {
                    ctx.html("<body style=color:red;background-color:#121212;> ERROR: Invalid auth code! Please use a valid discord oauth method! If you think this is an error please contact an administrator. </body>");
                    return;
                }
                ctx.redirect("/success");
                app.get("/success", ctx1 -> ctx1.html("<body style=color:green;background-color:#121212;> Success! You May now close the tab. </body>"));
                return;
            }
            ctx.result("Error, invalid query!");
        });
    }

    /**
     * Http Request builder to exchange the 0Auth code given by discord for credentials
     * @param code code to exchange
     * @return True if data was successfully obtained from Discord, false otherwise
     */
    protected boolean exchangeCode(String code) {
        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", String.valueOf(CLIENT_ID))
                .add("client_secret", CLIENT_SECRET)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .build();
        Request request = new Request.Builder()
                .url(API_ENDPOINT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {

            DataObject jsonResponse =  DataObject.fromJson(response.body().byteStream());
            if (jsonResponse.hasKey("error")) {
                logger.error("Could not create auth token: " + jsonResponse.get("error"));
                logger.error(jsonResponse.toString());
                return false;
            } else {
                String token = jsonResponse.get("token_type") + " " + jsonResponse.getString("access_token");
                Request getData = new Request.Builder()
                        .header("Authorization", token)
                        .url("https://discord.com/api/users/@me")
                        .build();
                Response data = client.newCall(getData).execute();
                DataObject userData = DataObject.fromJson(data.body().byteStream());
                authorizeUser(userData);
                return true;
            }

        } catch (IOException e) {
            logger.error("{} -  Could not create auth token: " +  e.getMessage(), getClass().getSimpleName());
            return false;
        } catch (SQLException ex) {
            logger.error("{} -  SQL error while trying to generate API token!", getClass().getSimpleName());
            return true;
        }
    }


    /**
     * Takes data from the {@link JavalinManager#exchangeCode(String)} method to insert into the database
     * @param userData {@link DataObject} (json) compiled from the Http request
     * @throws SQLException if a database error occurs
     */
    protected void authorizeUser(DataObject userData) throws SQLException {
        try {
            String email = userData.getString("email");
            String avatarUrl = "https://cdn.discordapp.com/avatars/" + userData.getString("id") + "/" + userData.getString("avatar");
            String user_tag = userData.getString("username") + "#" + userData.getString("discriminator");
            String token = tokenGenerator();
            long id = userData.getLong("id");

            Connection sql = SqlInstanceManager.getInstance().getConnectionAsync().get();
            boolean dev = false;
            ResultSet query;
            query = sql.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT dev_auth FROM api_auth WHERE user_id=" + id);
            query.beforeFirst();
            if (query.next()) dev = query.getBoolean("dev_auth");
            Statement stmt = sql.createStatement();
            stmt.execute("REPLACE INTO api_auth (user_id, email, avatar_url, user_tag, token, dev_auth) VALUES ('" + id + "', '" + email + "', '" + avatarUrl + "', '" + user_tag + "', '" + token + "', '" + (dev ? 1 : 0) + "');");
            stmt.close();
            query.close();
            UnclassifiedSlashCommands.notifyAuthUser(id, token);
            logger.info("{} -  API token generated for user: " + user_tag + "(" + userData.getString("id") + ")", getClass().getSimpleName());
            logger.info("{} -  API token user email: " + email, getClass().getSimpleName());

    } catch (SQLException | InterruptedException | ExecutionException ex) {
            logger.error("{} -  An unknown error occurred while generating API token. \n" + ex.getMessage(), getClass().getSimpleName());
        }
    }

    /**
     * Creates a token using a base 64 encoder that will be sent VIA dm's to the user
     * @return the token generated
     */
    protected String tokenGenerator() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return encoder.encodeToString(randomBytes);
    }


}
