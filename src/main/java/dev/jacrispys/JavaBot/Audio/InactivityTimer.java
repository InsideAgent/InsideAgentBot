package dev.jacrispys.JavaBot.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InactivityTimer {

    private static boolean inactivityExpired(long inactiveStart) {
        return Duration.ofMillis(System.currentTimeMillis() - inactiveStart).toMillis() >= Duration.ofMinutes(15).toMillis();
    }

    public static void startInactivity(AudioPlayer player, Guild guild) {
        long startMillis = System.currentTimeMillis();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable service = () -> {
            if(player.getPlayingTrack() != null && !player.isPaused()) {
                executorService.shutdown();
            } else {
                if(inactivityExpired(startMillis)) {
                    try {
                        TextChannel channel = guild.getTextChannelById(MySQLConnection.getInstance().getMusicChannel(guild));
                        assert channel != null;
                        channel.sendMessage("Left the channel due to inactivity!").queue(msg -> msg.delete().queueAfter(3, TimeUnit.SECONDS));
                        player.destroy();
                    }catch (SQLException ignored) {
                    } finally {
                        GuildAudioManager.getGuildAudioManager(guild).clearQueue();
                        guild.getAudioManager().closeAudioConnection();
                        executorService.shutdown();
                    }
                }
            }
        };
        executorService.scheduleAtFixedRate(service, 0, 5, TimeUnit.SECONDS);
    }
}
