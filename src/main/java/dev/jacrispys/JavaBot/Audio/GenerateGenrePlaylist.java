package dev.jacrispys.JavaBot.Audio;

import com.neovisionaries.i18n.CountryCode;
import dev.jacrispys.JavaBot.Utils.SpotifyManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateGenrePlaylist extends ListenerAdapter {

    public GenerateGenrePlaylist() {

    }

    public Recommendations generatePlaylistFromGenre(String genres, int limit) throws IOException, ParseException, SpotifyWebApiException {
        final GetRecommendationsRequest request = SpotifyManager.getInstance().getSpotifyApi().getRecommendations().market(CountryCode.ES).seed_genres(genres).limit(limit).build();
        return request.execute();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {

    }
}
