package dev.jacrispys.JavaBot.commands;

import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.StatType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UnclassifiedSlashCommands extends ListenerAdapter {

    private MySqlStats sqlStats;

    public UnclassifiedSlashCommands() {
        try {
            this.sqlStats = MySqlStats.getInstance();
        } catch (SQLException ignored) {}
    }

    public void initCommands(List<Guild> guilds) {
        updateJdaCommands();
        guilds.forEach(this::updateGuildCommands);
    }

    public List<CommandData> updateJdaCommands() {
        List<CommandData> commands = new ArrayList<>();
        Collections.addAll(commands,
                Commands.slash("setnick", "Sets the nickname of this bot, or a user.")
                .addOption(OptionType.STRING, "nickname", "The nickname to give the user", true)
                .addOption(OptionType.USER, "target", "User to set nickname.", false),
                Commands.slash("embedbuilder", "builds an embed")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to send the embed to.").addOptions());
        return commands;
    }

    protected void updateGuildCommands(Guild guild) {
    }

    @Override @SuppressWarnings("all")
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // Increment SQL Stat
        sqlStats.incrementGuildStat(event.getGuild().getIdLong(), StatType.COMMAND_COUNTER);
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
            case "embedbuilder" -> {
                event.deferReply(true).queue();
                Channel channel = event.getOption("channel") != null ? event.getOption("channel").getAsChannel() : null;
                if(channel != null && (!(event.getMember().getPermissions(event.getOption("channel").getAsChannel()).contains(Permission.MESSAGE_SEND)))) {
                    event.getHook().editOriginal("You do not have permission to send embeds in this channel!").queue();
                    return;
                }
                UUID id = UUID.randomUUID();
                String buttonId = "builder:" + id;
                EmbedCLI.getInstance().addEmbedCLI((event.getOption("channel") != null ? event.getOption("channel").getAsChannel() : event.getGuildChannel()), id.toString());
                event.getHook().editOriginal("Click Below!").setActionRow(Button.primary(buttonId, "Edit Embed?")).queue();
            }
        }
    }
}
