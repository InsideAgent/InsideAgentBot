package com.jacrispys.JavaBot.Events;

import com.jacrispys.JavaBot.JavaBotMain;
import com.jacrispys.JavaBot.Utils.ChannelStorageManager;
import com.jacrispys.JavaBot.Utils.GameSpyThread;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.concurrent.*;


public class GameSpy {


    private final Guild guild;

    private final ChannelStorageManager storageManager = new ChannelStorageManager(new Yaml());

    private final GameSpyThread spyThread = JavaBotMain.getGameSpyThread();

    public GameSpy(Guild guild) {
        this.guild = guild;
    }

    public void toggleGameSpy(MessageReceivedEvent event) {
        try {


            storageManager.getGuildData(guild, "GameSpy");
            if(storageManager.getGuildData(guild, "GameSpy").equals(false)) {
                storageManager.setGuildData(guild, "GameSpy", true);
                event.getMessage().reply("GameSpy successfully enabled!").queue(m ->  {
                    m.delete().queueAfter(3, TimeUnit.SECONDS);
                    event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
                });
                return;
            }
            storageManager.setGuildData(guild, "GameSpy", false);
            event.getMessage().reply("GameSpy successfully disabled!").queue(m ->  {
                m.delete().queueAfter(3, TimeUnit.SECONDS);
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            });
        } catch(NullPointerException ex) {
            storageManager.addGuildData(guild, "GameSpy", true);
            event.getMessage().reply("GameSpy successfully enabled!").queue(m ->  {
                m.delete().queueAfter(3, TimeUnit.SECONDS);
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            });
            System.out.println("added new data!");
        }

    }

    public void addSpy() {
        spyThread.addNewSpy(guild);
        System.out.println("starting spies");
    }

    public void runSpy(Guild guild) {
        spyThread.runSpy(guild);
    }




}
