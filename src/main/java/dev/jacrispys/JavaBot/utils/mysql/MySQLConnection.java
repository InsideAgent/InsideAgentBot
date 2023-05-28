package dev.jacrispys.JavaBot.utils.mysql;

import dev.jacrispys.JavaBot.utils.SecretData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

/**
 * Framework for mysql connection queries and updates
 */
public class MySQLConnection {

    private static MySQLConnection INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(MySQLConnection.class);

    public MySQLConnection() {
        try {
            SecretData.initLoginInfo();
            this.connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        INSTANCE = this;
    }

    public static MySQLConnection getInstance() {
        return INSTANCE != null ? INSTANCE : new MySQLConnection();
    }

    private Connection connection;


    /**
     * @param guild guild to register into the DB
     * @param defaultChannel default notification channel for guild
     * @return boolean if registration was a success
     */
    public boolean registerGuild(Guild guild, TextChannel defaultChannel) {
        try {
            Statement statement = connection.createStatement();
            String command = "INSERT IGNORE INTO guilds (ID,TicketChannel) VALUES (" + guild.getId() + ", " + defaultChannel.getId() + ");";
            statement.execute("INSERT IGNORE INTO guild_general_stats (ID) VALUE (" + guild.getIdLong() + ")");
            for(Member member : guild.getMembers()) {
                statement.execute("INSERT IGNORE INTO audio_activity (user_id, guild_id) VALUES (" + member.getIdLong() + "," + guild.getIdLong() + ")");
            }
            statement.execute(command);
            statement.close();
            logger.info("{} - Registered DB for Guild - " + guild.getName(), MySQLConnection.class.getSimpleName());
            return true;
        } catch (Exception e) {
            logger.error("{} - Failed to register DB for Guild - " + guild.getName(), MySQLConnection.class.getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Safe execution of any command
     * @param command command to execute
     */
    public void executeCommand(String command) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(command);
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Safe execution of update commands
     * @param command command to update DB variables through
     */
    public void executeUpdate(String command) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(command);
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Safe execution of queries
     * @param query DB query to execute
     * @return the ResultSet from the query
     */
    public ResultSet queryCommand(String query) throws Exception {
        Statement statement;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery(query);
        } catch (Exception e) {
            throw new Exception("Could not query selected data!");
        }

    }

    /**
     * Sets the channel for song announcements in a given guild
     */
    public void setMusicChannel(Guild guild, long channelId) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("UPDATE guilds SET musicChannel=" + channelId + " WHERE ID=" + guild.getId());
        statement.close();
    }

    /**
     * Obtains current music channel for a given guild
     */
    public Long getMusicChannel(Guild guild) throws SQLException {
        ResultSet rs = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT musicChannel FROM inside_agent_bot.guilds WHERE ID=" + guild.getId());
        rs.beforeFirst();
        rs.next();
        long channel = rs.getLong("musicChannel");
        rs.close();
        return channel;
    }

}
