package com.jacrispys.JavaBot.Utils.MySQL;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.*;

public class MySQLConnection {

    private Connection connection;
    private static MySQLConnection INSTANCE;

    public MySQLConnection() {
        INSTANCE = this;
    }

    public static MySQLConnection getInstance() {return INSTANCE;}

    public Connection getConnection(String dataBase) throws Exception {
        try {
            String userName = "Jacrispys";
            String db_password = System.getenv("DATA_BASE_PASS");

            String url = "jdbc:mysql://localhost:3306/" + dataBase;
            Class.forName("com.mysql.cj.jdbc.Driver");
            if(this.connection == null) {
                Connection connection = DriverManager.getConnection(url, userName, db_password);
                this.connection = connection;
                return connection;
            }
            return this.connection;


        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Could not connect to the given database!");
        }
    }

    public long getGameSpyChannel(Guild guild) throws Exception {
        try {
            long channelId;
            Statement statement = getConnection("inside_agent_bot").createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String command = "SELECT GameSpyChannel FROM inside_agent_bot.guilds WHERE ID=" + guild.getId();
            ResultSet rs = statement.executeQuery(command);
            rs.beforeFirst();
            rs.next();
            channelId = rs.getLong("GameSpyChannel");
            return channelId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Could not locate gameSpyChannel for given Guild!");
        }
    }

    public boolean registerGuild(Guild guild, TextChannel defaultChannel) {
        try {
            Statement statement = getConnection("inside_agent_bot").createStatement();
            String command = "INSERT INTO guilds (ID,GameSpy,TicketChannel,GameSpyChannel) VALUES (" + guild.getId() + ", 0, null, " + defaultChannel.getId() + ");";
            statement.execute(command);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean executeCommand(String command) {
        try {
            Statement statement = getConnection("inside_agent_bot").createStatement();
            return statement.execute(command);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet queryCommand(String query) throws Exception {
        Statement statement = null;
        try {
            statement = getConnection("inside_agent_bot").createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery(query);
        } catch (Exception e) {
            throw new Exception("Could not query selected data!");
        }

    }

}
