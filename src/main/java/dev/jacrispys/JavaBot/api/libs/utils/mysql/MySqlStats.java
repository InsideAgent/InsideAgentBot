package dev.jacrispys.JavaBot.api.libs.utils.mysql;

import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlStats {

    private final Connection connection;

    protected MySqlStats() throws SQLException {
        this.connection = MySQLConnection.getInstance().getConnection("inside_agent_bot");
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

enum StatType {
    PLAY_COUNTER,
    PAUSE_COUNTER,
    PLAYTIME_MILLIS,
    HIJACK_COUNTER,
    COMMAND_COUNTER
}
