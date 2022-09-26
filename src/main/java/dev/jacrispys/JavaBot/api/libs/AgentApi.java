package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.analytics.GuildAnalytics;
import dev.jacrispys.JavaBot.api.analytics.objects.JdaUser;
import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;

public interface AgentApi {

    UserConnection getUserConnection();
    DeveloperConnection getDevConnection();

    ClientConnection getConnection();

    boolean isDevAccount();

    GuildAnalytics getGuildAnalytics(AgentApi api, long guildId);

    JdaUser getJdaUser(long userId);

    // TODO: 8/31/2022 Add Methods to check data and perform changes on the bot. 
}
