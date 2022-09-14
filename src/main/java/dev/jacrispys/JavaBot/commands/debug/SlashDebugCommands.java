package dev.jacrispys.JavaBot.commands.debug;

import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import dev.jacrispys.JavaBot.audio.LoadAudioHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlashDebugCommands extends ListenerAdapter {

    // TODO: 9/14/2022 Expand debug commands to fit new API 
    
    public SlashDebugCommands() {
    }

    public void initCommands(List<Guild> guilds) {
        guilds.forEach(this::updateGuildCommands);
    }

    protected void updateGuildCommands(Guild guild) {
        List<CommandData> commands = new ArrayList<>();
        Collections.addAll(commands,
                Commands.slash("debug", "Developer Only Commands.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addSubcommands());
        if (guild.getIdLong() == 786741501014441995L) {
            guild.updateCommands().addCommands(commands).queue();
        }
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        switch (commandName) {

        }
    }

}
