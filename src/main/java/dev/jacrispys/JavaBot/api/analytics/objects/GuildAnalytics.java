package dev.jacrispys.JavaBot.api.analytics.objects;

import dev.jacrispys.JavaBot.Audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.Audio.objects.GuildPlaylist;
import dev.jacrispys.JavaBot.api.analytics.AudioGuildAnalytics;
import dev.jacrispys.JavaBot.api.analytics.GeneralGuildAnalytics;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.List;

public class GuildAnalytics implements Analytics, GeneralGuildAnalytics, AudioGuildAnalytics {

    /**
     * @return
     */
    @Override
    public long getPlays() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public long getPauses() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public PlayTime getTotalPlaytime() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<AudioUser> getTopListeners() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<TrackAnalytics> getTopSongs() {
        return null;
    }

    /**
     * @param user
     * @param <T>
     * @return
     */
    @Override
    public <T extends UserSnowflake> AudioUser getAudioUser(T user) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildPlaylist getGuildPlaylists() {
        return null;
    }

    /**
     * @param playlist
     * @return
     */
    @Override
    public long getPlaylistPlays(GuildPlaylist playlist) {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public List<GuildBookmark> getBookmarks() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildAnalytics getJoinDate() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildAnalytics getMembers() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildAnalytics getBotUsers() {
        return null;
    }

    /**
     * @param frequency
     * @return
     */
    @Override
    public GuildAnalytics getRegularUsers(int frequency) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildAnalytics getOverallStats() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildAnalytics getTotalUses() {
        return null;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public GuildAnalytics getUser(GuildUser user) {
        return null;
    }
}
