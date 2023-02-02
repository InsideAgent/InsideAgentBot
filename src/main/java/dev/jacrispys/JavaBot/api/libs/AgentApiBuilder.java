package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;
import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.TokenAuth;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;
import dev.jacrispys.JavaBot.api.libs.utils.AgentOptions;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.CheckReturnValue;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * Builds a {@link AgentApi} instance to interact with the API.
 */
public class AgentApiBuilder {

    protected @Nullable String authToken;
    protected @Nullable long userId;
    protected @Nullable String developerKey;
    protected @Nullable AgentOptions clientOptions;

    private AgentApiBuilder(String authToken) { this.authToken = authToken; }

    /**
     * @param authToken token to authenticate to the server with
     * @return new instance of the class to allow setting other variables.
     */
    @Nonnull
    @CheckReturnValue
    public static AgentApiBuilder createClient(@Nullable String authToken) {
        return new AgentApiBuilder(authToken);
    }

    /**
     * @param userId Discord user ID to link the User to the API
     * @return current instance of the class to allow setting of other variables.
     */
    @Nonnull
    public AgentApiBuilder setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    /**
     * @param developerKey alternate token that verifies credentials for developer access
     * @return current instance of the class to allow setting of other variables.
     */
    @Nonnull
    public AgentApiBuilder setDeveloperKey(String developerKey) {
        this.developerKey = developerKey;
        return this;
    }

    /**
     * @param options advanced configuration options for the API (WIP)
     * @return current instance of the class to allow setting of other variables.
     */
    @Nonnull
    public AgentApiBuilder setOptions(AgentOptions options) {
        this.clientOptions = options;
        return this;
    }

    /**
     * Builds all previous configurations into one instance of the API
     * @return instance of the API
     * @throws AuthorizationException if the user's token is not valid
     * @throws LoginException if logging into DiscordAPI fails
     * @throws SQLException if a database error occurs
     * @throws InterruptedException if logging into the database fails
     * @throws ExecutionException if obtaining a SqlInstanceManager fails.
     */
    public AgentApi build() throws AuthorizationException, LoginException, SQLException, InterruptedException, ExecutionException {
        ClientConnection auth = TokenAuth.authorize(userId, authToken);
        if(auth instanceof DeveloperConnection) {
            return new AgentApiImpl((DeveloperConnection) auth, clientOptions);
        } else return new AgentApiImpl((UserConnection) auth, clientOptions);

    }


}
