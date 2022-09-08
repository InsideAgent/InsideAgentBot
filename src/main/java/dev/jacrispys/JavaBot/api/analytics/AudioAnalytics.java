package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.AudioUser;
import dev.jacrispys.JavaBot.api.analytics.objects.PlayTime;
import dev.jacrispys.JavaBot.api.analytics.objects.TrackStats;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;
import java.util.List;

public interface AudioAnalytics {
    long getPlays() throws SQLException;
    long getPauses() throws SQLException;
    long getTotalPlaytime() throws SQLException;
    List<AudioUser> getTopListeners();
    List<TrackStats> getTopSongs();
    <T extends UserSnowflake> AudioUser getAudioUser(T user);
    long getHijackCount() throws SQLException;
}
