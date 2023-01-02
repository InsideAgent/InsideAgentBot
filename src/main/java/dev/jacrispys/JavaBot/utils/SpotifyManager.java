package dev.jacrispys.JavaBot.utils;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

public class SpotifyManager {

    private final Thread thread;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyManager.class);
    private final SpotifyApi spotifyApi;

    private static SpotifyManager instance = null;
    private static String accessToken;

    private SpotifyManager() {
        instance = this;
        this.spotifyApi = new SpotifyApi.Builder().setClientId(SecretData.getSpotifyId()).setClientSecret(SecretData.getSpotifySecret()).build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        this.thread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        var clientCredentials = clientCredentialsRequest.execute();
                        accessToken = clientCredentials.getAccessToken();
                        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                        Thread.sleep((clientCredentials.getExpiresIn() - 10) * 1000L);
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        logger.error("Failed to update the spotify access token. Retrying in 1 minute ", e);
                        Thread.sleep(60 * 1000);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to update the spotify access token", e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public SpotifyApi getSpotifyApi() {
        return this.spotifyApi;
    }

    public Thread getAuthThread() {
        return this.thread;
    }

    protected static String getAccessToken() {
        return accessToken;
    }

    public static final String API_BASE = "https://api.spotify.com/v1/";
    private static final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
    private static JsonBrowser getJson(String uri) throws IOException {
        try {
            var request = new HttpGet(uri);
            request.addHeader("Authorization", "Bearer " + getAccessToken());
            return HttpClientTools.fetchResponseAsJson(httpInterfaceManager.getInterface(), request);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String getArtistId(String id) throws IOException {
        try {
            var json = getJson(API_BASE + "tracks/" + id);
            if (json == null || json.get("artists").values().isEmpty()) {
                return null;
            }
            return json.get("artists").index(0).get("id").text();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static SpotifyManager getInstance() {
        return instance != null ? instance : new SpotifyManager();
    }
}
