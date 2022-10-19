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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ListenTimeTracker extends ListenerAdapter {

    private final Map<Long, Long> listeningGuilds = new HashMap<>();


    public ListenTimeTracker(JDA jda) {
        initListener(jda);
    }

    protected void initListener(JDA jda) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        executor.scheduleAtFixedRate(() -> {
            for (Long guild : listeningGuilds.keySet()) {
                GuildAudioManager manager = GuildAudioManager.getGuildAudioManager(jda.getGuildById(guild));
                if (manager.audioPlayer.getPlayingTrack() != null) {
                    for (Member member : jda.getGuildById(guild).getVoiceChannelById(listeningGuilds.get(guild)).getMembers()) {
                        if(member.equals(jda.getGuildById(guild).getSelfMember())) continue;
                        try {
                            MySqlStats stats = MySqlStats.getInstance();
                            stats.incrementUserStat(member, 5000L, UserStats.LISTEN_TIME);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

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
