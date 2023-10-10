package dev.jacrispys.JavaBot.utils;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import dev.jacrispys.JavaBot.api.libs.utils.async.AsyncHandlerImpl;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Manages instances of the {@link SpotifyApi}
 */
public class SpotifyManager extends AsyncHandlerImpl {

    private Thread thread;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyManager.class);
    private SpotifyApi spotifyApi;

    private static SpotifyManager instance = null;
    private static String accessToken;

    private long cooldown;

    /**
     * Uses credentials to obtain connection to spotify api
     */
    private SpotifyManager() {
        runThread();
    }


    /**
     * Thread update order is as follows...
     * Check cooldown & execute spotify token update
     * Check queues for artist ID requests
     */
    private void runThread() {
        this.thread = new Thread(() -> {
            while (true) {
                if (System.currentTimeMillis() >= cooldown && voidMethodQueue.isEmpty()) {
                    CompletableFuture<Void> cf = new CompletableFuture<>();
                    this.voidMethodQueue.add(new AsyncHandlerImpl.VoidMethodRunner(this::retrieveToken, cf));
                    completeVoid();
                    continue;
                }
                if (spotifyApi != null) {
                    completeMethod();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void retrieveToken() {
        instance = this;
        this.spotifyApi = new SpotifyApi.Builder().setClientId(SecretData.getSpotifyId()).setClientSecret(SecretData.getSpotifySecret()).build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        try {
            try {
                var clientCredentials = clientCredentialsRequest.execute();
                accessToken = clientCredentials.getAccessToken();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                cooldown = System.currentTimeMillis() + ((clientCredentials.getExpiresIn() - 10) * 1000L);
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("Failed to update the spotify access token. Retrying in 1 minute ", e);
                cooldown = System.currentTimeMillis() + (60 * 1000);
            }
        } catch (Exception e) {
            logger.error("Failed to update the spotify access token", e);
        }
    }


    public CompletableFuture<String> getArtistId(String id) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        this.methodQueue.add(new AsyncHandlerImpl.MethodRunner(() -> {
            try {
                String s = null;
                while (s == null) {
                    s = getArtistIdAsync(id);
                }
                cf.complete(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, cf));
        return cf;
    }

    public CompletableFuture<SpotifyApi> getSpotifyApi() {
        CompletableFuture<SpotifyApi> cf = new CompletableFuture<>();
        this.methodQueue.add(new AsyncHandlerImpl.MethodRunner(() -> {
            do {
            } while (spotifyApi == null);
            cf.complete(getSpotifyApiAsync());
        }, cf));
        return cf;

    }

    private SpotifyApi getSpotifyApiAsync() {
        return this.spotifyApi;
    }

    public Thread getAuthThread() {
        return this.thread;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static final String API_BASE = "https://api.spotify.com/v1/";
    private static final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

    /**
     * Gets json object from a given spotify URI
     *
     * @param uri spotify api endpoint to obtain
     * @return json data received from the http request
     * @throws IOException if http request fails
     */
    private JsonBrowser getJson(String uri) throws IOException {
        var request = new HttpGet(uri);
        request.addHeader("Authorization", "Bearer " + getAccessToken());
        return HttpClientTools.fetchResponseAsJson(httpInterfaceManager.getInterface(), request);
    }

    /**
     * Obtains the UUID of an artist from a given song
     */
    private String getArtistIdAsync(String id) throws IOException {
        do {
        } while (accessToken == null);
        var json = getJson(API_BASE + "tracks/" + id);
        if (json == null || json.get("artists").values().isEmpty()) {
            return null;
        }
        return json.get("artists").index(0).get("id").text();
    }

    public static SpotifyManager getInstance() {
        return instance != null ? instance : new SpotifyManager();
    }


    @Override
    public void completeVoid() {
        try {
            for (; ; ) {
                VoidMethodRunner runner = voidMethodQueue.take();
                runner.runnable().run();
                runner.cf().complete(null);
                if (voidMethodQueue.isEmpty()) break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void completeMethod() {
        try {
            for (; ; ) {
                MethodRunner runner = methodQueue.take();
                runner.runnable().run();
                while (true) {
                    if (!runner.cf().isDone() && !runner.cf().isCancelled()) continue;
                    break;
                }
                if (methodQueue.isEmpty()) break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
