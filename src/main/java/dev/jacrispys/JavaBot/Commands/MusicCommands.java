package dev.jacrispys.JavaBot.Commands;

import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import dev.jacrispys.JavaBot.audio.LoadAudioHandler;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MusicCommands extends ListenerAdapter {

    private final LoadAudioHandler audioHandler = new LoadAudioHandler();

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        if (event.getMessage().getContentRaw().toLowerCase().contains("-play")) {
            String trackUrl = event.getMessage().getContentRaw().split("-play")[1];
            VoiceChannel channel;
            try {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                audioHandler.loadAndPlay(event.getTextChannel(), trackUrl, GuildAudioManager.getGuildAudioManager(event.getGuild()), channel);
            } catch(NullPointerException ex) {
                event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
            }
        }
    }
}
