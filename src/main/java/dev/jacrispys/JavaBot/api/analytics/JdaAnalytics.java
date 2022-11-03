package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.AudioUser;
import dev.jacrispys.JavaBot.api.analytics.objects.TrackStats;
import dev.jacrispys.JavaBot.api.libs.AgentApi;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.StatType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class JdaAnalytics implements AudioAnalytics, GeneralJdaAnalytics {

    public final AgentApi api;
    public final JDA jda;
    public final MySqlStats sqlStats;

    protected JdaAnalytics(AgentApi api, JDA jda) {
        this.api = api;
        this.jda = jda;
        try {
            this.sqlStats = MySqlStats.getInstance();
        } catch (SQLException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the TOTAL amount of songs played (all time)
     * @throws SQLException when the query cannot be completed
     */
    @Override
    public long getPlays() throws SQLException {
        return (long) sqlStats.getJdaStat(StatType.PLAY_COUNTER);
    }

    /**
     * @return the TOTAL amount of times the bot has been paused (all time)
     * @throws SQLException when the query cannot be completed
     */
    @Override
    public long getPauses() throws SQLException {
        return (long) sqlStats.getJdaStat(StatType.PAUSE_COUNTER);
    }

    /**
     * @return the total timme spent listening to the bot (all time)
     * @throws SQLException when the query cannot be completed
     */
    @Override
    public long getTotalPlaytime() throws SQLException {
        return (long) sqlStats.getJdaStat(StatType.PLAYTIME_MILLIS);
    }

    /**
     * @return top users (all time)
     */
    @Override
    public List<AudioUser> getTopListeners() {
        return null;
    }

    /**
     * @return top played songs (all time)
     */
    @Override
    public List<TrackStats> getTopSongs() {
        return null;
    }

    /**
     * @param user instance to get from JDA
     * @param <T>  Allow instantiation from any member/user class
     * @return custom audio user object
     */
    @Override
    public <T extends UserSnowflake> AudioUser getAudioUser(T user) {
        return null;
    }

    /**
     * @return total times the bot has entered hijack mode (all time)
     * @throws SQLException when the query cannot be completed
     */
    @Override
    public long getHijackCount() throws SQLException {
        return (long) sqlStats.getJdaStat(StatType.HIJACK_COUNTER);
    }

    /**
     * @return total number of times the bot has received a command.
     * @throws SQLException when the query cannot be completed
     */
    @Override
    public long getTotalUses() throws SQLException {
        return (long) sqlStats.getJdaStat(StatType.COMMAND_COUNTER);
    }
}
