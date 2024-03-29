package dev.jacrispys.JavaBot.api.libs.utils.mysql;

import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import dev.jacrispys.JavaBot.utils.mysql.SqlInstanceManager;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

/**
 * Database handler for all Stats
 */
public class MySqlStats {

    private Connection connection;
    private static MySqlStats instance = null;

    private final Logger logger = LoggerFactory.getLogger(MySqlStats.class);

    /**
     *
     * @return instance of the current class
     * @throws SQLException if a database error occurs
     * @throws ExecutionException if the Async database connection creation fails
     * @throws InterruptedException if there is a thread fault while obtaining a connection
     */
    public static MySqlStats getInstance() {
        return instance != null ? instance : new MySqlStats();
    }

    protected MySqlStats() {
        try {
            connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("{} - Error creating a SQL stat instance!", getClass().getSimpleName());
            throw new RuntimeException(e);
        }
        instance = this;
    }

    public void obtainConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() throws SQLException {
        if (this.connection == null || !connection.isValid(10)) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                this.connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
            } catch (InterruptedException | ExecutionException | SQLException e) {
                logger.error("{} - Error obtaining SQL connection! \n" + e.getMessage(), getClass().getSimpleName());
            }
        }
        return this.connection;
    }

    /**
     * Increment's the given stat for the given guild
     * @param guildId guild to increment stat for
     * @param statType type of stat to increment
     */
    public void incrementGuildStat(long guildId, StatType statType) {
        try {
            Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId);
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE guild_general_stats SET " + statType.name().toLowerCase() + "=" + (statValue + 1) + " WHERE ID=" + guildId);
            statement.close();
            incrementJdaStat(statType);

        } catch (SQLException ignored) {
            logger.warn("{} - SQL error while updating stat: " + statType.name() + " in guild: " + guildId, getClass().getSimpleName());
        }
    }

    /**
     * Increment's the given stat for the given guild
     * @param guildId guild to increment stat for
     * @param increment amount to increment the stat
     * @param statType type of stat to increment
     */
    public void incrementGuildStat(long guildId, long increment, StatType statType) {
        try {
            Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId);
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE guild_general_stats SET " + statType.name().toLowerCase() + "=" + (statValue + increment) + " WHERE ID=" + guildId);
            statement.close();

            incrementJdaStat(increment, statType);

        } catch (SQLException ignored) {
            logger.warn("{} - SQL error while updating stat: " + statType.name() + " in guild: " + guildId, getClass().getSimpleName());

        }
    }

    /**
     * Obtains the given stat for the given guild
     * @param guildId guild to obtain stat for
     * @param statType type of stat to obtain
     * @throws SQLException if a database error occurs
     */
    public Object getGuildStat(long guildId, StatType statType) throws SQLException {
        Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId);
        rs.beforeFirst();
        rs.next();
        return rs.getObject(statType.name().toLowerCase(), statType.clazz);
    }

    /**
     * Should remain unused, unless there is an emergency.
     * Overrides all stats tracked for a specific guild
     * @param guildId guild whose stats to override
     * @param play_counter statistic to override
     * @param pause_counter statistic to override
     * @param playtime_millis statistic to override
     * @param hijack_counter statistic to override
     * @param command_counter statistic to override
     */
    private void overrideGuildStats(long guildId, long play_counter, long pause_counter, long playtime_millis, long hijack_counter, long command_counter) {
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate("REPLACE INTO guild_general_stats (play_counter, pause_counter, playtime_millis, hijack_counter, command_counter) VALUES " + guildId + play_counter + pause_counter + playtime_millis + hijack_counter + command_counter);
            statement.close();

        } catch (SQLException e) {
            logger.warn("{} - SQL error while overriding guild stats in guild: " + guildId, getClass().getSimpleName());
        }
    }

    //JDA Stats

    /**
     * Increment's the given stat globally
     * @param statType type of stat to increment
     */
    private void incrementJdaStat(StatType statType) {
        try {
            Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM jda_stats");
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE jda_stats SET " + statType.name().toLowerCase() + "=" + (statValue + 1));
            statement.close();

        } catch (SQLException e) {
            logger.warn("{} - SQL error while updating JDA stat: " + statType.name(), getClass().getSimpleName());
        }
    }

    /**
     * Increment's the given stat globally
     * @param increment amount to increment the stat by
     * @param statType type of stat to increment
     */
    private void incrementJdaStat(long increment, StatType statType) {
        try {
            Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM jda_stats");
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE jda_stats SET " + statType.name().toLowerCase() + "=" + (statValue + increment));
            statement.close();

        } catch (SQLException e) {
            logger.warn("{} - SQL error while updating JDA stat: " + statType.name(), getClass().getSimpleName());
        }
    }

    /**
     * Obtains the given stat in a global context
     * @param statType type of stat to obtain
     * @throws SQLException if a database error occurs
     */
    public Object getJdaStat(StatType statType) throws SQLException {
        Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM jda_stats");
        rs.beforeFirst();
        rs.next();
        return rs.getObject(statType.name().toLowerCase(), statType.clazz);
    }

    /**
     * Should remain unused, unless there is an emergency.
     * Overrides all stats tracked GLOBALLY
     * @param play_counter statistic to override
     * @param pause_counter statistic to override
     * @param playtime_millis statistic to override
     * @param hijack_counter statistic to override
     * @param command_counter statistic to override
     */
    private void overrideJdaStats(long play_counter, long pause_counter, long playtime_millis, long hijack_counter, long command_counter) {
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate("REPLACE INTO jda_stats (play_counter, pause_counter, playtime_millis, hijack_counter, command_counter) VALUES " + play_counter + pause_counter + playtime_millis + hijack_counter + command_counter);
            statement.close();

        } catch (SQLException e) {
            logger.warn("{} - SQL error while overriding JDA stats.", getClass().getSimpleName());
        }
    }

    /**
     * Increments a stat for a specific user
     * @param member to increment stat for
     * @param stat stat type to increment
     */
    public void incrementUserStat(Member member, UserStats stat) {
        try {
            Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + stat.name().toLowerCase() + " FROM audio_activity WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(stat.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE audio_activity SET " + stat.name().toLowerCase() + "=" + (statValue + 1) + " WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            statement.close();
            set.close();

        } catch (SQLException e) {
            logger.warn("{} - SQL error while updating stat: " + stat.name() + " for user: " + member.getUser().getName() + " in guild: " + member.getGuild().getIdLong(), getClass().getSimpleName());
        }
    }

    /**
     * Increments a stat for a specific user
     * @param member to increment stat for
     * @param increment amount to increment the stat by
     * @param stat stat type to increment
     */
    public void incrementUserStat(Member member, long increment, UserStats stat) {
        try {
            Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + stat.name().toLowerCase() + " FROM audio_activity WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(stat.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE audio_activity SET " + stat.name().toLowerCase() + "=" + (statValue + increment) + " WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            statement.close();

        } catch (SQLException e) {
            logger.warn("{} - SQL error while updating stat: " + stat.name() + " for user: " + member.getUser().getName() + " in guild: " + member.getGuild().getIdLong(), getClass().getSimpleName());
        }
    }

}

