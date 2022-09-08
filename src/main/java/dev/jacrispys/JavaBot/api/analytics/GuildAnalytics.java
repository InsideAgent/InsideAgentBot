package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.GuildStats;
import dev.jacrispys.JavaBot.api.analytics.objects.GuildUser;
import dev.jacrispys.JavaBot.api.libs.AgentApi;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class GuildAnalytics extends GuildStats implements AudioAnalytics, GeneralGuildAnalytics {

    private final long guildId;
    private final AgentApi api;


    public GuildAnalytics(AgentApi api, long guildId) throws NullPointerException {
        super(guildId, api, api.getConnection().getJda());
        this.api = api;
        if(validateGuild(guildId)) {
            this.guildId = guildId;
        } else throw new NullPointerException("Could not locate a guild with the given ID!");
    }

    private boolean validateGuild(long guildId) {
        return jda.getGuilds().contains(jda.getGuildById(guildId));
    }

}
