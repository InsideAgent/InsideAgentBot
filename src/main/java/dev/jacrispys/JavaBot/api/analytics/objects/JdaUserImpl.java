package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class JdaUserImpl implements JdaUser {

    private final long userId;
    private final JDA jda;

    public JdaUserImpl(JDA jda, long userId) {
        this.userId = userId;
        this.jda = jda;
    }

    /**
     * @return
     */
    @Override
    public List<Guild> getGuilds() {
        return jda.getUserById(userId).getMutualGuilds();
    }

    /**
     * @param guild
     * @return
     */
    @Override
    public AudioUser getAudioUser(Guild guild) {
        return new AudioUserImpl(jda, this, guild);
    }

    /**
     * @param guild
     * @return
     */
    @Override
    public GuildUser getGuildUser(Guild guild) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public User getUser() {
        return null;
    }
}
