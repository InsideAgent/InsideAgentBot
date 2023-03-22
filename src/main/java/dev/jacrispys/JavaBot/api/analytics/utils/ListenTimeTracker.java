package dev.jacrispys.JavaBot.api.analytics.utils;

import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.UserStats;
import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Executor Util class to update DB for active time tracking
 */
public class ListenTimeTracker extends ListenerAdapter {

    private final Map<Long, Long> listeningGuilds = new HashMap<>();


    public ListenTimeTracker(JDA jda) {
        initListener(jda);
    }

    /**
     * Initializes a {@link ScheduledExecutorService} that run's async every 5 seconds,
     * <br> checking if any audio players are running, if they are add 5000 MS to {@link UserStats#LISTEN_TIME}
     *
     * @param jda instance to connect to
     */
    protected void initListener(JDA jda) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        executor.scheduleAtFixedRate(() -> {
            for (Long guild : listeningGuilds.keySet()) {
                GuildAudioManager manager = GuildAudioManager.getGuildAudioManager(jda.getGuildById(guild));
                if (manager.audioPlayer.getPlayingTrack() != null) {
                    for (Member member : jda.getGuildById(guild).getVoiceChannelById(listeningGuilds.get(guild)).getMembers()) {
                        if (member.equals(jda.getGuildById(guild).getSelfMember())) continue;
                        MySqlStats stats = MySqlStats.getInstance();
                        stats.incrementUserStat(member, 5000L, UserStats.LISTEN_TIME);
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Override for {@link ListenerAdapter#onGuildVoiceUpdate(GuildVoiceUpdateEvent)}
     * manages {@link ListenTimeTracker#listeningGuilds} for which guilds/users are currently active
     *
     * @param event event to listen to
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() != null && event.getChannelLeft() == null) {
            if (event.getMember().getIdLong() == event.getGuild().getSelfMember().getIdLong()) {
                listeningGuilds.put(event.getGuild().getIdLong(), event.getChannelJoined().getIdLong());
            }
        } else if (event.getChannelJoined() != null && event.getChannelLeft() != null) {
            if (event.getMember().getIdLong() == event.getGuild().getSelfMember().getIdLong()) {
                listeningGuilds.put(event.getGuild().getIdLong(), event.getChannelJoined().getIdLong());
            }
        } else if (event.getChannelLeft() != null && event.getChannelJoined() == null) {
            if (event.getMember().getIdLong() == event.getGuild().getSelfMember().getIdLong()) {
                listeningGuilds.remove(event.getMember().getIdLong());
            }
        }
    }

}
