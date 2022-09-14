package dev.jacrispys.JavaBot.Events;

import dev.jacrispys.JavaBot.Commands.Audio.SlashMusicCommands;
import dev.jacrispys.JavaBot.Commands.UnclassifiedSlashCommands;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BotStartup extends ListenerAdapter {

    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();

        commands.addAll(new SlashMusicCommands().updateJdaCommands());
        commands.addAll(new UnclassifiedSlashCommands().updateJdaCommands());

        event.getJDA().updateCommands().addCommands(commands).queue();
    }

    @Deprecated(forRemoval = true)
    protected void verifyGameSpyData(Guild guild) {
        MySQLConnection connection = MySQLConnection.getInstance();
        for (Member member : guild.getMembers()) {
            connection.executeCommand("INSERT IGNORE INTO inside_agent_bot.gamespyusers SET Guild=" +
                    guild.getId() + ", MemberId=" + member.getIdLong() + ", totalTime=0");
        }
    }
}
