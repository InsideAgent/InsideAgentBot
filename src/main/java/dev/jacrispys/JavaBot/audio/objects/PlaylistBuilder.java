package dev.jacrispys.JavaBot.audio.objects;

import net.dv8tion.jda.api.entities.Guild;

public class PlaylistBuilder {

    // TODO: 9/29/2022 DB setup, impl in Java

    private final Guild guild;
    private final String playlistUrl;

    private PlaylistBuilder(Guild guild, String playlistUrl) {
        this.guild = guild;
        this.playlistUrl = playlistUrl;

        int i = 0;
        for(; i < 5; i++) {

        }
    }


    public static PlaylistBuilder createPlaylist(Guild guild, String playlistUrl) {
        return new PlaylistBuilder(guild, playlistUrl);
    }

    public Guild getGuild() {
        return guild;
    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public GuildPlaylist build() {
        return new GuildPlaylistImpl(this);
    }
}
