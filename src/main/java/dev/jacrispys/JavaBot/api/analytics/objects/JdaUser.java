package dev.jacrispys.JavaBot.api.analytics.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public interface JdaUser {

    List<Guild> getGuilds();
    AudioUser getAudioUser(Guild guild);
    GuildUser getGuildUser(Guild guild);
    User getUser();
}
