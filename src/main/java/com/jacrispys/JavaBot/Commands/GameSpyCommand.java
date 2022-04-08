package com.jacrispys.JavaBot.Commands;

import com.jacrispys.JavaBot.Events.GameSpy;
import com.jacrispys.JavaBot.JavaBotMain;
import com.jacrispys.JavaBot.Utils.GameSpyThread;
import com.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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
                        GameSpyThread spyThread = JavaBotMain.getGameSpyThread();
                        if(spy.toggleGameSpy(event)) {
                            spyThread.addNewSpy(event.getGuild());
                        } else {
                            spyThread.getSpy(event.getGuild()).shutdown();
                        }
                        MySQLConnection connection = MySQLConnection.getInstance();
                        connection.executeCommand("UPDATE guilds SET GameSpyChannel=" + event.getTextChannel().getId() + " WHERE ID=" + event.getGuild().getId());
                    } else {
                        spy.sendUpdate(event.getGuild());
                    }
                }
            }
        }
    }
}
