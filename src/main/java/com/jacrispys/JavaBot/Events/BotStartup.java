package com.jacrispys.JavaBot.Events;

import com.jacrispys.JavaBot.Utils.ChannelStorageManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

public class BotStartup extends ListenerAdapter {

    public void onReady(@NotNull ReadyEvent event) {
        // Start GameSpy on enabled servers.
        for(Guild guild : event.getJDA().getGuilds()) {
            try {
                ChannelStorageManager storageManager = new ChannelStorageManager(new Yaml());
                if(storageManager.getGuildData(guild, "GameSpy") instanceof Boolean) {
                    boolean isGameSpyEnabled = (boolean) storageManager.getGuildData(guild, "GameSpy");
                    if(isGameSpyEnabled) {
                        GameSpy gameSpy = new GameSpy(guild);
                        gameSpy.addSpy();
                    }
                }
                System.out.println("ready");
            } catch(Exception ex) {
                return;
            }
        }
    }
}
