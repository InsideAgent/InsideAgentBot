package dev.jacrispys.JavaBot.api.libs.auth;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

public class TokenAuth {


    protected TokenAuth() {
    }

    public static <T extends ClientConnection> T authorize(String authToken, String clientSecret, @Nullable String developerToken) throws AuthorizationException, LoginException {
        return new TokenAuth().createConnection(authToken, clientSecret, developerToken);
    }

    @SuppressWarnings("unchecked")
    protected <T extends ClientConnection> T createConnection(String authToken, String clientSecret, String developerToken) throws AuthorizationException, LoginException {
        if (validateAuth(authToken, clientSecret, developerToken)) {
            return (T) new DeveloperConnection();
        } else return (T) new UserConnection();
    }

    protected boolean validateAuth(String authToken, String clientSecret, @Nullable String developerToken) throws AuthorizationException {

        if(
        authorizeToken(authToken) &&
        authorizeClient(clientSecret)
        ) {
            if(developerToken == null) return false;
            if(authorizeDevToken(developerToken)) return true;
            throw new AuthorizationException("Could not verify developer token!");
        } else throw new AuthorizationException("Could not verify either authToken or clientSecret!");

    }

    // TODO: 9/15/2022 Add in-app authorization 

    protected boolean authorizeToken(String authToken) {
        return false;
    }

    protected boolean authorizeClient(String clientSecret) {
        return false;
    }

    protected boolean authorizeDevToken(@Nonnull String devToken) {
        return false;
    }

}
