package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;
import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.TokenAuth;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;
import dev.jacrispys.JavaBot.api.libs.utils.AgentOptions;
import dev.jacrispys.JavaBot.utils.SecretData;
import dev.jacrispys.JavaBot.utils.mysql.SqlInstanceManager;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.CheckReturnValue;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class AgentApiBuilder {

    protected @Nullable String authToken;
    protected @Nullable long userId;
    protected @Nullable String developerKey;
    protected @Nullable AgentOptions clientOptions;

    private AgentApiBuilder(String authToken) { this.authToken = authToken; }

    @Nonnull
    @CheckReturnValue
    public static AgentApiBuilder createClient(@Nullable String authToken) {
        return new AgentApiBuilder(authToken);
    }

    @Nonnull
    public AgentApiBuilder setUserId(long userId) {
        this.userId = userId;
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

    public AgentApi build() throws AuthorizationException, LoginException, SQLException, InterruptedException, IOException, ExecutionException {
        ClientConnection auth = TokenAuth.authorize(userId, authToken);
        if(auth instanceof DeveloperConnection) {
            return new AgentApiImpl((DeveloperConnection) auth, clientOptions);
        } else return new AgentApiImpl((UserConnection) auth, clientOptions);

    }


}
