package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics;
import dev.jacrispys.JavaBot.api.analytics.objects.GuildUser;
import net.dv8tion.jda.api.entities.User;

public interface GeneralGuildAnalytics {

    GuildAnalytics getJoinDate();

    GuildAnalytics getMembers();
    GuildAnalytics getBotUsers();

    GuildAnalytics getRegularUsers(int frequency);

    GuildAnalytics getOverallStats();

    GuildAnalytics getTotalUses();

    GuildAnalytics getUser(GuildUser user);

}
