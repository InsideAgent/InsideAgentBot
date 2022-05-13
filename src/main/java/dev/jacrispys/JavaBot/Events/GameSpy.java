package dev.jacrispys.JavaBot.Events;

import dev.jacrispys.JavaBot.JavaBotMain;
import dev.jacrispys.JavaBot.Utils.GameSpyThread;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
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

    public boolean toggleGameSpy(MessageReceivedEvent event) {
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
                return true;
            }
            connection.executeCommand("UPDATE guilds SET GameSpy=" + false + " WHERE ID=" + event.getGuild().getId());
            event.getMessage().reply("GameSpy successfully disabled!").queue(m ->  {
                m.delete().queueAfter(3, TimeUnit.SECONDS);
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            });
            return false;
        } catch(Exception ex) {
            event.getMessage().reply("Could not enable GameSpy! Please check a developer for the issue.").queue(m ->  {
                m.delete().queueAfter(3, TimeUnit.SECONDS);
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            });
            return false;
        }

    }


    public void addSpy() {
        spyThread.addNewSpy(guild);
    }

    public void sendUpdate(Guild guild) {
        spyThread.sendUpdate(guild);
    }




}
