package dev.jacrispys.JavaBot.Events;

import dev.jacrispys.JavaBot.Commands.Audio.SlashMusicCommands;
import dev.jacrispys.JavaBot.Commands.UnclassifiedSlashCommands;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BotStartup extends ListenerAdapter {

    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();

        commands.addAll(new SlashMusicCommands().updateJdaCommands());
        commands.addAll(new UnclassifiedSlashCommands().updateJdaCommands());

        event.getJDA().updateCommands().addCommands(commands).queue();

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
                MySQLConnection connection = MySQLConnection.getInstance();

                // Check Registration
                if (!(connection.queryCommand("SELECT isRegistered FROM guilds WHERE ID=" + guild.getIdLong()).getBoolean("isRegistered"))) {
                    connection.registerGuild(guild, guild.getTextChannels().get(0));
                }
                ResultSet rs = connection.queryCommand("SELECT GameSpy FROM inside_agent_bot.guilds WHERE ID=" + guild.getId());
                rs.beforeFirst();
                rs.next();
                rs.getBoolean("GameSpy");
                if (rs.getBoolean("GameSpy")) {
                    verifyGameSpyData(guild);
                    GameSpy gameSpy = new GameSpy(guild);
                    gameSpy.addSpy();
                }
                rs.close();
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
