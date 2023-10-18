package dev.jacrispys.JavaBot.events;

import dev.jacrispys.JavaBot.commands.UnclassifiedSlashCommands;
import dev.jacrispys.JavaBot.commands.audio.SlashMusicCommands;
import dev.jacrispys.JavaBot.commands.debug.SlashDebugCommands;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers guilds into database on bot startup, and guild join events
 */
public class BotStartup extends ListenerAdapter {

    public MySQLConnection getConnection() {
        return MySQLConnection.getInstance();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();

        commands.addAll(SlashMusicCommands.updateJdaCommands());
        commands.addAll(UnclassifiedSlashCommands.updateJdaCommands());
        event.getJDA().addEventListener(new SlashDebugCommands(event.getJDA()));

        event.getJDA().updateCommands().addCommands(commands).queue();
        for (Guild guild : event.getJDA().getGuilds()) {
            getConnection().registerGuild(guild, guild.getTextChannels().get(0));
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        getConnection().registerGuild(event.getGuild(), event.getGuild().getTextChannels().get(0));
    }
}
