package dev.jacrispys.JavaBot.audio.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaylistBuilder {

    // TODO: 9/29/2022 DB setup, impl in Java

    private final Guild guild;
    private final String playlistUrl;
    @Nullable
    private User owner = null;

    private PlaylistBuilder(Guild guild, String playlistUrl) {
        this.guild = guild;
        this.playlistUrl = playlistUrl;
    }


    public static PlaylistBuilder createPlaylist(@NotNull Guild guild, @NotNull String playlistUrl) {
        return new PlaylistBuilder(guild, playlistUrl);
    }

    @Nullable
    public PlaylistBuilder setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    public GuildPlaylist build() {
        return new GuildPlaylistImpl(this.guild, this.playlistUrl, this.owner);
    }
}
