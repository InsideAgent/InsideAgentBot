package dev.jacrispys.JavaBot.api.analytics.objects;

import dev.jacrispys.JavaBot.utils.mysql.SqlInstanceManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * API component that allows access to the 'AudioActivity' database table
 *
 * @see AudioActivities
 */
public class AudioActivity {


    private final JdaUser user;

    volatile Connection connection;
    private final static Map<AudioUser, AudioActivity> instances = new HashMap<>();

    private Connection getConnection() throws SQLException {
        if (this.connection == null || !connection.isValid(10)) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                this.connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
            } catch (InterruptedException | ExecutionException | SQLException e) {
                e.printStackTrace();
            }
        }
        return this.connection;
    }

    /**
     * Instance manager for audio activity objects.
     *
     * @param user to obtain data from
     * @return instance of the current class if it exists, or creates a new one.
     * @throws ExecutionException   if an async connection fails
     * @throws InterruptedException if the thread gets blocked
     * @see AudioUser
     */
    public static AudioActivity getAudioActivity(AudioUser user) throws ExecutionException, InterruptedException {
        return instances.getOrDefault(user, null) != null ? instances.get(user) : new AudioActivity(user);
    }

    /**
     * Private constructor for audio activity objects.
     *
     * @param user to obtain data from
     * @throws ExecutionException   if an async connection fails
     * @throws InterruptedException if the thread gets blocked
     * @see AudioUser
     */
    private AudioActivity(AudioUser user) throws ExecutionException, InterruptedException {
        this.user = user;
        instances.put(user, this);
        this.connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
    }

    /**
     * Obtains a statistic from the 'audio_activity' table with {@link AudioActivities} as a query parameter
     *
     * @param guildId  to filter by in the database
     * @param activity - the type of data to be retrieved
     * @return a POJO from the MySQL {@link ResultSet}
     * @throws SQLException if a database exception occurs
     */
    protected Object getGuildStat(long guildId, @NotNull AudioActivities activity) throws SQLException {
        Statement stmt = getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT " + activity.name().toLowerCase() + " FROM audio_activity WHERE guild_id=" + guildId + " AND user_id=" + user.getUser().getIdLong());
        rs.beforeFirst();
        rs.next();
        return rs.getObject(activity.name().toLowerCase());
    }

    /**
     * Increments a given stat in the 'guild_general_stats' database table
     *
     * @param guildId  for correct row to update
     * @param activity - type of activity to increment stat for
     * @see AudioActivity#incrementStat(long, int, AudioActivities)
     */
    public void incrementStat(long guildId, @NotNull AudioActivities activity) {
        try {
            Statement statement = getConnection().createStatement();
            long statValue = statement.executeQuery("SELECT " + activity.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId).getLong(activity.name().toLowerCase());
            statement.executeUpdate("UPDATE guild_general_stats SET " + activity.name().toLowerCase() + "=" + statValue + 1 + " WHERE ID=" + guildId);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increments a given stat in the 'guild_general_stats' database table
     *
     * @param guildId   for correct row to update
     * @param activity  - type of activity to increment stat for
     * @param increment - int value to increase the dataset by
     * @see AudioActivity#incrementStat(long, AudioActivities)
     */
    public void incrementStat(long guildId, int increment, AudioActivities activity) {
        try {
            Statement statement = getConnection().createStatement();
            long statValue = statement.executeQuery("SELECT " + activity.name().toLowerCase() + " FROM guild_general_stats WHERE ID=" + guildId).getLong(activity.name().toLowerCase());
            statement.executeUpdate("UPDATE guild_general_stats SET " + activity.name().toLowerCase() + "=" + statValue + increment + " WHERE ID=" + guildId);
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Specified internal call to {@link AudioActivity#getGuildStat(long, AudioActivities)}
     *
     * @param guildId to update the data in
     * @return number of song queues from given guild
     * @throws SQLException if a database error occurs
     */
    public long getSongQueues(long guildId) throws SQLException {
        return (long) getGuildStat(guildId, AudioActivities.SONG_QUEUES);
    }

    /**
     * Specified internal call to {@link AudioActivity#getGuildStat(long, AudioActivities)}
     *
     * @param guildId to update the data in
     * @return number of playlist queues from given guild
     * @throws SQLException if a database error occurs
     */
    public long getPlaylistQueues(long guildId) throws SQLException {
        return (long) getGuildStat(guildId, AudioActivities.PLAYLIST_QUEUES);
    }

    /**
     * Specified internal call to {@link AudioActivity#getGuildStat(long, AudioActivities)}
     *
     * @param guildId to update the data in
     * @return number of milliseconds listened from given guild
     * @throws SQLException if a database error occurs
     */
    public long getListenTimeMillis(long guildId) throws SQLException {
        return (long) getGuildStat(guildId, AudioActivities.LISTEN_TIME);
    }

    /**
     * Specified internal call to {@link AudioActivity#getGuildStat(long, AudioActivities)}
     *
     * @param guildId to update the data in
     * @return number of times tracks have been skipped by users who didn't queue them
     * @throws SQLException if a database error occurs
     */
    public long getSkipOthers(long guildId) throws SQLException {
        return (long) getGuildStat(guildId, AudioActivities.SKIP_OTHERS);
    }


}

/**
 * Holds the Data types available in the 'audio_activity' table
 *
 * @see AudioActivity
 */
enum AudioActivities {

    SONG_QUEUES,
    PLAYLIST_QUEUES,
    LISTEN_TIME,
    SKIP_OTHERS

}
