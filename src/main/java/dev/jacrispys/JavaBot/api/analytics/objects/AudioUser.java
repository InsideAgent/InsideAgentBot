package dev.jacrispys.JavaBot.api.analytics.objects;

import java.sql.SQLException;

/**
 * User object to identify API users by
 * <br> extends {@link JdaUser} as a parent
 */
public interface AudioUser extends JdaUser {
    /**
     * gets the {@link AudioActivity} instance for the given user
     * @return instance of {@link AudioActivity}
     * @throws SQLException if a database error occurs
     */
    AudioActivity getAudioActivity() throws SQLException;

}
