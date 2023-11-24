package dev.jacrispys.JavaBot.commands.debug;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import dev.jacrispys.JavaBot.utils.SecretData;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Undocumented, developer diagnostic tools to check for specific statistics.
 */
public class SlashDebugCommands extends ListenerAdapter {
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
                new SubcommandData("active", "List of active AudioPlayers"),
                new SubcommandData("override", "Override yml data in loginInfo.")
                        .addOption(OptionType.STRING, "yml_key", "The YML key to override", true)
                        .addOption(OptionType.STRING, "yml_value", "The value to set as an override", true),
                new SubcommandData("addsuperuser", "Add a new super user.")
                        .addOption(OptionType.USER, "user", "User to be added to super users", true));
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
        if (!event.getName().equals("debug")) return;
        String commandName = event.getSubcommandName();
        if (!SecretData.getSuperUsers().contains(event.getUser().getIdLong())) {
            event.reply("Only super users may use debug commands! Please contact the developer for more information.").setEphemeral(true).queue();
        }
        switch (commandName) {
            case "latency" -> {
                event.deferReply(true).queue();
                long guildId = event.getOption("guildid") != null ? event.getOption("guildid").getAsLong() : event.getGuild().getIdLong();
                if (guildId == event.getGuild().getIdLong()) {
                    event.getHook().editOriginal(new MessageEditBuilder().setEmbeds(latencyEmbed(event.getGuild(), Instant.now().toEpochMilli() - (event.getInteraction().getTimeCreated().toInstant().toEpochMilli()))).build()).queue();
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
            case "active" -> event.reply(new MessageCreateBuilder().setEmbeds(activePlayers(event.getUser())).build()).setEphemeral(true).queue();
            case "override" -> {
                event.deferReply().queue();
                try {
                    String key = event.getOption("yml_key").getAsString();
                    String value = event.getOption("yml_value").getAsString();

                    Object obj = SecretData.getCustomData(key);
                    if (obj == null || key.equals("SUPER_USERS")) {
                        event.getInteraction().getHook().editOriginal("Invalid key or value entered. Please try again.").queue();
                        return;
                    }
                    SecretData.setCustomData(key, value);
                    event.getInteraction().getHook().editOriginalEmbeds(overrideEmbed(key, obj.toString(), value, event.getUser()).build()).queue();
                } catch (NullPointerException ex) {
                    event.getInteraction().getHook().editOriginal("Invalid key or value entered. Please try again.").queue();
                }
            }
            case "addsuperuser" -> {
                User user = event.getOption("user").getAsUser();
                long userId = user.getIdLong();
                List<Long> superUsers = SecretData.getSuperUsers();
                superUsers.add(userId);
                SecretData.setCustomData("SUPER_USERS", superUsers);
                event.replyEmbeds(overrideEmbed("SUPER_USERS", "N/A", "@" + user.getName() + " : " + userId, event.getUser()).build()).queue();
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (trackedGuilds.containsKey(event.getGuild().getIdLong()) && (trackedGuilds.get(event.getGuild().getIdLong()).component1().toString().equals(event.getMessage().getContentRaw()))) {
            trackedGuilds.get(event.getGuild().getIdLong()).component2().editOriginal(new MessageEditBuilder().setEmbeds(latencyEmbed(event.getGuild(), (Instant.now().toEpochMilli() - (event.getMessage().getTimeCreated().toInstant().toEpochMilli())))).build()).queue();
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
        activeEb.setFooter("*" + activePlayerCount() + "/" + requester.getJDA().getGuilds().size() + " AudioPlayers currently active.*");
        return activeEb.build();
    }

    private String activePlayersList() {
        StringBuilder activeList = new StringBuilder();
        GuildAudioManager.getAudioManagers().entrySet().stream().limit(10).forEach(entry -> {
            Guild guild = entry.getKey();
            AudioPlayer audio = entry.getValue().audioPlayer;
            if (audio.getPlayingTrack() != null) {
                activeList.append(guild.getName()).append(" - ").append("[").append(audio.getPlayingTrack().getInfo().author).append(" - ").append(audio.getPlayingTrack().getInfo().title).append("](").append(audio.getPlayingTrack().getInfo().uri).append(")").append("\n");
            }
        });
        return activeList.toString();
    }

    private long activePlayerCount() {
        List<GuildAudioManager> active = new ArrayList<>();

        GuildAudioManager.getAudioManagers().values().forEach(value -> {
            if(value.audioPlayer.getPlayingTrack() != null) {
                active.add(value);
            }
        });
        return active.size();
    }


    private EmbedBuilder overrideEmbed(String key, String oldValue, String newValue, User editor) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("YAML Key override for: \"" + key + "\"");
        eb.addField("Old Value: ", "`" + oldValue + "`\n", false);
        eb.addField("New Value: ", "`" + newValue + "`\n", false);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
        Date currentDate = new Date();
        eb.setFooter("Edited at: " + formatter.format(currentDate));
        eb.setAuthor("Edited by: @" + editor.getName(), null, editor.getEffectiveAvatarUrl());
        eb.setColor(0xed284c);
        return eb;
    }

}
