package dev.jacrispys.JavaBot.audio;

import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class GuildAudioManager {

    private final AudioPlayerManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final Map<Guild, TextChannel> announceChannel = new HashMap<>();
    private final Map<AudioTrack, User> requester = new HashMap<>();

    private boolean djEnabled = false;

    private static final Map<Guild, GuildAudioManager> audioManagers = new HashMap<>();

    private static Guild currentGuild = null;

    public static synchronized GuildAudioManager getGuildAudioManager(Guild guild) {
        if (audioManagers.get(guild) == null) {
            GuildAudioManager audioManager = new GuildAudioManager(guild);
            audioManagers.put(guild, audioManager);
            if(currentGuild == null) currentGuild = guild;
            return audioManager;
        }
        if(currentGuild == null) currentGuild = guild;
        return audioManagers.get(guild);
    }




    protected GuildAudioManager(Guild instance) {
        this.audioManager = new DefaultAudioPlayerManager();
        this.audioPlayer = audioManager.createPlayer();
        AudioSourceManagers.registerLocalSource(audioManager);

        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(System.getenv("SpotifyClientId"));
        spotifyConfig.setClientSecret(System.getenv("SpotifySecret"));
        spotifyConfig.setCountryCode("US");
        audioManager.registerSourceManager(new SpotifySourceManager(null, spotifyConfig, audioManager));

        currentGuild = instance;

        AudioSourceManagers.registerRemoteSources(audioManager);
        this.scheduler = new TrackScheduler(this.audioPlayer, currentGuild);
        audioPlayer.addListener(this.scheduler);
        sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public AudioPlayerManager getAudioManager() {
        return this.audioManager;
    }

    public void trackLoaded(TextChannel channel, String trackUrl, AudioTrack track, VoiceChannel voiceChannel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
        announceChannel.put(channel.getGuild(), channel);

        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel);
    }

    public void playListLoaded(TextChannel channel, String trackUrl, AudioPlaylist playlist, VoiceChannel voiceChannel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
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
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Could not find: " + trackUrl).queue();
    }

    public void trackLoadFailed(TextChannel channel, String trackUrl, FriendlyException exception) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Could not play: " + exception.getMessage()).queue();
    }

    public void setRequester(AudioTrack track, User requester) {
        this.requester.put(track, requester);
    }

    @Nullable
    public Map<AudioTrack, User> getRequester() {
        return this.requester;
    }

    private void play(Guild guild, GuildAudioManager guildAudioManager, AudioTrack track, VoiceChannel voiceChannel) {

        attachToVoiceChannel(guild, voiceChannel);
        guildAudioManager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
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
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (i < 0 || i > 500) {
            channel.sendMessage("Volume must be between 1-500!").queue();
            return;
        }
        channel.sendMessage("Volume is currently set at: " + i).queue();
        audioPlayer.setVolume(i);
    }

    public void displayQueue(TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }

        StringBuilder queue = new StringBuilder();
        BlockingQueue<AudioTrack> tracks = scheduler.getTrackQueue();
        int i = 1;
        for (AudioTrack track : tracks) {
            queue.append("`" + i + ". " + track.getInfo().author + " - " + track.getInfo().title + "` \n");
            i++;
        }
        channel.sendMessage("Showing Queue of " + tracks.size() + " tracks!").queue();
        if(!queue.toString().isEmpty()) {
            channel.sendMessage(queue.toString()).queue();
        }
    }


    public void announceNextTrack(Guild guild, AudioTrack newSong) {
        if(djEnabled) {
            try {
                TextChannel channel = guild.getTextChannelById(MySQLConnection.getInstance().getMusicChannel(guild));
                channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
                return;
            } catch (SQLException ex) {
                return;
            }
        }
        try {
            TextChannel channel = guild.getTextChannelById(MySQLConnection.getInstance().getMusicChannel(guild));
            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle("Currently Playing... \uD83C\uDFB5\uD83C\uDFB5\uD83C\uDFB5");
            eb.addField(newSong.getInfo().title, "By - " + newSong.getInfo().author, false);
            eb.setColor(Color.decode("#155b5e"));
            channel.sendMessageEmbeds(eb.build()).queue();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void clearQueue(TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Clearing queue!").queue();
        scheduler.setQueue(new LinkedBlockingQueue<>());
    }

    public void pausePlayer(TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (!audioPlayer.isPaused()) {
            channel.sendMessage("Paused ⏸️").queue();
            audioPlayer.setPaused(true);
        }
    }

    public void resumePlayer(TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (audioPlayer.isPaused()) {
            channel.sendMessage("Resumed ▶️").queue();
            audioPlayer.setPaused(false);
        }
    }

    public void shufflePlayer(TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        Collections.shuffle(trackList);
        BlockingQueue<AudioTrack> tracks = new LinkedBlockingQueue<>(trackList);
        scheduler.setQueue(tracks);
        channel.sendMessage("Shuffling! \uD83C\uDFB2").queue();
    }

    public void sendTrackInfo(TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if(audioPlayer.getPlayingTrack() == null) {
            channel.sendMessage("Cannot get track info as no song is playing!").queue();
            return;
        }
        AudioTrack track = audioPlayer.getPlayingTrack();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(track.getInfo().author + " - " + track.getInfo().title);
        String durationSlider = ("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        float div = ((float) track.getPosition() / (float) track.getDuration()*20);
        int duration = Math.round(div);
        String emoji = ("\uD83D\uDD18");
        if(duration > 20) duration = 20;
        durationSlider = durationSlider.substring(0,duration) + emoji + durationSlider.substring(duration+1);
        String time = "[" + DurationFormatUtils.formatDuration(track.getPosition(), "HH:mm:ss") + "/" + DurationFormatUtils.formatDuration(track.getDuration(), "HH:mm:ss") + "]";
        eb.addField("", "-Requested By: " + getRequester().get(track).getAsMention() + "\n" + durationSlider + "\n" + time, false);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void removeTrack(int position, TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        BlockingQueue<AudioTrack> tracks = scheduler.getTrackQueue();
        AudioTrack removed = tracks.stream().toList().get(position - 1);
        if(removed != null) {
            channel.sendMessage("Successfully Removed: `" + removed.getInfo().author + " - "  + removed.getInfo().title + "` from the queue!").queue();
            ArrayList<AudioTrack> trackList = new ArrayList<>(tracks.stream().toList());
            trackList.remove(position - 1);
            tracks = new LinkedBlockingQueue<>(trackList);
            scheduler.setQueue(tracks);
        } else {
            channel.sendMessage("Could not locate track at position: " + position + "!").queue();
        }

    }

    public void seekTrack(String time, TextChannel channel) {
        if(djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            Function<String, String> adjust = input -> input.indexOf(":", 3) >= 0
                    ? input : "00:" + input;
            long millis = dtf.parse(adjust.apply(time)).get(ChronoField.MILLI_OF_DAY);
            audioPlayer.getPlayingTrack().setPosition(millis);
            channel.sendMessage("Seeking to: " + time + "!").queue();
        } catch (Exception ex) {
            channel.sendMessage("Invalid use of seek! Please use the format 'HH:mm:ss'").queue();
        }
    }

    protected void djLoaded(TextChannel channel, String trackUrl, AudioTrack track, VoiceChannel voiceChannel) {
        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel);
    }


    public void enableDJ(TextChannel channel, User sender, Guild guild) {
        if(!djEnabled) {
            clearQueue(channel);
            skipTrack(channel);
            djEnabled = true;
        } else {
            djEnabled = false;
            clearQueue(channel);
            skipTrack(channel);
            channel.sendMessage("So sad but, the party is over now! :(").queue();
            return;
        }
        VoiceChannel vc = (VoiceChannel) guild.getMember(sender).getVoiceState().getChannel();
        this.getAudioManager().loadItemOrdered(this, "http://10.0.0.109:8000/mixxx", new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                djLoaded(channel, "http://10.0.0.109:8000/mixxx", audioTrack, vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
        channel.sendMessage("This bot has now been taken over by DJ " + sender.getAsMention() + "! ヽ(⌐■_■)ノ♬").queue();


    }
}
