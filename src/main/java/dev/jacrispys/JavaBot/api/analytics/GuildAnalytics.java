package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.GuildStats;
import dev.jacrispys.JavaBot.api.analytics.objects.GuildUser;
import dev.jacrispys.JavaBot.api.libs.AgentApi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class GuildAnalytics extends GuildStats implements UserAnalytics, AudioAnalytics, GeneralGuildAnalytics {

    private final long guildId;
    private final AgentApi api;

    private final JDA jda;


    public GuildAnalytics(AgentApi api, long guildId) throws NullPointerException {
        this.api = api;
        jda = api.getConnection().getJda();
        if(validateGuild(guildId)) {
            this.guildId = guildId;
        } else throw new NullPointerException("Could not locate a guild with the given ID!");
    }

    private boolean validateGuild(long guildId) {
        return jda.getGuilds().contains(jda.getGuildById(guildId));
    }

    /**
     * @return
     */
    @Override
    public GuildStats getJoinDate() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<Member> getMembers() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public List<GuildUser> getBotUsers() {
        return null;
    }

    /**
     * @param frequency
     * @return
     */
    @Override
    public List<GuildUser> getRegularUsers(int frequency) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GuildStats getOverallStats() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public long getTotalUses() {
        return -1;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public GuildUser getGuildUser(User user) {
        return null;
    }

}
