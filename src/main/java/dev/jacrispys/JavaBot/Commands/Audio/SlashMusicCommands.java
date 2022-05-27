package dev.jacrispys.JavaBot.Commands.Audio;

import dev.jacrispys.JavaBot.Audio.GuildAudioManager;
import dev.jacrispys.JavaBot.Audio.LoadAudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SlashMusicCommands extends ListenerAdapter {

    public SlashMusicCommands() {

    }

    public void initCommands(JDA jda, List<Guild> guilds) {
        updateJdaCommands(jda);
        guilds.forEach(this::updateGuildCommands);
    }

    protected void updateJdaCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("play", "Add a link to most streaming platforms, or use its name to search!")
                        .addOption(OptionType.STRING, "url", "Track to search for.", true)
                        .addOptions(new OptionData(OptionType.STRING, "search", "Method to search for track with.", false)
                                .addChoice("spotify", "spsearch:")
                                .addChoice("apple", "amsearch:")
                                .addChoice("youtube", "ytsearch:")),
                Commands.slash("p", "Add a link to most streaming platforms, or use its name to search!")
                        .addOption(OptionType.STRING, "url", "Track to search for.", true)
                        .addOptions(new OptionData(OptionType.STRING, "search", "Method to search for track with.", false)
                                .addChoice("spotify", "spsearch:")
                                .addChoice("apple", "amsearch:")
                                .addChoice("youtube", "ytsearch:")),
                Commands.slash("skip", "Skips the current song!"),
                Commands.slash("s", "Skips the current song!"),
                Commands.slash("volume", "A number 1-500 to adjust volume!")
                        .addOption(OptionType.INTEGER, "volume", " 1-500", true),
                Commands.slash("clear", "Clears the queue."),
                Commands.slash("c", "Clears the queue."),
                Commands.slash("stop", "Pauses the currently playing audio."),
                Commands.slash("pause", "Pauses the currently playing audio."),
                Commands.slash("resume", "Resumes the currently playing audio."),
                Commands.slash("dc", "Disconnects the bot from its channel!"),
                Commands.slash("leave", "Disconnects the bot from its channel!"),
                Commands.slash("disconnect", "Disconnects the bot from its channel!"),
                Commands.slash("follow", "moves the bot to your current channel!"),
                Commands.slash("queue", "Shows a Embed of songs (10 per page) with page selectors, and a button to remove the message!"),
                Commands.slash("q", "Shows a Embed of songs (10 per page) with page selectors, and a button to remove the message!"),
                Commands.slash("shuffle", "Shuffles the current queue."),
                Commands.slash("song", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("songinfo", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("info", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("remove", "Removes a song from the queue at a given index number!")
                        .addOption(OptionType.INTEGER, "index", "Index to remove from queue!", true),
                Commands.slash("seek", "Takes in arg in the form of HH:mm:ss that seeks to that time in the current song!")
                        .addOption(OptionType.INTEGER, "minutes", "number of minutes", true)
                        .addOption(OptionType.INTEGER, "seconds", "number of seconds", true)
                        .addOption(OptionType.INTEGER, "hours", "(Optional) Number of hours", false),
                Commands.slash("fix", "Fixes connectivity by changing regions."),
                Commands.slash("loop", "Enables/Disables loop (skipping resets loops)")
                        .addOption(OptionType.STRING, "loop-queue", "Choose between loop or queue", false),
                Commands.slash("move", "Swaps positions of two index's in the queue.")
                        .addOption(OptionType.INTEGER, "pos1", "position one to move.", true)
                        .addOption(OptionType.INTEGER, "pos2", "second position to mve.", true),
                Commands.slash("hijack", "Secret Command \uD83E\uDD2BOnly Accessible by certified DJ's!"),
                Commands.slash("playtop", "adds a song to the top of queue")
                        .addOption(OptionType.STRING, "url", "track to add to queue", true)
                        .addOption(OptionType.STRING, "method", "Method to search for track with.", false),
                Commands.slash("ptop", "adds a song to the top of queue")
                        .addOption(OptionType.STRING, "url", "track to add to queue", true)
                        .addOption(OptionType.STRING, "method", "Method to search for track with.", false),
                Commands.slash("skipto", "Skips the queue to a given index.")
                        .addOption(OptionType.INTEGER, "index", "Index to skip to.", true),
                Commands.slash("st", "Skips the queue to a given index.")
                        .addOption(OptionType.INTEGER, "index", "Index to skip to.", true),
                Commands.slash("fileplay", "adds a song to the queue")
                        .addOption(OptionType.ATTACHMENT, "url", "track to add to queue", true),
                Commands.slash("fp", "adds a song to the queue")
                        .addOption(OptionType.ATTACHMENT, "index", "track to add to queue", true)
                ).queue();
    }

    protected void updateGuildCommands(Guild guild) {
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        GuildAudioManager audioManager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        LoadAudioHandler audioHandler = new LoadAudioHandler(audioManager);
        switch (commandName) {
            case "play","p","ptop","playtop" -> {
                VoiceChannel channel;
                assert event.getMember() != null;
                assert event.getGuild() != null;
                channel = (VoiceChannel) event.getGuild().getMember(event.getMember()).getVoiceState().getChannel();
                String track = event.getOption("url").getAsString();
                if (channel == null) {
                    event.reply("Could not load song, as you are not in a voice channel!").setEphemeral(true).queue();
                    return;
                }
                try {
                    new URL(track);
                } catch (MalformedURLException ignored) {
                    String searchMethod = "ytsearch";
                    if(event.getOption("search") != null) {
                        searchMethod = event.getOption("search").getAsString();
                    }
                      switch (searchMethod.toLowerCase()) {
                        case ("spsearch:") -> searchMethod = "spsearch:";
                        case ("amsearch:") -> searchMethod = "amsearch:";
                        default -> searchMethod = "ytsearch:";
                    };
                    track = searchMethod + track;
                }
                boolean playTop = (commandName.equalsIgnoreCase("ptop") || commandName.equalsIgnoreCase("playtop"));
                audioHandler.loadAndPlay(event.getTextChannel(), track, channel, event.getUser(), playTop);
                event.reply("added to queue").setEphemeral(true).queue();
            }
            default -> event.reply(event.getCommandString()).setEphemeral(true).queue();
        }
    }
}
