package dev.jacrispys.JavaBot.api.libs.auth;

import javax.security.auth.login.LoginException;

/**
 * Connection granted to most user's when using the {@link dev.jacrispys.JavaBot.api.libs.AgentApi}
 */
public class UserConnection extends ClientConnection {


    protected UserConnection() throws InterruptedException {
        super();

    }
}
