package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;
import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.TokenAuth;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;
import dev.jacrispys.JavaBot.api.libs.utils.AgentOptions;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

public class AgentApiBuilder {

    protected @Nullable String authToken;
    protected @Nullable String clientSecret;
    protected @Nullable String developerKey;
    protected @Nullable AgentOptions clientOptions;

    private AgentApiBuilder(String authToken) { this.authToken = authToken; }

    @Nonnull
    @CheckReturnValue
    public static AgentApiBuilder createClient(@Nullable String authToken) {
        return new AgentApiBuilder(authToken);
    }

    @Nonnull
    public AgentApiBuilder setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Nonnull
    public AgentApiBuilder setDeveloperKey(String developerKey) {
        this.developerKey = developerKey;
        return this;
    }

    @Nonnull
    public AgentApiBuilder setOptions(AgentOptions options) {
        this.clientOptions = options;
        return this;
    }

    public AgentApi build() throws AuthorizationException, LoginException {
        ClientConnection auth = TokenAuth.authorize(authToken, clientSecret, developerKey);
        if(auth instanceof DeveloperConnection) {
            return new AgentApiImpl((DeveloperConnection) auth, clientOptions);
        } else return new AgentApiImpl((UserConnection) auth, clientOptions);

    }


}
