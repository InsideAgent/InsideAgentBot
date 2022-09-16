package dev.jacrispys.JavaBot.api.libs.utils;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;
import dev.jacrispys.JavaBot.utils.SecretData;
import io.javalin.Javalin;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;
import org.eclipse.jetty.util.ajax.JSON;

import java.io.IOException;

public class JavalinManager {

    private Javalin app;
    private final long CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String API_ENDPOINT = "https://discord.com/api/oauth2/token";
    private final String REDIRECT_URI = "https://bot.insideagent.pro";

    private final OkHttpClient client = new OkHttpClient();

    public JavalinManager(int port) {
        initJavalin(port);
        this.CLIENT_ID = SecretData.getDiscordId(true);
        this.CLIENT_SECRET = SecretData.getDiscordSecret(true);
    }


    protected void initJavalin(int port) {
        app = Javalin.create().start(port);
        app.get("/", ctx -> {
            String query = ctx.queryParam("code");
            if(query != null) {
                ctx.result("You May now close the tab.");
                if(!exchangeCode(query)) {
                    ctx.html("<body style=color:red;background-color:#121212;> ERROR: Invalid auth code! Please use a valid discord oauth method! If you think this is an error please contact an administrator. </body>");
                }
                return;
            }
            ctx.result("Error, invalid query!");
        });
    }

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
            return false;
        }
    }


    protected void authorizeUser(DataObject userData) {

    }
}
