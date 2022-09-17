package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.GuildStats;
import dev.jacrispys.JavaBot.api.libs.AgentApi;

public class GuildAnalytics extends GuildStats implements AudioAnalytics, GeneralGuildAnalytics {

    private final long guildId;
    private final AgentApi api;


    public GuildAnalytics(AgentApi api, long guildId) throws NullPointerException {
        super(guildId, api, api.getConnection().getJDA());
        this.api = api;
        if(validateGuild(guildId)) {
            this.guildId = guildId;
        } else throw new NullPointerException("Could not locate a guild with the given ID!");
    }

    private boolean validateGuild(long guildId) {
        return jda.getGuilds().contains(jda.getGuildById(guildId));
    }

}
