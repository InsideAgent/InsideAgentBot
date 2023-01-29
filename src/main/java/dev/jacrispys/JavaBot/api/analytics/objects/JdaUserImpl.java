package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

/**
 * Implementation of the {@link JdaUser} interface
 */
public class JdaUserImpl implements JdaUser {
    private final long userId;
    private final JDA jda;

    public JdaUserImpl(JDA jda, long userId) {
        this.userId = userId;
        this.jda = jda;
    }

    /**
     * @return list of guilds the user is a member of
     */
    @Override
    public List<Guild> getGuilds() {
        return jda.getUserById(userId).getMutualGuilds();
    }

    /**
     * @param guild to reference for AudioUser data
     * @return an instance of the AudioUser
     */
    @Override
    public AudioUser getAudioUser(Guild guild) {
        return new AudioUserImpl(jda, this, guild);
    }

    /**
     * @return instance of a GuildUser from the given guild
     * @param guild to get data from
     */
    @Override
    public GuildUser getGuildUser(Guild guild) {
        return new GuildUserImpl(jda,this, guild.getMember(getUser()), guild);
    }

    /**
     * @return JDA User object that correlates with this project's adaptation
     */
    @Override
    public User getUser() {
        return jda.getUserById(this.userId);
    }
}
