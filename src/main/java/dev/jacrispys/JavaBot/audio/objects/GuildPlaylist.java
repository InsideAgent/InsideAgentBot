package dev.jacrispys.JavaBot.audio.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public interface GuildPlaylist {

    Guild getGuild();
    String getPlaylist();
    User getOwner();
}
