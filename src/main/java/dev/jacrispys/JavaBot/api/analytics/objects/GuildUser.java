package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public interface GuildUser {

    User getUser();

    Guild getUserGuild();

    Member getMember();
}
