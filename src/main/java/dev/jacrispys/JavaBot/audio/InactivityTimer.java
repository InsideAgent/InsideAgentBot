package dev.jacrispys.JavaBot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages inactive audio players, and destroys them when needed.
 */
public class InactivityTimer extends ListenerAdapter {


    private static final Logger logger = LoggerFactory.getLogger(InactivityTimer.class);
    private static final String className = InactivityTimer.class.getSimpleName();

    /**
     * Checks if given inactivity period has expired.
     * @param inactiveStart System Millis that indicate when inactivity was first discovered
     * @return true if 30 minutes past the start period has elapsed
     */
    private static boolean inactivityExpired(long inactiveStart) {
        return Duration.ofMillis(System.currentTimeMillis() - inactiveStart).toMillis() >= Duration.ofMinutes(30).toMillis();
    }

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private static final Map<Long, ScheduledFuture<?>> runnables = new HashMap<>();

    public static Map<Long, ScheduledFuture<?>> getRunnables() {
        return runnables;
    }

    /**
     * Schedules a runnable to check if any audio player's are inactive
     * @param player audio player to watch inactivity from
     * @param guildId guild the audioplayer is located in
     * @param jda instance to JDA API
     */
    public static void startInactivity(AudioPlayer player, Long guildId, JDA jda) {
        long startMillis = System.currentTimeMillis();
        Runnable service = () -> {
            if (player.getPlayingTrack() != null && !player.isPaused() && jda.getGuildById(guildId).getSelfMember().getVoiceState().getChannel().getMembers().size() > 1) {
                runnables.get(guildId).cancel(true);
                runnables.remove(guildId);
            } else {
                if (inactivityExpired(startMillis)) {
                    try {
                        TextChannel channel = jda.getGuildById(guildId).getTextChannelById(MySQLConnection.getInstance().getMusicChannel(jda.getGuildById(guildId)));
                        inactivityMessage(channel);
                    } catch (SQLException ex) {
                        logger.warn("{} -  SQL error locating music channel.", className);
                    } finally {
                        if (jda.getGuildById(guildId).getSelfMember().getVoiceState().inAudioChannel()) {
                            AudioManager manager = jda.getGuildById(guildId).getAudioManager();
                            GuildAudioManager.getGuildAudioManager(jda.getGuildById(guildId)).clearQueue();
                            GuildAudioManager.getGuildAudioManager(jda.getGuildById(guildId)).audioPlayer.destroy();
                            manager.closeAudioConnection();
                        }
                        if (!runnables.get(guildId).cancel(true)) {
                            logger.error("{} -  Error occurred whilst cancelling inactivity timer!", className);
                        }
                        runnables.remove(guildId);
                    }
                }
            }
        };
        runnables.put(guildId, executorService.scheduleAtFixedRate(service, 0, 5, TimeUnit.SECONDS));
    }

    /**
     * Starts inactivity if a user has left the VC and meets a number of other criterion
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() != null) {
            if (event.getChannelLeft().getMembers().contains(event.getGuild().getSelfMember())) {
                if (event.getChannelLeft().getMembers().size() < 2) {
                    startInactivity(GuildAudioManager.getGuildAudioManager(event.getGuild()).audioPlayer, event.getGuild().getIdLong(), event.getJDA());
                }
            }
        }
    }

    /**
     * Sends a message to the given channel that the inactivity grace period has expired.
     */
    private static void inactivityMessage(TextChannel channel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        User selfUser = channel.getJDA().getSelfUser();
        embedBuilder.setAuthor("|   Left the channel & destroyed the audio player due to inactivity!", null, selfUser.getEffectiveAvatarUrl());
        embedBuilder.setColor(Color.PINK);
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
        logger.debug("{} -  Destroyed an audio player due to inactivity.", className);
    }
}
