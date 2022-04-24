package dev.jacrispys.JavaBot.Commands;

import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import dev.jacrispys.JavaBot.audio.LoadAudioHandler;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class MusicCommands extends ListenerAdapter {

    private final LoadAudioHandler audioHandler = new LoadAudioHandler();

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        GuildAudioManager audioManager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        if (event.getMessage().getContentRaw().toLowerCase().contains("-play")) {
            String trackUrl = event.getMessage().getContentRaw().split("-play ")[1];
            VoiceChannel channel;
            try {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                new URL(trackUrl);
                audioHandler.loadAndPlay(event.getTextChannel(), trackUrl, audioManager, channel);
            } catch(MalformedURLException ex) {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                String ytSearch = ("ytsearch:" + trackUrl);
                audioHandler.loadAndPlay(event.getTextChannel(), ytSearch, audioManager, channel);
            }
        }else if(event.getMessage().getContentRaw().equalsIgnoreCase("-skip")) {
            audioHandler.skipTrack(audioManager, event.getTextChannel());
        }else if (event.getMessage().getContentRaw().contains("-volume")) {
            try {
            int i = Integer.parseInt(event.getMessage().getContentRaw().split("-volume ")[1]);

                audioManager.setVolume(i, event.getTextChannel());
            } catch(NumberFormatException ex) {
                event.getMessage().reply(event.getMessage().getContentRaw().split("-volume ")[1] + " is not a number 1 - 100!").queue();
            }
        }
    }
}
