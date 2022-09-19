package dev.jacrispys.JavaBot.api.libs.auth;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class TokenAuth {

    private Connection connection = MySQLConnection.getInstance().getConnection("inside_agent_bot");

    protected TokenAuth() throws SQLException {
    }

    public static <T extends ClientConnection> T authorize(long userId, String authToken) throws AuthorizationException, LoginException, SQLException {
        return new TokenAuth().createConnection(userId, authToken);
    }

    @SuppressWarnings("unchecked")
    protected <T extends ClientConnection> T createConnection(long userId, String authToken) throws AuthorizationException, LoginException {
        if (validateAuth(userId, authToken)) {
            return (T) new DeveloperConnection();
        } else return (T) new UserConnection();
    }

    protected boolean validateAuth(long userId, String authToken) throws AuthorizationException {

        if (authorizeToken(userId, authToken)) {
            if (authorizeDevToken(userId, authToken)) return true;
            return false;
        } else throw new AuthorizationException("Could not verify your auth token!");

    }

    protected boolean authorizeToken(long userId, String authToken) {
        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            ResultSet rs = statement.executeQuery("SELECT token from api_auth WHERE user_id=" + userId);

            rs.beforeFirst();
            rs.next();
            String token = rs.getString("token");
            statement.close();
            return Objects.equals(authToken, token);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    protected boolean authorizeDevToken(long userId, @Nonnull String devToken) throws AuthorizationException {
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery("SELECT token, dev_auth from api_auth WHERE user_id=" + userId);
            rs.beforeFirst();
            rs.next();
            String token = rs.getString("token");
            boolean dev = rs.getBoolean("dev_auth");
            statement.close();
            return Objects.equals(devToken, token) && dev;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
