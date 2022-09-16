package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class GuildUserImpl implements GuildUser {
    
    private final User user;
    private final Guild guild;

    // TODO: 9/12/2022 Expand the constructor to include any needed params, user builder? Include more methods to the interface, hook with AudioUser.  
    
    public GuildUserImpl(User user, Guild guild) {
        this.guild = guild;
        this.user = user;
    }
    
    /**
     * @return an instance of the parent user
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * @return an instance of the guild the user is from
     */
    @Override
    public Guild getUserGuild() {
        return guild;
    }

    /**
     * @return an instance of the member the of the user in the guild
     */
    @Override
    public Member getMember() {
        return guild.getMember(user);
    }
}