package dev.jacrispys.JavaBot.api.analytics.objects;

import dev.jacrispys.JavaBot.audio.objects.GuildBookmark;
import dev.jacrispys.JavaBot.audio.objects.GuildPlaylist;
import dev.jacrispys.JavaBot.api.analytics.AudioGuildAnalytics;
import dev.jacrispys.JavaBot.api.analytics.GeneralGuildAnalytics;
import dev.jacrispys.JavaBot.api.libs.AgentApi;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.StatType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public abstract class GuildStats implements Stats, GeneralGuildAnalytics, AudioGuildAnalytics {

    public final long guildId;
    public final AgentApi api;
    public final JDA jda;
    public final MySqlStats sqlStats;

    protected GuildStats(long guildId, AgentApi api, JDA jda) {
        this.guildId = guildId;
        this.api = api;
        this.jda = jda;
        try {
            this.sqlStats = MySqlStats.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the total number of songs played in a guild.
     */
    @Override
    public long getPlays() throws SQLException {
        return (long) sqlStats.getGuildStat(guildId, StatType.PLAY_COUNTER);
    }

    /**
     * @return the amount of times the bot has been paused in a guild.
     */
    @Override
    public long getPauses() throws SQLException {
        return (long) sqlStats.getGuildStat(guildId, StatType.PAUSE_COUNTER);
    }

    /**
     * @return the amount of time the bot has played songs in a guild.
     */
    @Override
    public long getTotalPlaytime() throws SQLException {
        return (long) sqlStats.getGuildStat(guildId, StatType.PLAYTIME_MILLIS);
    }

    /**
     * @return a list of the most active music listeners
     */
    // TODO: 9/7/2022 Decide whether activity is based off songs queued or  duration in VC with music playing
    @Override
    public List<AudioUser> getTopListeners() {
        return null;
    }

    /**
     * @return a list of tracks that have been played the most in a guild
     */
    // TODO: 9/7/2022 create database table for tracks played in guild & overall JDA, pull method to JDA stats.
    @Override
    public List<TrackStats> getTopSongs() {
        return null;
    }

    /**
     * @param user is the JDA instance of the user
     * @param <T>  generic to allow members and users
     * @return custom object for {@link AudioUser}
     */
    @Override
    public <T extends UserSnowflake> AudioUser getAudioUser(T user) {
        return null;
    }

    /**
     * @return the amount of times this guild has been hijacked
     */
    @Override
    public long getHijackCount() throws SQLException {
        return (long) sqlStats.getGuildStat(guildId, StatType.HIJACK_COUNTER);
    }

    /**
     * @return custom {@link GuildPlaylist} object
     */
    @Override
    public GuildPlaylist getGuildPlaylists() {
        return null;
    }

    /**
     * @param playlist to retrieve plays from
     * @return a value from the DB of how many plays a guild playlist has
     */
    @Override
    public long getPlaylistPlays(GuildPlaylist playlist) {
        return 0;
    }

    /**
     * @return a list of {@link GuildBookmark}
     */
    @Override
    public List<GuildBookmark> getBookmarks() {
        return null;
    }

    /**
     * @return the JDA date/time the bot joined a given guild.
     */
    @Override
    public OffsetDateTime getJoinDate() {
        return Objects.requireNonNull(jda.getGuildById(guildId)).getSelfMember().getTimeJoined();
    }

    /**
     * @return a list of {@link GuildUser} that are bot accounts
     */
    @Override
    public List<GuildUser> getBotUsers() {
        return null;
    }

    /**
     * @param frequency is a custom scalar for how often users access the bot (scale of 1-100)
     * @return a list of users above the {@code frequency} requested.
     */
    // TODO: 9/7/2022 Create scaling for frequency
    @Override
    public List<GuildUser> getRegularUsers(int frequency) {
        return null;
    }

    /**
     * @return instance of {@link GuildStats}
     */
    @Override
    public GuildStats getOverallStats() {
        return this;
    }

    /**
     * @return total times any command has been used
     */
    @Override
    public long getTotalUses() throws SQLException {
        return (long) sqlStats.getGuildStat(guildId, StatType.COMMAND_COUNTER);
    }

    /**
     * @param user to find the {@link GuildUser} for.
     * @return a {@link GuildUser} instance of the JDA {@link User}
     */
    @Override
    public GuildUser getGuildUser(User user) {
        return null;
    }

}
