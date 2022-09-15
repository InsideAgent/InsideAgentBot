package dev.jacrispys.JavaBot.commands.debug;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SlashDebugCommands extends ListenerAdapter {

    // TODO: 9/14/2022 Expand debug commands to fit new API

    private final Map<Long, Pair<UUID, InteractionHook>> trackedGuilds = new HashMap<>();

    public SlashDebugCommands(JDA jda) {
        initCommands(jda);
    }

    private void initCommands(JDA jda) {
        jda.getGuilds().forEach(this::updateGuildCommands);
    }

    protected void updateGuildCommands(Guild guild) {
        List<CommandData> commands = new ArrayList<>();
        List<SubcommandData> subCommands = new ArrayList<>();
        Collections.addAll(subCommands,
                new SubcommandData("latency", "View the bot's current latency.")
                        .addOption(OptionType.STRING, "guildid", "A guild to get the latency of.", false),
                new SubcommandData("active", "List of active AudioPlayers"));
        Collections.addAll(commands,
                Commands.slash("debug", "Developer Only Commands.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addSubcommands(subCommands));
        if (guild.getIdLong() == 786741501014441995L) {
            guild.updateCommands().addCommands(commands).queue();
        }
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getSubcommandName();
        switch (commandName) {
            case "latency" -> {
                event.deferReply(true).queue();
                long guildId = event.getOption("guildid") != null ? event.getOption("guildid").getAsLong() : event.getGuild().getIdLong();
                if (guildId == event.getGuild().getIdLong()) {
                    event.getHook().editOriginal(new MessageBuilder().setEmbeds(latencyEmbed(event.getGuild(), Instant.now().toEpochMilli() - (event.getInteraction().getTimeCreated().toInstant().toEpochMilli()))).build()).queue();
                } else {
                    if (!(event.getJDA().getGuilds().contains(event.getJDA().getGuildById(guildId)))) {
                        event.getHook().editOriginal("I cannot return a request from that guild!").queue();
                        return;
                    }
                    UUID uuid = UUID.randomUUID();
                    Guild guild = event.getJDA().getGuildById(guildId);
                    trackedGuilds.put(guildId, new Pair<>(uuid, event.getHook()));
                    event.getUser().openPrivateChannel().queue(dm -> dm.sendMessage("Click below to obtain UUID for latency verification. (Verify in" + guild.getName() + ") \n || " + uuid + " ||").queue(m -> m.delete().queueAfter(10, TimeUnit.MINUTES)));
                }
            }
            case "active" -> {

            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (trackedGuilds.containsKey(event.getGuild().getIdLong()) && (trackedGuilds.get(event.getGuild().getIdLong()).component1().toString().equals(event.getMessage().getContentRaw()))) {
            trackedGuilds.get(event.getGuild().getIdLong()).component2().editOriginal(new MessageBuilder().setEmbeds(latencyEmbed(event.getGuild(), (Instant.now().toEpochMilli() - (event.getMessage().getTimeCreated().toInstant().toEpochMilli())))).build()).queue();
            event.getMessage().delete().queue();
            trackedGuilds.remove(event.getGuild().getIdLong());
        }
    }

    private MessageEmbed latencyEmbed(Guild guild, long latency) {
        EmbedBuilder latencyEb = new EmbedBuilder();
        latencyEb.setAuthor("Inside Agent Latency - [DEBUG]", null, guild.getIconUrl());
        latencyEb.addField("", "*From guild: " + guild.getName() + " *\n", true);
        latencyEb.addField("Latency: ", "Gateway Latency: `" + guild.getJDA().getGatewayPing() + "`Ms \n" + "Server latency: `" + latency + "`Ms", false);
        latencyEb.setFooter("Negative ping what a joke :(");
        return latencyEb.build();
    }

    private MessageEmbed activePlayers(User requester) {
        EmbedBuilder activeEb = new EmbedBuilder();
        activeEb.setAuthor("Players Currently Active...", null, requester.getAvatarUrl());
        activeEb.addField("", activePlayersList(), false);
    }

    private String activePlayersList() {
        // TODO: 9/14/2022 Setup for max 10 guilds in field 
        List<Guild> activePlayers = new ArrayList<>();
        StringBuilder activeList = new StringBuilder();
        GuildAudioManager.getAudioManagers().forEach((guild, guildAudioManager) -> {
            AudioPlayer audio = GuildAudioManager.getGuildAudioManager(guild).audioPlayer;
            if (audio.getPlayingTrack() != null) {
                activeList.append(guild.getName()).append(" - ").append("[").append(audio.getPlayingTrack().getInfo().author).append(" - ").append(audio.getPlayingTrack().getInfo().title).append("\n");
            }
        });
        return activeList.toString();
    }

}
