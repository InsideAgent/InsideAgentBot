package dev.jacrispys.JavaBot.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer audioPlayer;
    private BlockingQueue<AudioTrack> queue;
    private final Guild guild;


    /**
     *
     * @param audioPlayer the player that schedules the audio
     */
    public TrackScheduler(AudioPlayer audioPlayer, @NotNull Guild guild) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
        this.guild = guild;
    }

    public BlockingQueue<AudioTrack> getTrackQueue() {
        return this.queue;
    }
    public void setQueue(BlockingQueue<AudioTrack> queue) {
        this.queue = queue;
    }

    /**
     *
     * @param track is the audio to be played
     *  startTrack will return true if no song is playing, as the boolean is noInterrupt
     * if false, offers the track to the queue
     */
    @SuppressWarnings("all")
    public void queue(AudioTrack track) {
        if(!audioPlayer.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    /**
     *  Starts the next track in the queue, ignores if something is playing.
     */
    public void nextTrack() {
        AudioTrack nextTrack = queue.poll();
        audioPlayer.startTrack(nextTrack, false);
        if(nextTrack == null) InactivityTimer.startInactivity(audioPlayer, guild.getIdLong(), guild.getJDA());
    }


    @SuppressWarnings("all")
    public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack track, AudioTrackEndReason endReason) {
        if(GuildAudioManager.getGuildAudioManager(guild).queueLoop) {
            this.queue.offer(track.makeClone());
        }

        if(endReason.mayStartNext) {
            if(GuildAudioManager.getGuildAudioManager(guild).songLoop) {
                AudioTrack loopedTrack = track.makeClone();
                User requester = Objects.requireNonNull(GuildAudioManager.getGuildAudioManager(guild).getRequester()).get(track);
                audioPlayer.startTrack(loopedTrack, false);
                Objects.requireNonNull(GuildAudioManager.getGuildAudioManager(guild).getRequester()).remove(track);
                GuildAudioManager.getGuildAudioManager(guild).setRequester(loopedTrack, requester);
                return;
            }
            nextTrack();
        }
    }

    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        try {
            GuildAudioManager.getGuildAudioManager(guild).announceNextTrack(guild, track);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

}
