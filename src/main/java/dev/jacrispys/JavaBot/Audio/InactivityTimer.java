package dev.jacrispys.JavaBot.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InactivityTimer extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InactivityTimer.class);

    private static boolean inactivityExpired(long inactiveStart) {
        return Duration.ofMillis(System.currentTimeMillis() - inactiveStart).toMillis() >= Duration.ofMinutes(15).toMillis();
    }

    @SuppressWarnings("all")
    public static void startInactivity(AudioPlayer player, Long guildId, JDA jda) {
        long startMillis = System.currentTimeMillis();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable service = () -> {
            if(player.getPlayingTrack() != null && !player.isPaused() && jda.getGuildById(guildId).getSelfMember().getVoiceState().getChannel().getMembers().size() > 1) {
                executorService.shutdown();
            } else {
                if(inactivityExpired(startMillis)) {
                    try {
                        TextChannel channel = jda.getGuildById(guildId).getTextChannelById(MySQLConnection.getInstance().getMusicChannel(jda.getGuildById(guildId)));
                        assert channel != null;
                        inactivityMessage(channel);
                    }catch (SQLException ignored) {
                    } finally {
                        if(!jda.getGuildById(guildId).getAudioManager().isConnected()) {
                            jda.getGuildById(guildId).getAudioManager().closeAudioConnection();
                        } else  {
                            logger.error("Could not remove disconnected bot from VC!");
                            // Spaghetti ass code only works after the if statement that tells me it doesnt work 💀
                            jda.getGuildById(guildId).getAudioManager().closeAudioConnection();
                        }
                        player.destroy();
                        GuildAudioManager.getGuildAudioManager(jda.getGuildById(guildId)).clearQueue();
                        GuildAudioManager.getGuildAudioManager(jda.getGuildById(guildId)).resumePlayer();
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
                startInactivity(GuildAudioManager.getGuildAudioManager(event.getGuild()).audioPlayer, event.getGuild().getIdLong(), event.getJDA());
            }
        }
    }

    private static void inactivityMessage(TextChannel channel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        User selfUser = channel.getJDA().getSelfUser();
        embedBuilder.setAuthor("|   Left the channel & destroyed the audio player due to inactivity!", null, selfUser.getEffectiveAvatarUrl());
        embedBuilder.setColor(Color.PINK);
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
