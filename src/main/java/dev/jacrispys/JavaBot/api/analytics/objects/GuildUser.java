package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Child of {@link JdaUser} that specifies a guild that the User is a member of
 */
public interface GuildUser extends JdaUser {
    /**
     * @return instance of the user's parent (member) guild
     */
    Guild getUserGuild();

    /**
     * @return JDA {@link Member} object from the given guild
     */
    Member getMember();
}
