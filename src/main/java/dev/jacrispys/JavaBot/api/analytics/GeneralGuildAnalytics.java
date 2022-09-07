package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.AudioUser;
import dev.jacrispys.JavaBot.api.analytics.objects.GuildStats;
import dev.jacrispys.JavaBot.api.analytics.objects.GuildUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public interface GeneralGuildAnalytics {

    GuildStats getJoinDate();

    List<Member> getMembers();
    List<GuildUser> getBotUsers();

    List<GuildUser> getRegularUsers(int frequency);

    GuildStats getOverallStats();

    long getTotalUses();

    GuildUser getGuildUser(User user);


}
