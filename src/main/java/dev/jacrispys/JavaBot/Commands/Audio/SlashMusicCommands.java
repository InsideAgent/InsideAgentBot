package dev.jacrispys.JavaBot.Commands.Audio;

import dev.jacrispys.JavaBot.Audio.GuildAudioManager;
import dev.jacrispys.JavaBot.Audio.LoadAudioHandler;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SlashMusicCommands extends ListenerAdapter {

    public SlashMusicCommands() {

    }

    public void initCommands(List<Guild> guilds) {
        updateJdaCommands();
        guilds.forEach(this::updateGuildCommands);
    }

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
                        .addOption(OptionType.ATTACHMENT, "file", "track to add to queue", true)
        );
        return commands;
    }

    protected void updateGuildCommands(Guild guild) {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        GuildAudioManager audioManager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        LoadAudioHandler audioHandler = new LoadAudioHandler(audioManager);
        switch (commandName) {
            case "play", "playtop", "fileplay" -> {
                    event.deferReply().setEphemeral(true).queue();
                    VoiceChannel channel;
                    assert event.getMember() != null;
                    assert event.getMember().getVoiceState() != null;
                    assert event.getMember().getVoiceState().getChannel() != null;
                    channel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

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
                    event.getHook().editOriginal(Objects.requireNonNull(audioHandler.loadAndPlay(track, channel, event.getUser(), playTop))).queue();
                try {
                    MySQLConnection.getInstance().setMusicChannel(Objects.requireNonNull(event.getGuild()), event.getTextChannel().getIdLong());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            case "skip" -> event.reply(audioHandler.skipTrack(audioManager)).queue();
            case "volume" -> event.reply(audioManager.setVolume(Objects.requireNonNull(event.getOption("volume")).getAsInt())).queue();
            case "clear" -> event.reply(audioManager.clearQueue()).queue();
            case "stop", "pause" -> event.reply(audioManager.pausePlayer()).queue();
            case "resume" -> event.reply(audioManager.resumePlayer()).queue();
            case "dc", "leave", "disconnect" -> event.reply(audioManager.disconnectBot()).queue();
            case "follow" -> event.reply(audioManager.followUser(Objects.requireNonNull(event.getMember()))).setEphemeral(true).queue();
            case "queue" -> event.reply(audioManager.displayQueue()).queue();
            case "shuffle" -> event.reply(audioManager.shufflePlayer()).queue();
            case "song", "song-info", "info" -> event.reply(audioManager.sendTrackInfo()).setEphemeral(true).queue();
            case "remove" -> event.reply(audioManager.removeTrack(Objects.requireNonNull(event.getOption("index")).getAsInt())).queue();
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
                event.reply(audioManager.seekTrack(stringBuilder.toString())).queue();
            }
            case "fix" -> event.reply(audioManager.fixAudio(Objects.requireNonNull(event.getMember()))).setEphemeral(true).queue();
            case "loop" -> {
                Message message;
                if (event.getOption("type") != null) {
                    message = Objects.requireNonNull(event.getOption("type")).getAsString().equalsIgnoreCase("queue") ? audioManager.loopQueue() : audioManager.loopSong();
                } else message = audioManager.loopQueue();
                event.reply(message).queue();
            }
            case "move" -> event.reply(audioManager.moveSong(Objects.requireNonNull(event.getOption("pos1")).getAsInt(), Objects.requireNonNull(event.getOption("pos2")).getAsInt())).queue();
            case "hijack" -> event.reply(audioManager.enableDJ(event.getUser(), event.getGuild())).queue();
            case "skipto" -> event.reply(audioManager.skipTo(Objects.requireNonNull(event.getOption("index")).getAsInt())).queue();
            default -> throw new IllegalStateException("Unexpected value: " + commandName);
        }
    }
}
