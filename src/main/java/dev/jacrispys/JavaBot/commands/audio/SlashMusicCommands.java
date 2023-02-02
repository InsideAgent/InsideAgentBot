package dev.jacrispys.JavaBot.commands.audio;

import dev.jacrispys.JavaBot.audio.GenerateGenrePlaylist;
import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import dev.jacrispys.JavaBot.audio.LoadAudioHandler;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceCreateEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * New solution to commands, registers slash commands for audio and handles their implementations
 */
public class SlashMusicCommands extends ListenerAdapter {

    public SlashMusicCommands() {

    }

    /**
     * Registers guild specific commands (unused)
     * @param guilds list of guilds to update commands for
     */
    public void initCommands(List<Guild> guilds) {
        updateJdaCommands();
        guilds.forEach(this::updateGuildCommands);
    }

    /**
     * Generates a list of commands to be updated {@link ListenerAdapter#onReady(ReadyEvent)}
     * @return the list of Commands
     */
    public List<CommandData> updateJdaCommands() {
        List<CommandData> commands = new ArrayList<>();
        Collections.addAll(commands,
                Commands.slash("play", "Add a link to most streaming platforms, or use its name to search!")
                        .addOption(OptionType.STRING, "query", "Track to search for.", true)
                        .addOptions(new OptionData(OptionType.STRING, "search", "Method to search for track with.", false)
                                .addChoice("spotify", "spsearch:")
                                .addChoice("apple", "amsearch:")
                                .addChoice("youtube", "ytsearch:")),
                Commands.slash("skip", "Skips the current song!"),
                Commands.slash("volume", "A number 1-500 to adjust volume!")
                        .addOption(OptionType.INTEGER, "volume", " 1-500", true),
                Commands.slash("clear", "Clears the queue."),
                Commands.slash("stop", "Pauses the currently playing audio."),
                Commands.slash("pause", "Pauses the currently playing audio."),
                Commands.slash("resume", "Resumes the currently playing audio."),
                Commands.slash("dc", "Disconnects the bot from its channel!"),
                Commands.slash("leave", "Disconnects the bot from its channel!"),
                Commands.slash("disconnect", "Disconnects the bot from its channel!"),
                Commands.slash("follow", "moves the bot to your current channel!"),
                Commands.slash("queue", "Shows a Embed of songs (10 per page) with page selectors, and a button to remove the message!"),
                Commands.slash("shuffle", "Shuffles the current queue."),
                Commands.slash("song", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("song-info", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("info", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("remove", "Removes a song from the queue at a given index number!")
                        .addOption(OptionType.INTEGER, "index", "Index to remove from queue!", true),
                Commands.slash("seek", "Takes in arg in the form of HH:mm:ss that seeks to that time in the current song!")
                        .addOption(OptionType.INTEGER, "minutes", "number of minutes", true)
                        .addOption(OptionType.INTEGER, "seconds", "number of seconds", true)
                        .addOption(OptionType.INTEGER, "hours", "(Optional) Number of hours", false),
                Commands.slash("fix", "Fixes connectivity by changing regions."),
                Commands.slash("loop", "Enables/Disables loop (skipping resets loops)")
                        .addOptions(new OptionData(OptionType.STRING, "type", "Choose between loop or queue", false)
                                .addChoice("queue", "queue")
                                .addChoice("song", "song")),
                Commands.slash("move", "Swaps positions of two index's in the queue.")
                        .addOption(OptionType.INTEGER, "pos1", "position one to move.", true)
                        .addOption(OptionType.INTEGER, "pos2", "second position to mve.", true),
                Commands.slash("hijack", "Secret Command \uD83E\uDD2BOnly Accessible by certified DJ's!"),
                Commands.slash("playtop", "adds a song to the top of queue")
                        .addOption(OptionType.STRING, "query", "track to add to queue", true)
                        .addOptions(new OptionData(OptionType.STRING, "search", "Method to search for track with.", false)
                                .addChoice("spotify", "spsearch:")
                                .addChoice("apple", "amsearch:")),
                Commands.slash("skipto", "Skips the queue to a given index.")
                        .addOption(OptionType.INTEGER, "index", "Index to skip to.", true),
                Commands.slash("fileplay", "adds a song to the queue")
                        .addOption(OptionType.ATTACHMENT, "file", "track to add to queue", true),
                Commands.slash("radio", "Starts a radio based off picked genre!")
                        .addOption(OptionType.INTEGER, "limit", "The amount of songs that will be generated (max: 500).", true)
                        .addOption(OptionType.INTEGER, "popularity", "Scale from 0 - 100 on the max popularity a song can have", false)
        );
        return commands;
    }

    protected void updateGuildCommands(Guild guild) {
    }

    /**
     * Internal updates to Database
     */
    protected void updateMusicChannel(Guild guild, TextChannel channel) {
        try {
            MySQLConnection.getInstance().setMusicChannel(Objects.requireNonNull(guild), channel.getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private StageInstance stageInstance;

    /**
     * Checks for stage instances being created, to allow bot to join stages and play music
     */
    @Override
    public void onStageInstanceCreate(@NotNull StageInstanceCreateEvent event) {
        GuildAudioManager manager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        manager.stageUpdate(true);
        stageInstance = event.getInstance();
    }

    /**
     * Removes compatibility for stage channels once the instance is deleted
     */
    @Override
    public void onStageInstanceDelete(@NotNull StageInstanceDeleteEvent event) {
        GuildAudioManager manager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        manager.stageUpdate(false);
        stageInstance = null;
    }

    /**
     * Accounts for stages running after reboot
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            for (StageChannel channel : guild.getStageChannels()) {
                if (channel.getStageInstance() != null) {
                    GuildAudioManager manager = GuildAudioManager.getGuildAudioManager(guild);
                    manager.stageUpdate(true);
                    stageInstance = channel.getStageInstance();
                }
            }
        }
    }

    /**
     * Handles implementation for all registered audio slash commands
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        GuildAudioManager audioManager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        LoadAudioHandler audioHandler = new LoadAudioHandler(audioManager);
        if (audioManager.isStageEvent() && stageInstance != null) {
            if ((!stageInstance.getChannel().isModerator(event.getMember())) && (!stageInstance.getSpeakers().contains(event.getMember()))) {
                event.replyEmbeds(audioManager.djEnabledEmbed(event.getJDA())).queue();
                return;
            }
        }
        switch (commandName) {
            case "play", "playtop", "fileplay" -> {
                event.deferReply().setEphemeral(true).queue();
                AudioChannel channel = null;
                assert event.getMember() != null;
                assert event.getMember().getVoiceState() != null;
                assert event.getMember().getVoiceState().getChannel() != null;
                channel = event.getMember().getVoiceState().getChannel();
                updateMusicChannel(event.getGuild(), event.getGuildChannel().asTextChannel());

                String track = null;
                if (!(event.getName().equalsIgnoreCase("fileplay") || event.getName().equalsIgnoreCase("fp"))) {
                    track = Objects.requireNonNull(event.getOption("query")).getAsString();
                    if (channel == null) {
                        event.getHook().editOriginal("Could not load song, as you are not in a voice channel!").queue();
                        return;
                    }
                    try {
                        new URL(track);
                    } catch (MalformedURLException ignored) {
                        String searchMethod = "ytsearch";
                        if (event.getOption("search") != null) {
                            searchMethod = Objects.requireNonNull(event.getOption("search")).getAsString();
                        }
                        switch (searchMethod.toLowerCase()) {
                            case ("spsearch:") -> searchMethod = "spsearch:";
                            case ("amsearch:") -> searchMethod = "amsearch:";
                            default -> searchMethod = "ytsearch:";
                        }
                        track = searchMethod + track;
                    }
                }
                if (event.getName().equalsIgnoreCase("fileplay")) {
                    track = Objects.requireNonNull(event.getOption("file")).getAsAttachment().getUrl();
                }
                boolean playTop = (commandName.equalsIgnoreCase("playtop"));
                event.getHook().editOriginal((MessageEditData) audioHandler.loadAndPlay(track, channel, event.getMember(), playTop, true)).queue();
            }
            case "skip" ->
                    event.reply((MessageCreateData) audioHandler.skipTrack(audioManager, event.getMember())).queue();
            case "volume" ->
                    event.reply((MessageCreateData) audioManager.setVolume(Objects.requireNonNull(event.getOption("volume")).getAsInt())).queue();
            case "clear" -> event.reply((MessageCreateData) audioManager.clearQueue()).queue();
            case "stop", "pause" -> event.reply((MessageCreateData) audioManager.pausePlayer()).queue();
            case "resume" -> event.reply((MessageCreateData) audioManager.resumePlayer()).queue();
            case "dc", "leave", "disconnect" -> event.reply((MessageCreateData) audioManager.disconnectBot(event.getMember())).queue();
            case "follow" ->
                    event.reply((MessageCreateData) audioManager.followUser(Objects.requireNonNull(event.getMember()))).setEphemeral(true).queue();
            case "queue" -> event.reply((MessageCreateData) audioManager.displayQueue()).queue();
            case "shuffle" -> event.reply((MessageCreateData) audioManager.shufflePlayer()).queue();
            case "song", "song-info", "info" ->
                    event.reply((MessageCreateData) audioManager.sendTrackInfo()).setEphemeral(true).queue();
            case "remove" ->
                    event.reply((MessageCreateData) audioManager.removeTrack(Objects.requireNonNull(event.getOption("index")).getAsInt())).queue();
            case "seek" -> {
                StringBuilder stringBuilder = new StringBuilder();
                if (event.getOption("hours") != null) {
                    if (Objects.requireNonNull(event.getOption("hours")).getAsInt() < 10) stringBuilder.append("0");
                    stringBuilder.append(Objects.requireNonNull(event.getOption("hours")).getAsInt()).append(":");
                }
                if (Objects.requireNonNull(event.getOption("minutes")).getAsInt() < 10) stringBuilder.append("0");
                stringBuilder.append(Objects.requireNonNull(event.getOption("minutes")).getAsInt()).append(":");
                if (Objects.requireNonNull(event.getOption("seconds")).getAsInt() < 10) stringBuilder.append("0");
                stringBuilder.append(Objects.requireNonNull(event.getOption("seconds")).getAsInt());
                event.reply((MessageCreateData) audioManager.seekTrack(stringBuilder.toString())).queue();
            }
            case "fix" ->
                    event.reply((MessageCreateData) audioManager.fixAudio(Objects.requireNonNull(event.getMember()))).setEphemeral(true).queue();
            case "loop" -> {
                MessageData message;
                if (event.getOption("type") != null) {
                    message = Objects.requireNonNull(event.getOption("type")).getAsString().equalsIgnoreCase("queue") ? audioManager.loopQueue() : audioManager.loopSong();
                } else message = audioManager.loopQueue();
                event.reply((MessageCreateData) message).queue();
            }
            case "move" ->
                    event.reply((MessageCreateData) audioManager.moveSong(Objects.requireNonNull(event.getOption("pos1")).getAsInt(), Objects.requireNonNull(event.getOption("pos2")).getAsInt())).queue();
            case "hijack" ->
                    event.reply((MessageCreateData) audioManager.enableDJ(event.getUser(), event.getGuild())).queue();
            case "skipto" ->
                    event.reply((MessageCreateData) audioManager.skipTo(Objects.requireNonNull(event.getOption("index")).getAsInt())).queue();
            case "radio" -> {
                event.deferReply().setEphemeral(false).queue();
                if (GenerateGenrePlaylist.reactMessage.containsKey(event.getUser())) {
                    event.getHook().editOriginal("Cannot use this command until current request has been fulfilled!").queue();
                    return;
                }
                if (event.getOption("limit").getAsInt() > 100) {
                    event.getHook().editOriginal("Cannot add more than 100 songs to radio!").queue();
                    return;
                }
                GenerateGenrePlaylist.limit.put(event.getUser(), event.getOption("limit").getAsInt());
                if (event.getOption("popularity") != null) {
                    GenerateGenrePlaylist.popularity.put(event.getUser(), event.getOption("popularity").getAsInt());
                }
                event.getHook().editOriginal((MessageEditData) audioManager.genreList(event.getUser().getIdLong())).queue();
                event.getHook().retrieveOriginal().queue(message -> {
                    String[] ones = {"zero", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"};
                    for (int i = 0; i < 10; i++) {
                        message.addReaction(Emoji.fromUnicode(ones[i + 1])).queue();
                    }
                    GenerateGenrePlaylist.reactMessage.put(event.getUser(), message.getIdLong());
                });
            }
        }
    }
}
