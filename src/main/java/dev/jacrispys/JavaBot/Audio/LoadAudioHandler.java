package dev.jacrispys.JavaBot.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.concurrent.SynchronousQueue;


public record LoadAudioHandler(GuildAudioManager guildAudioManager) {

    public Message loadAndPlay(final String trackUrl, VoiceChannel voiceChannel, User requester, boolean playTop) {
        final SynchronousQueue<Message> queue = new SynchronousQueue<>();
        guildAudioManager.getAudioManager().loadItemOrdered(guildAudioManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                guildAudioManager.setRequester(audioTrack, requester);
                try {
                    queue.put(new MessageBuilder().setEmbeds(guildAudioManager.trackLoaded(trackUrl, audioTrack, voiceChannel, playTop)).build());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    guildAudioManager.setRequester(track, requester);
                }
                try {
                    queue.put(new MessageBuilder().setEmbeds(guildAudioManager.playListLoaded(trackUrl, audioPlaylist, voiceChannel, playTop)).build());
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

    public Message skipTrack(GuildAudioManager audioManager) {
        return audioManager.skipTrack();
    }
}
