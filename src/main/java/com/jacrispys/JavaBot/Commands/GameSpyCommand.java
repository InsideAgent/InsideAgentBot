package com.jacrispys.JavaBot.Commands;

import com.jacrispys.JavaBot.Events.GameSpy;
import com.jacrispys.JavaBot.Utils.ChannelStorageManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

public class GameSpyCommand extends ListenerAdapter {

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        if (event.getMessage().getContentRaw().equalsIgnoreCase("!gamespy") || event.getMessage().getContentRaw().equalsIgnoreCase("!gamespydump")) {
            Member sender = event.getGuild().getMember(event.getAuthor());
            for(Role role : sender.getRoles()) {
                String roleName = role.getName();
                if(roleName.equalsIgnoreCase("JacrispysRole") || roleName.equalsIgnoreCase("Spy")) {
                    GameSpy spy = new GameSpy(event.getGuild());
                    if (event.getMessage().getContentRaw().equalsIgnoreCase("!gamespy")) {
                        spy.toggleGameSpy(event);
                        ChannelStorageManager storageManager = new ChannelStorageManager(new Yaml());
                        storageManager.setGuildData(event.getGuild(), "gameSpyChannel", event.getChannel().getId());
                    } else {
                        spy.runSpy(event.getGuild());
                    }
                }
            }
        }
    }
}
