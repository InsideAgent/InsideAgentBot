package dev.jacrispys.JavaBot.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InactivityTimer extends ListenerAdapter {

    private static boolean inactivityExpired(long inactiveStart) {
        return Duration.ofMillis(System.currentTimeMillis() - inactiveStart).toMillis() >= Duration.ofMinutes(15).toMillis();
    }

    @SuppressWarnings("all")
    public static void startInactivity(AudioPlayer player, Guild guild) {
        long startMillis = System.currentTimeMillis();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable service = () -> {
            if(player.getPlayingTrack() != null && !player.isPaused() && guild.getSelfMember().getVoiceState().getChannel().getMembers().size() > 1) {
                executorService.shutdown();
            } else {
                if(inactivityExpired(startMillis)) {
                    try {
                        TextChannel channel = guild.getTextChannelById(MySQLConnection.getInstance().getMusicChannel(guild));
                        assert channel != null;
                        channel.sendMessage("Left the channel due to inactivity!").queue(msg -> msg.delete().queueAfter(3, TimeUnit.SECONDS));
                    }catch (SQLException ignored) {
                    } finally {
                        player.destroy();
                        GuildAudioManager.getGuildAudioManager(guild).clearQueue();
                        guild.getAudioManager().closeAudioConnection();
                        executorService.shutdown();
                    }
                }
            }
        };
        executorService.scheduleAtFixedRate(service, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if(event.getChannelLeft().getMembers().contains(event.getGuild().getSelfMember())) {
            if(event.getChannelLeft().getMembers().size() < 2) {
                startInactivity(GuildAudioManager.getGuildAudioManager(event.getGuild()).audioPlayer, event.getGuild());
            }
        }
    }
}
