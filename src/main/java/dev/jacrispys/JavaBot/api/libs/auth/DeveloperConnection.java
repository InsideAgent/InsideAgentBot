package dev.jacrispys.JavaBot.api.libs.auth;

import net.dv8tion.jda.api.JDA;

import javax.security.auth.login.LoginException;

public class DeveloperConnection extends ClientConnection {

    protected DeveloperConnection() throws LoginException {

    }

    public void testDev() {
        System.out.println("Dev");
    }

}
