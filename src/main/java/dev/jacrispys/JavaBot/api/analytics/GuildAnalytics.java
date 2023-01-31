package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.GuildStats;
import dev.jacrispys.JavaBot.api.libs.AgentApi;

/**
 * Child of abstract {@link GuildStats}, allows for instantiation,
 * <br> along with validation checks for if a guild is part of the bots scope
 */
public class GuildAnalytics extends GuildStats implements AudioAnalytics, GeneralGuildAnalytics {

    public GuildAnalytics(AgentApi api, long guildId) throws NullPointerException {
        super(guildId, api, api.getConnection().getJDA());
        if(!validateGuild(guildId)) {
            throw new NullPointerException("Could not locate a guild with the given ID!");
        }
    }

    /**
     * Checks if the given guild has been joined by the bot
     * @param guildId to check membership
     * @return true if InsideAgent is a member, false otherwise
     */
    private boolean validateGuild(long guildId) {
        return jda.getGuilds().contains(jda.getGuildById(guildId));
    }

}
