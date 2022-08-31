package dev.jacrispys.JavaBot.api.libs.auth;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TokenAuth {


    protected TokenAuth() {
    }

    public static ClientConnection authorize(String authToken, String clientSecret, @Nullable String developerToken) throws AuthorizationException {
        return new TokenAuth().createConnection(authToken, clientSecret, developerToken);
    }

    protected ClientConnection createConnection(String authToken, String clientSecret, String developerToken) throws AuthorizationException {
        if (validateAuth(authToken, clientSecret, developerToken)) {
            return new DeveloperConnection();
        } else return new UserConnection();
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
