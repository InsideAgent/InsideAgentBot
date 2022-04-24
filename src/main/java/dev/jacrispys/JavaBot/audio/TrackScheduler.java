package dev.jacrispys.JavaBot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> queue;


    /**
     *
     * @param audioPlayer the player that schedules the audio
     */
    public TrackScheduler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     *
     * @param track is the audio to be played
     *  startTrack will return true if no song is playing, as the boolean is noInterrupt
     * if false, offers the track to the queue
     */
    public void queue(AudioTrack track) {
        if(!audioPlayer.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    /**
     *  Starts the next track in the queue, ignores if something is playing.
     */
    public void nextTrack() {
        audioPlayer.startTrack(queue.poll(), false);
    }


    public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            nextTrack();
        }
    }

    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

}
