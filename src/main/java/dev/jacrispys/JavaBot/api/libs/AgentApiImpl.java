package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.analytics.GuildAnalytics;
import dev.jacrispys.JavaBot.api.analytics.objects.JdaUser;
import dev.jacrispys.JavaBot.api.analytics.objects.JdaUserImpl;
import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;
import dev.jacrispys.JavaBot.api.libs.utils.AgentOptions;

/**
 * Implementation of all methods from {@link AgentApi}
 */
public class AgentApiImpl implements AgentApi {

    private UserConnection connection = null;
    private DeveloperConnection devConnection = null;
    private final AgentOptions clientOptions;

    private boolean devToken = false;

    public AgentApiImpl(UserConnection connection, AgentOptions clientOptions) {
        this.connection = connection;
        this.clientOptions = clientOptions;
    }

    public AgentApiImpl(DeveloperConnection connection, AgentOptions clientOptions) {
        this.devConnection = connection;
        this.clientOptions = clientOptions;
        this.devToken = true;
    }

    @Override
    public UserConnection getUserConnection() throws NullPointerException {
        if(connection == null) throw new NullPointerException("Cannot obtain user connection when using a dev key, Idiot.");
        return connection;
    }

    @Override
    public DeveloperConnection getDevConnection() {
        if(devConnection == null) throw new NullPointerException("Cannot obtain developer connection without dev key!");
        return devConnection;
    }

    @Override
    public ClientConnection getConnection() {
        return connection != null ? connection : devConnection;
    }

    @Override
    public boolean isDevAccount() {
        return devToken;
    }

    @Override
    public GuildAnalytics getGuildAnalytics(AgentApi api, long guildId) {
        return new GuildAnalytics(api, guildId);
    }

    @Override
    public JdaUser getJdaUser(long userId) {
        return new JdaUserImpl(getConnection().getJDA(), userId);
    }
}
