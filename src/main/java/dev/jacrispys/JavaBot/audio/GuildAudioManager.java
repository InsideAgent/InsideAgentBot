package dev.jacrispys.JavaBot.audio;

import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GuildAudioManager {

    private final AudioPlayerManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final Map<Guild, TextChannel> announceChannel = new HashMap<>();

    private static GuildAudioManager instance = null;

    public static void initManager() {
        if (instance == null) {
            instance = new GuildAudioManager();
        }
    }

    private static final Map<Guild, GuildAudioManager> audioManagers = new HashMap<>();

    private static Guild currentGuild = null;

    public static synchronized GuildAudioManager getGuildAudioManager(Guild guild) {
        if (audioManagers.get(guild) == null) {
            GuildAudioManager audioManager = new GuildAudioManager();
            audioManagers.put(guild, audioManager);
            currentGuild = guild;
            return audioManager;
        }
        return audioManagers.get(guild);
    }


    protected GuildAudioManager() {
        this.audioManager = new DefaultAudioPlayerManager();
        this.audioPlayer = audioManager.createPlayer();
        AudioSourceManagers.registerLocalSource(audioManager);

        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(System.getenv("SpotifyClientId"));
        spotifyConfig.setClientSecret(System.getenv("SpotifySecret"));
        spotifyConfig.setCountryCode("US");
        audioManager.registerSourceManager(new SpotifySourceManager(null, spotifyConfig, audioManager));


        AudioSourceManagers.registerRemoteSources(audioManager);
        this.scheduler = new TrackScheduler(this.audioPlayer, currentGuild);
        audioPlayer.addListener(this.scheduler);
        sendHandler = new AudioPlayerSendHandler(this.audioPlayer);

    }

    public AudioPlayerManager getAudioManager() {
        return this.audioManager;
    }

    public void trackLoaded(TextChannel channel, String trackUrl, AudioTrack track, VoiceChannel voiceChannel) {
        channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
        announceChannel.put(channel.getGuild(), channel);

        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel);
    }

    public void playListLoaded(TextChannel channel, String trackUrl, AudioPlaylist playlist, VoiceChannel voiceChannel) {
        AudioTrack firstTrack = playlist.getSelectedTrack();
        announceChannel.put(channel.getGuild(), channel);

        if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }

        channel.sendMessage(("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")")).queue();

        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), firstTrack, voiceChannel);

        if (!playlist.isSearchResult()) {
            channel.sendMessage(("Adding to queue playlist titled: " + playlist.getName())).queue();
            for (int i = 1; i < playlist.getTracks().size(); i++) {
                play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), playlist.getTracks().get(i), voiceChannel);
            }
        }
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

    public void skipTrack(TextChannel channel) {
        GuildAudioManager manager = getGuildAudioManager(channel.getGuild());
        manager.scheduler.nextTrack();
        if (audioPlayer.getPlayingTrack() != null) {
            channel.sendMessage("Track Skipped! Now Playing: " + manager.audioPlayer.getPlayingTrack().getInfo().title).queue();
        }
    }

    private void attachToVoiceChannel(Guild guild, VoiceChannel channel) {
        boolean inVoiceChannel = guild.getSelfMember().getVoiceState().inAudioChannel();

        if (!inVoiceChannel) {
            AudioManager manager = guild.getAudioManager();
            manager.openAudioConnection(channel);
            manager.setSendingHandler(sendHandler);
        }
    }

    public void setVolume(int i, TextChannel channel) {
        if (i < 0 || i > 100) {
            channel.sendMessage("Volume must be between 1-100!").queue();
        }
        channel.sendMessage("Volume is currently set at: " + i).queue();
        audioPlayer.setVolume(i);
    }

    public void displayQueue(TextChannel channel) {
        EmbedBuilder msg = new EmbedBuilder();
        msg.setTitle("Pagination");
        msg.setDescription("Hello World! This is the first page");

        msg.setFooter("Page 1/4");
        msg.setColor(0x33cc33);
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("page_1", Emoji.fromUnicode("⏪")));
        buttons.add(Button.primary("page_1", Emoji.fromUnicode("◀")));
        buttons.add(Button.danger("page_cancel", Emoji.fromUnicode("❌")));
        buttons.add(Button.primary("page_2", Emoji.fromUnicode("▶")));
        buttons.add(Button.primary("page_4", Emoji.fromUnicode("⏩")));

        channel.sendMessageEmbeds((msg.build())).setActionRow(buttons).queue();

        BlockingQueue<AudioTrack> tracks = scheduler.getTrackQueue();
        for (AudioTrack track : tracks) {

        }
    }


    public void announceNextTrack(Guild guild) {

    }

    public void clearQueue(TextChannel channel) {
        channel.sendMessage("Clearing queue!").queue();
        scheduler.setQueue(new LinkedBlockingQueue<>());
    }

    public void pausePlayer(TextChannel channel) {
        if (!audioPlayer.isPaused()) {
            channel.sendMessage("Paused ⏸️").queue();
            audioPlayer.setPaused(true);
        }
    }

    public void resumePlayer(TextChannel channel) {
        if (audioPlayer.isPaused()) {
            channel.sendMessage("Resumed ▶️").queue();
            audioPlayer.setPaused(false);
        }
    }

    public void shufflePlayer(TextChannel channel) {
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        Collections.shuffle(trackList);
        BlockingQueue<AudioTrack> tracks = new LinkedBlockingQueue<>(trackList);
        scheduler.setQueue(tracks);
        channel.sendMessage("Shuffling! \uD83C\uDFB2").queue();
    }
}
