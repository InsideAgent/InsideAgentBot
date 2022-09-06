package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.GuildUser;

public class GuildAnalytics implements UserAnalytics, AudioAnalytics, GeneralGuildAnalytics{
    /**
     * @return
     */
    @Override
    public dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics getJoinDate() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics getMembers() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics getBotUsers() {
        return null;
    }

    /**
     * @param frequency
     * @return
     */
    @Override
    public dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics getRegularUsers(int frequency) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics getOverallStats() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics getTotalUses() {
        return null;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public dev.jacrispys.JavaBot.api.analytics.objects.GuildAnalytics getUser(GuildUser user) {
        return null;
    }
}
