package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.GuildStats;
import dev.jacrispys.JavaBot.api.libs.AgentApi;

public class GuildAnalytics extends GuildStats implements AudioAnalytics, GeneralGuildAnalytics {

    public GuildAnalytics(AgentApi api, long guildId) throws NullPointerException {
        super(guildId, api, api.getConnection().getJDA());
        if(!validateGuild(guildId)) {
            throw new NullPointerException("Could not locate a guild with the given ID!");
        }
    }

    private boolean validateGuild(long guildId) {
        return jda.getGuilds().contains(jda.getGuildById(guildId));
    }

}
