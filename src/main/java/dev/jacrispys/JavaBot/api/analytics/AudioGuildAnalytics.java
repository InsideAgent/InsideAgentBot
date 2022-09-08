package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.Audio.objects.GuildPlaylist;
import dev.jacrispys.JavaBot.Audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.api.analytics.objects.AudioUser;
import dev.jacrispys.JavaBot.api.analytics.objects.PlayTime;
import dev.jacrispys.JavaBot.api.analytics.objects.TrackStats;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;
import java.util.List;

public interface AudioGuildAnalytics extends GeneralGuildAnalytics {

    long getPlays() throws SQLException;
    long getPauses() throws SQLException;
    PlayTime getTotalPlaytime();
    List<AudioUser> getTopListeners();
    List<TrackStats> getTopSongs();
    <T extends UserSnowflake> AudioUser getAudioUser(T user);
    GuildPlaylist getGuildPlaylists();
    long getPlaylistPlays(GuildPlaylist playlist);
    private long getHijackCount() {
        return 0;
    }
    List<GuildBookmark> getBookmarks();

}
