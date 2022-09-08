package dev.jacrispys.JavaBot.utils;

import org.apache.http.ParseException;
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

    private SpotifyManager() {
        instance = this;
        this.spotifyApi = new SpotifyApi.Builder().setClientId(SecretData.getSpotifyId()).setClientSecret(SecretData.getSpotifySecret()).build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        this.thread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        var clientCredentials = clientCredentialsRequest.execute();
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

    public static SpotifyManager getInstance() {
        return instance != null ? instance : new SpotifyManager();
    }
}
