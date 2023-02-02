package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * Implementation for the {@link AudioUser} interface
 * <br> Extends {@link JdaUserImpl} as a parent to both {@link AudioUser} and {@link AudioUserImpl}
 */
public class AudioUserImpl extends JdaUserImpl implements AudioUser {
    private final Guild guild;

    /**
     *
     * @param jda instance to access the API through
     * @param user to get AudioUser data for
     * @param guild to find tracked data for
     */
    public AudioUserImpl(JDA jda, JdaUser user, Guild guild) {
        super(jda, user.getUser().getIdLong());
        this.guild = guild;
    }

    /**
     * @return an audioActivity instance of the given user.
     */
    @Override
    public AudioActivity getAudioActivity() {
        try {
            return AudioActivity.getAudioActivity(this);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param guild to reference for AudioUser data
     * @return an instance of the AudioUser
     */
    @Override
    public AudioUser getAudioUser(Guild guild) {
        return this;
    }
}
