package dev.jacrispys.JavaBot.commands;

import dev.jacrispys.JavaBot.events.GameSpy;
import dev.jacrispys.JavaBot.JavaBotMain;
import dev.jacrispys.JavaBot.utils.GameSpyThread;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

public class GameSpyCommand extends ListenerAdapter {

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        if (event.getMessage().getContentRaw().equalsIgnoreCase("!gamespy") || event.getMessage().getContentRaw().equalsIgnoreCase("!gamespydump")) {
                try {
                    MySQLConnection connection = MySQLConnection.getInstance();
                    ResultSet rs = connection.queryCommand("select * from inside_agent_bot.guilds where ID=" + event.getGuild().getId());
                    rs.beforeFirst();
                    if(!rs.next()) {
                        event.getGuildChannel().sendMessage("Cannot execute commands before guild is indexed! Please use `!registerguild` to index your guild!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                        rs.close();
                        return;
                    }
                    rs.close();
                } catch (Exception ignored) {
                    event.getGuildChannel().sendMessage("Cannot execute commands before guild is indexed! Please use `!registerguild` to index your guild!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }
            Member sender = event.getGuild().getMember(event.getAuthor());
            assert sender != null;
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
                        connection.executeCommand("UPDATE guilds SET GameSpyChannel=" + event.getGuildChannel().getId() + " WHERE ID=" + event.getGuild().getId());
                        break;
                    } else {
                        spy.sendUpdate(event.getGuild());
                    }
                }
            }
        }
    }
}