package dev.jacrispys.JavaBot.Commands.Audio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.List;

public class SlashMusicCommands extends ListenerAdapter {

    public SlashMusicCommands(JDA jda, List<Guild> guilds) {

    }

    protected void updateGuildCommands(Guild guild) {
        guild.updateCommands().addCommands(
                Commands.slash("play", "Add a link to most streaming platforms, or use its name to search!")
                        .addOption(OptionType.STRING, "url/search", "Track to search for."),
                Commands.slash("p", "Add a link to most streaming platforms, or use its name to search!")
                        .addOption(OptionType.STRING, "url/search", "Track to search for."),
                Commands.slash("skip", "Skips the current song!"),
                Commands.slash("s", "Skips the current song!"),
                Commands.slash("volume", "A number 1-500 to adjust volume!")
                        .addOption(OptionType.INTEGER, "volume", " 1-500"),
                Commands.slash("clear", "Clears the queue."),
                Commands.slash("c", "Clears the queue."),
                Commands.slash("stop", "Pauses the currently playing audio."),
                Commands.slash("pause", "Pauses the currently playing audio."),
                Commands.slash("resume", "Resumes the currently playing audio."),
                Commands.slash("dc", "Disconnects the bot from its channel!"),
                Commands.slash("leave", "Disconnects the bot from its channel!"),
                Commands.slash("disconnect", "Disconnects the bot from its channel!"),
                Commands.slash("follow", "moves the bot to your current channel!"),
                Commands.slash("move", "moves the bot to your current channel!"),
                Commands.slash("queue", "Shows a Embed of songs (10 per page) with page selectors, and a button to remove the message!"),
                Commands.slash("q", "Shows a Embed of songs (10 per page) with page selectors, and a button to remove the message!"),
                Commands.slash("shuffle", "Shuffles the current queue."),
                Commands.slash("song", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("songinfo", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("info", "Shows info about the song, including a progress bar, the song requester, and Title/Author!"),
                Commands.slash("remove", "Removes a song from the queue at a given index number!")
                        .addOption(OptionType.INTEGER, "Index", "Index to remove from queue!"),
                Commands.slash("seek", "Takes in arg in the form of HH:mm:ss that seeks to that time in the current song!")
                        .addOption(OptionType.INTEGER, "Hours", "", false));


    }


}
