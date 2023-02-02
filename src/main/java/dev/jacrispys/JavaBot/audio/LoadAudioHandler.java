package dev.jacrispys.JavaBot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.UserStats;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;


/**
 * Manages audio tracks being loaded and played.
 * @param guildAudioManager the audio manager that the tracks are sourced from.
 */
public record LoadAudioHandler(GuildAudioManager guildAudioManager) {


    @NotNull
    public MessageData loadAndPlay(final String trackUrl, AudioChannel voiceChannel, Member requester, boolean playTop, boolean editMsg) {
        final SynchronousQueue<MessageData> queue = new SynchronousQueue<>();
        guildAudioManager.getAudioManager().loadItemOrdered(guildAudioManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                guildAudioManager.setRequester(audioTrack, requester.getUser());

                try {
                    MySqlStats.getInstance().incrementUserStat(requester, UserStats.SONG_QUEUES);
                } catch (SQLException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    queue.put(editMsg ? new MessageEditBuilder().setEmbeds(guildAudioManager.trackLoaded(trackUrl, audioTrack, voiceChannel, playTop)).build() : new MessageCreateBuilder().setEmbeds(guildAudioManager.trackLoaded(trackUrl, audioTrack, voiceChannel, playTop)).build());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                try {
                    if(audioPlaylist.isSearchResult()) {
                        MySqlStats.getInstance().incrementUserStat(requester, UserStats.SONG_QUEUES);
                    } else if (!audioPlaylist.isSearchResult()){
                        MySqlStats.getInstance().incrementUserStat(requester, UserStats.PLAYLIST_QUEUES);
                    }
                } catch (SQLException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    guildAudioManager.setRequester(track, requester.getUser());
                }
                try {
                    queue.put(editMsg ? new MessageEditBuilder().setEmbeds(guildAudioManager.playListLoaded(trackUrl, audioPlaylist, voiceChannel, playTop)).build() : new MessageCreateBuilder().setEmbeds(guildAudioManager.playListLoaded(trackUrl, audioPlaylist, voiceChannel, playTop)).build());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void noMatches() {
                try {
                    queue.put(guildAudioManager.trackNotFound(trackUrl, editMsg));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void loadFailed(FriendlyException e) {
                try {
                    queue.put(guildAudioManager.trackLoadFailed(trackUrl, e, editMsg));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public MessageData skipTrack(GuildAudioManager audioManager, Member request) {
        return audioManager.skipTrack(request);
    }
}
