package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;

public class AudioUserImpl  extends GuildUserImpl implements AudioUser{
    public AudioUserImpl(User user, Guild guild) {
        super(user, guild);
    }

    /**
     * @return an audioActivity instance of the given user.
     */
    @Override
    public AudioActivity getAudioActivity() throws SQLException {
        return AudioActivity.getAudioActivity(this);
    }
}
