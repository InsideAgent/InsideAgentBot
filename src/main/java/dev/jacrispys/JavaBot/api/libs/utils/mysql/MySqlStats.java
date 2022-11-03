package dev.jacrispys.JavaBot.api.libs.utils.mysql;

import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import dev.jacrispys.JavaBot.utils.mysql.SqlInstanceManager;
import net.dv8tion.jda.api.entities.Member;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

public class MySqlStats {

    private final Connection connection;
    private static MySqlStats instance = null;

    public static MySqlStats getInstance() throws SQLException, ExecutionException, InterruptedException {
        return instance != null ? instance : new MySqlStats();
    }

    protected MySqlStats() throws SQLException, ExecutionException, InterruptedException {
        this.connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
        instance = this;
    }

    public void incrementGuildStat(long guildId, StatType statType) {
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId);
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE guild_general_stats SET " + statType.name().toLowerCase() + "=" + (statValue + 1) + " WHERE ID=" + guildId);
            statement.close();
            incrementJdaStat(statType);

        } catch (SQLException ignored) {
        }
    }

    public void incrementGuildStat(long guildId, long increment, StatType statType) {
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId);
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE guild_general_stats SET " + statType.name().toLowerCase() + "=" + (statValue + increment) + " WHERE ID=" + guildId);
            statement.close();

            incrementJdaStat(increment, statType);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object getGuildStat(long guildId, StatType statType) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId);
        rs.beforeFirst();
        rs.next();
        return rs.getObject(statType.name().toLowerCase(), statType.clazz);
    }

    private void overrideGuildStats(long guildId, long play_counter, long pause_counter, long playtime_millis, long hijack_counter, long command_counter) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("REPLACE INTO guild_general_stats (play_counter, pause_counter, playtime_millis, hijack_counter, command_counter) VALUES " + guildId + play_counter + pause_counter + playtime_millis + hijack_counter + command_counter);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //JDA Stats

    private void incrementJdaStat(StatType statType) {
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM jda_stats");
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE jda_stats SET " + statType.name().toLowerCase() + "=" + (statValue + 1));
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void incrementJdaStat(long increment, StatType statType) {
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM jda_stats");
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(statType.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE jda_stats SET " + statType.name().toLowerCase() + "=" + (statValue + increment));
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object getJdaStat(StatType statType) throws SQLException {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM jda_stats");
        rs.beforeFirst();
        rs.next();
        return rs.getObject(statType.name().toLowerCase(), statType.clazz);
    }

    private void overrideJdaStats(long play_counter, long pause_counter, long playtime_millis, long hijack_counter, long command_counter) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("REPLACE INTO jda_stats (play_counter, pause_counter, playtime_millis, hijack_counter, command_counter) VALUES " + play_counter + pause_counter + playtime_millis + hijack_counter + command_counter);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementUserStat(Member member, UserStats stat) {
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + stat.name().toLowerCase() + " FROM audio_activity WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(stat.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE audio_activity SET " + stat.name().toLowerCase() + "=" + (statValue + 1) + " WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementUserStat(Member member, long increment, UserStats stat) {
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet set = statement.executeQuery("SELECT " + stat.name().toLowerCase() + " FROM audio_activity WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            set.beforeFirst();
            long statValue = set.next() ? set.getLong(stat.name().toLowerCase()) : 0L;
            statement.executeUpdate("UPDATE audio_activity SET " + stat.name().toLowerCase() + "=" + (statValue + increment) + " WHERE guild_id=" + member.getGuild().getIdLong() + " AND user_id=" + member.getIdLong());
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

