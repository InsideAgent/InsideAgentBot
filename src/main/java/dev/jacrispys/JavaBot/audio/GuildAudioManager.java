package dev.jacrispys.JavaBot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class GuildAudioManager {

    private final AudioPlayerManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    private static GuildAudioManager instance = null;

    public static void initManager() {
        if(instance == null) {
            instance = new GuildAudioManager();
        }
    }

    private static final Map<Guild, GuildAudioManager> audioManagers = new HashMap<>();

    public static synchronized GuildAudioManager getGuildAudioManager(Guild guild) {
        if(audioManagers.get(guild) == null) {
            GuildAudioManager audioManager = new GuildAudioManager();
            audioManagers.put(guild, audioManager);
            return audioManager;
        }
        return audioManagers.get(guild);
    }


    protected GuildAudioManager() {
        this.audioManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioManager);
        AudioSourceManagers.registerLocalSource(audioManager);
        this.audioPlayer = audioManager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        audioPlayer.addListener(this.scheduler);
        sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public AudioPlayerManager getAudioManager() {
        return this.audioManager;
    }

    public void trackLoaded(TextChannel channel, String trackUrl, AudioTrack track, VoiceChannel voiceChannel) {
        channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel);
    }
    public void playListLoaded(TextChannel channel, String trackUrl, AudioPlaylist playlist, VoiceChannel voiceChannel) {
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }

        channel.sendMessage(("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")")).queue();

        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), firstTrack, voiceChannel);
    }
    public void trackNotFound(TextChannel channel, String trackUrl) {
        channel.sendMessage("Could not find: " + trackUrl).queue();
    }

    public void trackLoadFailed(TextChannel channel, String trackUrl, FriendlyException exception) {
        channel.sendMessage("Could not play: " + exception.getMessage()).queue();
    }

    private void play(Guild guild, GuildAudioManager guildAudioManager, AudioTrack track, VoiceChannel voiceChannel) {
        attachToVoiceChannel(guild, voiceChannel);
        guildAudioManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildAudioManager manager = getGuildAudioManager(channel.getGuild());
        manager.scheduler.nextTrack();
    }

    private void attachToVoiceChannel(Guild guild, VoiceChannel channel) {
        boolean inVoiceChannel;
        try {
            guild.getSelfMember().getVoiceState().getChannel();
            inVoiceChannel = true;
        } catch(NullPointerException ex) {
            inVoiceChannel = false;
        }

        if(!inVoiceChannel) {
            AudioManager manager = guild.getAudioManager();
            manager.openAudioConnection(channel);
        }
    }
}
