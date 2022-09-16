package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;
import dev.jacrispys.JavaBot.api.libs.utils.AgentOptions;

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


    /**
     * @return
     */
    @Override
    public UserConnection getUserConnection() throws NullPointerException {
        if(connection == null) throw new NullPointerException("Cannot obtain user connection when using a dev key, Idiot.");
        return connection;
    }

    /**
     * @return
     */
    @Override
    public DeveloperConnection getDevConnection() {
        if(devConnection == null) throw new NullPointerException("Cannot obtain developer connection without dev key!");
        return devConnection;
    }

    /**
     * @return
     */
    @Override
    public ClientConnection getConnection() {
        return connection != null ? connection : devConnection;
    }

    /**
     * @return
     */
    @Override
    public boolean isDevAccount() {
        return devToken;
    }
}
