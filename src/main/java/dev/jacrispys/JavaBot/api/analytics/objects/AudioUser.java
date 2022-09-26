package dev.jacrispys.JavaBot.api.analytics.objects;

import java.sql.SQLException;

public interface AudioUser extends JdaUser {
    AudioActivity getAudioActivity() throws SQLException;

}
