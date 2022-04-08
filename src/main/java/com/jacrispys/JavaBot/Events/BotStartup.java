package com.jacrispys.JavaBot.Events;

import com.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;

public class BotStartup extends ListenerAdapter {

    public void onReady(@NotNull ReadyEvent event) {
        // Start GameSpy on enabled servers.
        for (Guild guild : event.getJDA().getGuilds()) {
            try {
                MySQLConnection connection = MySQLConnection.getInstance();
                ResultSet rs = connection.queryCommand("SELECT GameSpy FROM inside_agent_bot.guilds WHERE ID=" + guild.getId());
                rs.beforeFirst();
                rs.next();
                rs.getBoolean("GameSpy");
                if (rs.getBoolean("GameSpy")) {
                    verifyGameSpyData(guild);
                    GameSpy gameSpy = new GameSpy(guild);
                    gameSpy.addSpy();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }

    protected  void verifyGameSpyData(Guild guild) {
        MySQLConnection connection = MySQLConnection.getInstance();
        for(Member member : guild.getMembers()) {
            connection.executeCommand("INSERT IGNORE INTO inside_agent_bot.gamespyusers SET Guild=" +
                    guild.getId() + ", MemberId=" + member.getIdLong() + ", totalTime=0");
        }
    }
}
