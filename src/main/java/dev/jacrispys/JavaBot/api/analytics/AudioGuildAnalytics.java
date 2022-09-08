package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.Audio.objects.GuildPlaylist;
import dev.jacrispys.JavaBot.Audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.api.analytics.objects.AudioUser;
import dev.jacrispys.JavaBot.api.analytics.objects.PlayTime;
import dev.jacrispys.JavaBot.api.analytics.objects.TrackStats;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;
import java.util.List;

public interface AudioGuildAnalytics extends AudioAnalytics, GeneralGuildAnalytics {

    // TODO: 9/7/2022 Pull general methods to superclass to allow JDA analytics to access methods.
    GuildPlaylist getGuildPlaylists();
    long getPlaylistPlays(GuildPlaylist playlist);
    List<GuildBookmark> getBookmarks();

}
