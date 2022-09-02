package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.Audio.objects.GuildPlaylist;
import dev.jacrispys.JavaBot.Audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.api.analytics.objects.AudioUser;
import dev.jacrispys.JavaBot.api.analytics.objects.PlayTime;
import dev.jacrispys.JavaBot.api.analytics.objects.TrackAnalytics;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.List;

public interface AudioGuildAnalytics extends GeneralGuildAnalytics {

    long getPlays();
    long getPauses();
    PlayTime getTotalPlaytime();
    List<AudioUser> getTopListeners();
    List<TrackAnalytics> getTopSongs();
    <T extends UserSnowflake> AudioUser getAudioUser(T user);
    GuildPlaylist getGuildPlaylists();
    long getPlaylistPlays(GuildPlaylist playlist);

    private long getHijackCount() {
        return 0;
    }

    List<GuildBookmark> getBookmarks();

}
