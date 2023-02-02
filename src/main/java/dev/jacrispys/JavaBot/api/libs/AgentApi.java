package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.analytics.GuildAnalytics;
import dev.jacrispys.JavaBot.api.analytics.objects.JdaUser;
import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;

/**
 * Main class of the API, all instances will be through this class.
 */
public interface AgentApi {

    /**
     * Obtain a connection specific to general users
     * @return Connection
     */
    UserConnection getUserConnection();

    /**
     * Obtain a connection specific to developers.
     * @return Connection
     */
    DeveloperConnection getDevConnection();

    /**
     * Obtain a general connection to the API, should not be used.
     * @return general connection
     */
    ClientConnection getConnection();

    /**
     * @return true if the token used to validate the connection has dev permissions.
     */
    boolean isDevAccount();

    /**
     * @param api instance for GuildAnalytics
     * @param guildId guild to obtain analytics for
     * @return an instance of guild analytics for the given guild
     */
    GuildAnalytics getGuildAnalytics(AgentApi api, long guildId);

    /**
     * @param userId Takes the id from a {@link net.dv8tion.jda.api.entities.User}
     * @return an instance of JdaUser after being converted from an ID
     */
    JdaUser getJdaUser(long userId);

    // TODO: 8/31/2022 Add Methods to check data and perform changes on the bot. 
}
