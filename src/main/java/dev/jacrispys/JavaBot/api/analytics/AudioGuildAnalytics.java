package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.audio.objects.GuildPlaylist;

import java.util.List;

public interface AudioGuildAnalytics extends AudioAnalytics, GeneralGuildAnalytics {

    // TODO: 9/7/2022 Pull general methods to superclass to allow JDA analytics to access methods.
    GuildPlaylist getGuildPlaylists();
    long getPlaylistPlays(GuildPlaylist playlist);
    List<GuildBookmark> getBookmarks();

}
