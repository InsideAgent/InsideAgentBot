package dev.jacrispys.JavaBot.Commands.Audio;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum GuildCommands {

    PLAY("Add a link to most streaming platforms, or use its name to search!","p", OptionType.STRING, OptionType.ATTACHMENT),
    SKIP("Skips the current song!", "s", (OptionType) null),
    VOLUME("A number 1-500 to adjust volume!", "v", OptionType.INTEGER),
    CLEAR("clears the current queue!", "c", (OptionType) null),
    STOP("pauses the current playing audio!", "pause", (OptionType) null),
    RESUME("resumes the current queue!", (String) null, (OptionType) null),
    DISCONNECT("disconnects the bot from its channel!", new String[]{"dc", "leave"}, (OptionType) null),
    FOLLOW("moves the bot to your current channel!", "move", (OptionType) null),
    QUEUE("Shows a Embed of songs (10 per page) with page selectors, and a button to remove the message!", "q", (OptionType) null),
    SHUFFLE("Shuffles the queue!", (String) null, (OptionType) null),
    INFO("Shows info about the song, including a progress bar, the song requester, and Title/Author!", new String[]{"songinfo", "song", "current"}, (OptionType) null),
    REMOVE("Removes a song from the queue at a given index number!", (String) null, OptionType.INTEGER),
    SEEK("Takes in arg in the form of HH:mm:ss that seeks to that time in the current song!", (String) null, OptionType.STRING),
    FIX("Resets the VoiceChannel's region to help reduce latency and audio lag.", (String) null, (OptionType) null),
    LOOP("Args are either song or queue to loop a song or to loop the entire queue! (Skipping resets any loops!)", (String) null, OptionType.STRING),
    MOVE("Intakes two numbers and swaps their positions in the queue!", (String) null, OptionType.INTEGER, OptionType.INTEGER),
    HIJACK("Secret Command \uD83E\uDD2B Only Accessible by certified DJ's!", (String) null, (OptionType) null),
    PLAYTOP("Adds a song to the top of the queue.", "ptop", OptionType.STRING, OptionType.ATTACHMENT),
    SKIPTO("Skips to the given index in the queue.", "st", OptionType.INTEGER);


    @Nullable
    private final String commandDescription;
    @Nullable
    private final String[] aliases;
    @Nullable
    private final List<OptionType> argTypes;

    @Nullable
    public String getDescription() {
        return commandDescription;
    }

    @Nullable
    public List<String> getAliases() {
        return Arrays.stream(aliases).toList();
    }

    @Nullable
    public List<OptionType> getArgTypes() {
        return argTypes;
    }

    GuildCommands (@Nullable String commandDescription, @Nullable String[] aliases, OptionType... argTypes) {
        this.commandDescription = commandDescription;
        this.aliases = aliases;
        this.argTypes = Arrays.stream(argTypes).toList();
    }
    GuildCommands (@Nullable String commandDescription, @Nullable String aliases, OptionType... argTypes) {
        this.commandDescription = commandDescription;
        this.aliases = new String[]{aliases};
        this.argTypes = Arrays.stream(argTypes).toList();
    }
}
