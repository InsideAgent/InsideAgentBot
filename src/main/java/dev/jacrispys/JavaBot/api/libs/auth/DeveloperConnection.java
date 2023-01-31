package dev.jacrispys.JavaBot.api.libs.auth;

import net.dv8tion.jda.api.JDA;

import javax.security.auth.login.LoginException;

/**
 * Child of {@link ClientConnection} should be instantiated if user's auth token identifies as a developer token.
 */
public class DeveloperConnection extends ClientConnection {

    protected DeveloperConnection() throws InterruptedException {
        super();
    }

    /**
     * Test function to see if developer token grants access to developer methods.
     */
    public void testDev() {
        System.out.println("Dev");
    }

}
