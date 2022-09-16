package dev.jacrispys.JavaBot.utils.mysql;

import dev.jacrispys.JavaBot.utils.SecretData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MySQLConnection {

    private static MySQLConnection INSTANCE;

    public MySQLConnection() {
        try {
            SecretData.initLoginInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        INSTANCE = this;
    }

    public static MySQLConnection getInstance() {
        return INSTANCE != null ? INSTANCE : new MySQLConnection();
    }

    private final Map<String, Connection> connections = new HashMap<>();

    public Connection getConnection(String dataBase) throws SQLException {
        if (connections.containsKey(dataBase)) {
            return connections.get(dataBase);
        }
        try {
            String userName = "Jacrispys";
            String db_password = SecretData.getDataBasePass();

            String url = "jdbc:mysql://" + SecretData.getDBHost() + ":3306/" + dataBase + "?autoReconnect=true";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            Connection connection = DriverManager.getConnection(url, userName, db_password);
            connections.put(dataBase, connection);
            return connection;


        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Could not connect to the given database!");
        }
    }


    public boolean registerGuild(Guild guild, TextChannel defaultChannel) {
        try {
            Statement statement = getConnection("inside_agent_bot").createStatement();
            String command = "INSERT IGNORE INTO guilds (ID,TicketChannel) VALUES (" + guild.getId() + ", " + defaultChannel.getId() + ");";
            statement.execute("INSERT IGNORE INTO guild_general_stats (ID) VALUE (" + guild.getIdLong() + ")");
            for(Member member : guild.getMembers()) {
                statement.execute("INSERT IGNORE INTO audio_activity (user_ID, guild_ID) VALUES (" + member.getIdLong() + "," + guild.getIdLong() + ")");
            }
            statement.execute(command);
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void executeCommand(String command) {
        try {
            Statement statement = getConnection("inside_agent_bot").createStatement();
            statement.execute(command);
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeUpdate(String command) {
        try {
            Statement statement = getConnection("inside_agent_bot").createStatement();
            statement.executeUpdate(command);
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet queryCommand(String query) throws Exception {
        Statement statement;
        try {
            statement = getConnection("inside_agent_bot").createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery(query);
        } catch (Exception e) {
            throw new Exception("Could not query selected data!");
        }

    }

    public void setMusicChannel(Guild guild, long channelId) throws SQLException {
        Statement statement = getConnection("inside_agent_bot").createStatement();
        statement.executeUpdate("UPDATE guilds SET musicChannel=" + channelId + " WHERE ID=" + guild.getId());
        statement.close();
    }

    public Long getMusicChannel(Guild guild) throws SQLException {
        ResultSet rs = getConnection("inside_agent_bot").createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT musicChannel FROM inside_agent_bot.guilds WHERE ID=" + guild.getId());
        rs.beforeFirst();
        rs.next();
        long channel = rs.getLong("musicChannel");
        rs.close();
        return channel;
    }
}
