package dev.jacrispys.JavaBot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Manager for audio sending via {@link AudioPlayer}
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    /**
     * @return true if the AudioFrame is provided successfully
     */
    @Override
    public boolean canProvide() {
        return audioPlayer.provide(frame);
    }

    /**
     * @return flipped byte buffer to provide 20Ms audio
     */
    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        ((Buffer) buffer).flip();
        return buffer;
    }

    /**
     * @return true to ensure that Opus audio encoding is used.
     */
    @Override
    public boolean isOpus() {
        return true;
    }
}
