package dev.jacrispys.JavaBot.api.exceptions;

/**
 * Exception thrown when a login attempt fails within {@link dev.jacrispys.JavaBot.api.libs.AgentApi}
 */
public class AuthorizationException extends Exception {
    public AuthorizationException() {
        super();
    }

    public AuthorizationException(String s) {
        super(s);
    }
}
