package dev.jacrispys.JavaBot.audio.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

public class GuildPlaylistImpl implements GuildPlaylist {

    private final Guild guild;
    private final String playlistUrl;
    @Nullable
    private final User owner;

    public GuildPlaylistImpl(Guild guild, String playlistUrl, @Nullable User owner) {
        this.guild = guild;
        this.playlistUrl = playlistUrl;
        this.owner = owner;

        indexPlaylist(guild, playlistUrl, owner);
    }

    protected void indexPlaylist(Guild guild, String playlistUrl, User owner) {

    }

    /**
     * @return guild that the playlist was created for
     */
    @Override
    public Guild getGuild() {
        return this.guild;
    }

    /**
     * @return string url of the playlist
     */
    @Override
    public String getPlaylist() {
        return this.playlistUrl;
    }

    /**
     * @return owner of the playlist (person who created it)
     */
    @Override
    @Nullable
    public User getOwner() {
        return this.owner;
    }
}