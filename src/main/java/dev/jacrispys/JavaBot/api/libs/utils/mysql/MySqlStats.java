package dev.jacrispys.JavaBot.api.libs.utils.mysql;

import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlStats {

    private final Connection connection;
    private static MySqlStats instance = null;

    public static MySqlStats getInstance() throws SQLException {
        return instance != null ? instance : new MySqlStats();
    }

    protected MySqlStats() throws SQLException {
        this.connection = MySQLConnection.getInstance().getConnection("inside_agent_bot");
        instance = this;
    }

    public void incrementStat(long guildId, StatType statType) {
        try {
            Statement statement = connection.createStatement();
            long statValue = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId).getLong(statType.name().toLowerCase());
            statement.executeUpdate("UPDATE guild_general_stats SET " + statType.name().toLowerCase() + "=" + statValue + 1 + " WHERE ID=" + guildId);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementStat(long guildId, int increment, StatType statType) {
        try {
            Statement statement = connection.createStatement();
            long statValue = statement.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId).getLong(statType.name().toLowerCase());
            statement.executeUpdate("UPDATE guild_general_stats SET " + statType.name().toLowerCase() + "=" + statValue + increment + " WHERE ID=" + guildId);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object getGuildStat(long guildId, StatType statType) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT " + statType.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId);
        rs.beforeFirst();
        rs.next();
        return rs.getObject(statType.name().toLowerCase(), statType.clazz);
    }

    public void overrideGuildStat(long guildId, long play_counter, long pause_counter, long playtime_millis, long hijack_counter, long command_counter) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("REPLACE INTO guild_general_stats (play_counter, pause_counter, playtime_millis, hijack_counter, command_counter) VALUES " + guildId + play_counter + pause_counter + playtime_millis + hijack_counter + command_counter);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

