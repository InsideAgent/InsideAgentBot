package dev.jacrispys.JavaBot.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;


public record LoadAudioHandler(GuildAudioManager guildAudioManager) {

    public void loadAndPlay(TextChannel channel, final String trackUrl, VoiceChannel voiceChannel, User requester, boolean playTop) {
        guildAudioManager.getAudioManager().loadItemOrdered(guildAudioManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                guildAudioManager.trackLoaded(channel, trackUrl, audioTrack, voiceChannel, playTop);
                guildAudioManager.setRequester(audioTrack, requester);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                guildAudioManager.playListLoaded(channel, trackUrl, audioPlaylist, voiceChannel, playTop);
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    guildAudioManager.setRequester(track, requester);
                }
            }

            @Override
            public void noMatches() {
                guildAudioManager.trackNotFound(channel, trackUrl);
            }


            @Override
            public void loadFailed(FriendlyException e) {
                guildAudioManager.trackLoadFailed(channel, trackUrl, e);
                e.printStackTrace();
            }
        });


    }

    public void skipTrack(GuildAudioManager audioManager, TextChannel textChannel) {
        audioManager.skipTrack(textChannel);
    }
}
