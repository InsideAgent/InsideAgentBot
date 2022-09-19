package dev.jacrispys.JavaBot.events;

import dev.jacrispys.JavaBot.commands.UnclassifiedSlashCommands;
import dev.jacrispys.JavaBot.commands.audio.SlashMusicCommands;
import dev.jacrispys.JavaBot.commands.debug.SlashDebugCommands;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BotStartup extends ListenerAdapter {

    private final MySQLConnection connection = MySQLConnection.getInstance();

    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();

        commands.addAll(new SlashMusicCommands().updateJdaCommands());
        commands.addAll(new UnclassifiedSlashCommands(event.getJDA()).updateJdaCommands());
        event.getJDA().addEventListener(new SlashDebugCommands(event.getJDA()));

        event.getJDA().updateCommands().addCommands(commands).queue();
        for (Guild guild : event.getJDA().getGuilds()) {
            connection.registerGuild(guild, guild.getTextChannels().get(0));
        }
    }

}
