package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

/**
 * User object for the api, very similar to {@link User}, but adapted to fit this project's API
 */
public interface JdaUser {

    /**
     * @return guilds that the user is a member of
     */
    List<Guild> getGuilds();

    /**
     * @return instance of an AudioUser from the given guild
     * @param guild to get data from
     */
    AudioUser getAudioUser(Guild guild);
    /**
     * @return instance of a GuildUser from the given guild
     * @param guild to get data from
     */
    GuildUser getGuildUser(Guild guild);

    /**
     * @return JDA User object that correlates with this project's adaptation
     */
    User getUser();
}
