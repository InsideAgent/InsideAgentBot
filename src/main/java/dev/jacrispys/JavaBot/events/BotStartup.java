package dev.jacrispys.JavaBot.events;

import dev.jacrispys.JavaBot.commands.audio.SlashMusicCommands;
import dev.jacrispys.JavaBot.commands.UnclassifiedSlashCommands;
import dev.jacrispys.JavaBot.commands.debug.SlashDebugCommands;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BotStartup extends ListenerAdapter {

    private final MySQLConnection connection = MySQLConnection.getInstance();

    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();

        commands.addAll(new SlashMusicCommands().updateJdaCommands());
        commands.addAll(new UnclassifiedSlashCommands().updateJdaCommands());

        event.getJDA().updateCommands().addCommands(commands).queue();
        event.getJDA().addEventListener(new SlashDebugCommands(event.getJDA()));



        // Start GameSpy on enabled servers.
        for (Guild guild : event.getJDA().getGuilds()) {
            guild.getAudioManager().closeAudioConnection();
            guild.retrieveWebhooks().queue(webhooks -> webhooks.forEach(webhook -> {
                if (webhook.getOwner() != null && webhook.getOwner().equals(guild.getSelfMember())) {
                    webhook.delete().queue();
                }
            }));
            if (guild.getSelfMember().getVoiceState() != null) {
                AudioManager manager = guild.getAudioManager();
                manager.closeAudioConnection();
            }
            try {

                // Check Registration
                ResultSet set = connection.queryCommand("SELECT isRegistered FROM guilds WHERE ID=" + guild.getIdLong());
                set.beforeFirst();
                set.next();
                if (!(set.getBoolean("isRegistered"))) {
                    connection.registerGuild(guild, guild.getTextChannels().get(0));
                }
            } catch (Exception ignored) {
                return;
            }
        }
    }

    protected void verifyGameSpyData(Guild guild) {
        MySQLConnection connection = MySQLConnection.getInstance();
        for (Member member : guild.getMembers()) {
            connection.executeCommand("INSERT IGNORE INTO inside_agent_bot.gamespyusers SET Guild=" +
                    guild.getId() + ", MemberId=" + member.getIdLong() + ", totalTime=0");
        }
    }

}
