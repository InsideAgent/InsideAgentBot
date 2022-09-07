package dev.jacrispys.JavaBot.api.analytics.objects;

import dev.jacrispys.JavaBot.Audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.Audio.objects.GuildPlaylist;
import dev.jacrispys.JavaBot.api.analytics.AudioGuildAnalytics;
import dev.jacrispys.JavaBot.api.analytics.GeneralGuildAnalytics;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.List;

public class GuildStats implements Stats, GeneralGuildAnalytics, AudioGuildAnalytics {

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
    public List<TrackStats> getTopSongs() {
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
    public GuildStats getJoinDate() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<Member> getMembers() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<GuildUser> getBotUsers() {
        return null;
    }

    /**
     * @param frequency
     * @return
     */
    @Override
    public List<GuildUser> getRegularUsers(int frequency) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildStats getOverallStats() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public long getTotalUses() {
        return -1;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public GuildUser getGuildUser(User user) {
        return null;
    }

}
