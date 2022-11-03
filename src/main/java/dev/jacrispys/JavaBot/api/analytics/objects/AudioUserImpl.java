package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class AudioUserImpl extends JdaUserImpl implements AudioUser {
    private final Guild guild;

    public AudioUserImpl(JDA jda, JdaUser user, Guild guild) {
        super(jda, user.getUser().getIdLong());
        this.guild = guild;
    }

    /**
     * @return an audioActivity instance of the given user.
     */
    @Override
    public AudioActivity getAudioActivity() throws SQLException {
        try {
            return AudioActivity.getAudioActivity(this);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param guild
     * @return
     */
    @Override
    public AudioUser getAudioUser(Guild guild) {
        return this;
    }
}
