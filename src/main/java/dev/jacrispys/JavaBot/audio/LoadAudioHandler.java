package dev.jacrispys.JavaBot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.UserStats;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.sql.SQLException;
import java.util.concurrent.SynchronousQueue;


public record LoadAudioHandler(GuildAudioManager guildAudioManager) {


    public Message loadAndPlay(final String trackUrl, VoiceChannel voiceChannel, Member requester, boolean playTop) {
        final SynchronousQueue<Message> queue = new SynchronousQueue<>();
        guildAudioManager.getAudioManager().loadItemOrdered(guildAudioManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                guildAudioManager.setRequester(audioTrack, requester.getUser());

                try {
                    MySqlStats.getInstance().incrementUserStat(requester, UserStats.SONG_QUEUES);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                try {
                    queue.put(new MessageCreateBuilder().setEmbeds(guildAudioManager.trackLoaded(trackUrl, audioTrack, voiceChannel, playTop)).build());
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
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    guildAudioManager.setRequester(track, requester.getUser());
                }
                try {
                    queue.put(new MessageCreateBuilder().setEmbeds(guildAudioManager.playListLoaded(trackUrl, audioPlaylist, voiceChannel, playTop)).build());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void noMatches() {
                try {
                    queue.put(guildAudioManager.trackNotFound(trackUrl));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void loadFailed(FriendlyException e) {
                try {
                    queue.put(guildAudioManager.trackLoadFailed(trackUrl, e));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        try {
            Message message = queue.take();
            System.out.println(message.getContentRaw());
            return message;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message skipTrack(GuildAudioManager audioManager, Member request) {
        return audioManager.skipTrack(request);
    }
}
