package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.audio.objects.GuildPlaylistImpl;

import java.util.List;

/**
 * Child interface to {@link AudioAnalytics} and {@link GeneralGuildAnalytics}
 * <br> allows specific functions unique to guild analytics that are connected to Audio functions
 */
public interface AudioGuildAnalytics extends AudioAnalytics, GeneralGuildAnalytics {

    // TODO: 9/7/2022 Pull general methods to superclass to allow JDA analytics to access methods.
    GuildPlaylistImpl getGuildPlaylists();
    long getPlaylistPlays(GuildPlaylistImpl playlist);
    List<GuildBookmark> getBookmarks();

}
