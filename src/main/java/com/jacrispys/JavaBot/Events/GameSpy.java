package com.jacrispys.JavaBot.Events;

import com.jacrispys.JavaBot.JavaBotMain;
import com.jacrispys.JavaBot.Utils.GameSpyThread;
import com.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.util.concurrent.*;


public class GameSpy {


    private final Guild guild;


    private final GameSpyThread spyThread = JavaBotMain.getGameSpyThread();

    public GameSpy(Guild guild) {
        this.guild = guild;
    }

    public void toggleGameSpy(MessageReceivedEvent event) {
        try {
            MySQLConnection connection = MySQLConnection.getInstance();

            ResultSet rs = connection.queryCommand("SELECT GameSpy FROM inside_agent_bot.guilds WHERE ID=" + guild.getId());
            rs.beforeFirst();
            rs.next();
            boolean isGameSpy = rs.getBoolean("GameSpy");
            if(!isGameSpy) {
                connection.executeCommand("UPDATE guilds SET GameSpy=" + true + " WHERE ID=" + event.getGuild().getId());
                event.getMessage().reply("GameSpy successfully enabled!").queue(m ->  {
                    m.delete().queueAfter(3, TimeUnit.SECONDS);
                    event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
                });
                return;
            }
            connection.executeCommand("UPDATE guilds SET GameSpy=" + false + " WHERE ID=" + event.getGuild().getId());
            event.getMessage().reply("GameSpy successfully disabled!").queue(m ->  {
                m.delete().queueAfter(3, TimeUnit.SECONDS);
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            });
        } catch(Exception ex) {
            event.getMessage().reply("Could not enable GameSpy! Please check a developer for the issue.").queue(m ->  {
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

    public void sendUpdate(Guild guild) {
        spyThread.sendUpdate(guild);
    }




}
