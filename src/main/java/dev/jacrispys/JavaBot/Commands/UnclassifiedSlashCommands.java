package dev.jacrispys.JavaBot.Commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UnclassifiedSlashCommands extends ListenerAdapter {

    public UnclassifiedSlashCommands() {

    }

    public void initCommands(List<Guild> guilds) {
        updateJdaCommands();
        guilds.forEach(this::updateGuildCommands);
    }

    public List<CommandData> updateJdaCommands() {
        List<CommandData> commands = new ArrayList<>();
        commands.add(Commands.slash("setnick", "Sets the nickname of this bot, or a user.")
                .addOption(OptionType.STRING, "nickname", "The nickname to give the user", true)
                .addOption(OptionType.USER, "target", "User to set nickname.", false));
        return commands;
    }

    protected void updateGuildCommands(Guild guild) {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        switch (commandName) {
            case "setnick" -> {
                if (!event.getMember().hasPermission(Permission.NICKNAME_MANAGE)) {
                    event.reply("You do not have permission to use this command!").setEphemeral(true).queue();
                    return;
                }
                User target;
                if (event.getOption("target") != null) {
                    target = event.getOption("target").getAsUser();
                } else target = event.getJDA().getSelfUser();
                event.getGuild().getMember(target).modifyNickname(event.getOption("nickname").getAsString()).queue();
                event.reply(target.getAsMention() + " has successfully been nicknamed as: " + event.getOption("nickname").getAsString()).setEphemeral(true).queue();
            }
            default -> {
                if (!event.isAcknowledged())
                    event.reply("Could not find a command registered as: `" + commandName + "`, please report this!").setEphemeral(true).queue();
            }
        }
    }
}
