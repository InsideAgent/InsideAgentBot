package unit;

import dev.jacrispys.JavaBot.utils.SecretData;
import dev.jacrispys.JavaBot.utils.SpotifyManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpManTests {

    private final String TEST_TRACK = "6shRGWCtBUOPFLFTTqXZIC?si=f88f88fde04e49c4";
    @Test
    void spotifyManagerInstance() {
        SpotifyManager man = SpotifyManager.getInstance();
        Assertions.assertNotNull(man);
    }

    @Test
    void spotifyApi() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        SecretData.initLoginInfo();
        SpotifyManager man = SpotifyManager.getInstance();
        String host = man.getSpotifyApi().get().getHost();
        Assertions.assertNotNull(host);
    }

    @Test
    void spotifyArtistId() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        String expected = "3GBPw9NK25X1Wt2OUvOwY3";
        SecretData.initLoginInfo();
        SpotifyManager man = SpotifyManager.getInstance();
        String actual = man.getArtistId(TEST_TRACK).get(10000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(expected, actual);
    }
}
