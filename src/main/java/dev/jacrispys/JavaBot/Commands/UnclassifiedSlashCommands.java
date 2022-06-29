package dev.jacrispys.JavaBot.Commands;

import dev.jacrispys.JavaBot.Audio.GuildAudioManager;
import dev.jacrispys.JavaBot.Audio.LoadAudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnclassifiedSlashCommands extends ListenerAdapter {

    public UnclassifiedSlashCommands() {

    }

    public void initCommands(JDA jda, List<Guild> guilds) {
        updateJdaCommands(jda);
        guilds.forEach(this::updateGuildCommands);
    }

    protected void updateJdaCommands(JDA jda) {
        jda.updateCommands()
                .addCommands(
                        Commands.slash("nick", "Sets the nickname of this bot, or a user.")
                                .addOption(OptionType.STRING, "nickname", "The nickname to give the user", true)
                                .addOption(OptionType.USER, "target", "User to set nickname.")
                ).queue();
    }

    protected void updateGuildCommands(Guild guild) {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        switch (commandName) {
            case "nick" -> {
                User target;
                if (event.getOption("target") != null) {
                    target = event.getOption("target").getAsUser();
                } else target = event.getJDA().getSelfUser();
                event.getGuild().getMember(target).modifyNickname(event.getOption("nickname").getAsString()).queue();
                event.reply(target.getAsMention() + " has successfully been nicknamed as: " + event.getOption("nickname").getAsString()).setEphemeral(true).queue();
            }
            default -> event.reply("Unknown command! Please try again.").queue();
        }
    }
}
