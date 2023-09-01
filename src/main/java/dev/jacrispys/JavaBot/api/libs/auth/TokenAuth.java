package dev.jacrispys.JavaBot.api.libs.auth;

import dev.jacrispys.JavaBot.api.exceptions.AuthorizationException;
import dev.jacrispys.JavaBot.utils.mysql.SqlInstanceManager;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.JDA;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Grants authorization to the {@link dev.jacrispys.JavaBot.api.libs.AgentApi} and gives access based on the token
 */
public class TokenAuth {

    volatile Connection connection = SqlInstanceManager.getInstance().getConnectionAsync().get();

    protected TokenAuth() throws ExecutionException, InterruptedException {
    }

    private Connection getConnection() throws SQLException {
        if (this.connection == null || !connection.isValid(10)) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                this.connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
            } catch (InterruptedException | ExecutionException | SQLException e) {
                e.printStackTrace();
            }
        }
        return this.connection;
    }

    /**
     *
     * @param userId user to authenticate with
     * @param authToken token to validate through the DataBase
     * @return either {@link DeveloperConnection} or {@link UserConnection}
     * @param <T> generic parameter to allow any Child of {@link ClientConnection} to be returned
     * @throws AuthorizationException if the token in not valid
     * @throws InterruptedException if {@link ClientConnection}'s call to {@link JDA#awaitReady()} fails
     * @throws ExecutionException if obtaining a {@link SqlInstanceManager#getInstance()} call fails
     */
    public static <T extends ClientConnection> T authorize(long userId, String authToken) throws AuthorizationException, InterruptedException, ExecutionException {
        return new TokenAuth().createConnection(userId, authToken);
    }

    /**
     * Protected method to create a instance/connection to {@link dev.jacrispys.JavaBot.api.libs.AgentApi}
     * @param userId user to authenticate with
     * @param authToken token to validate through the DataBase
     * @return either {@link DeveloperConnection} or {@link UserConnection}
     * @param <T> generic parameter to allow any Child of {@link ClientConnection} to be returned
     * @throws AuthorizationException if the token in not valid
     * @throws InterruptedException if {@link ClientConnection}'s call to {@link JDA#awaitReady()} fails
     */
    @SuppressWarnings("unchecked")
    protected <T extends ClientConnection> T createConnection(long userId, String authToken) throws AuthorizationException, InterruptedException {
        if (validateAuth(userId, authToken)) {
            return (T) new DeveloperConnection();
        } else return (T) new UserConnection();
    }

    /**
     * Calls {@link TokenAuth#authorizeToken(long, String)} to search the database for a token
     * @param userId user to authenticate with
     * @param authToken token to validate through the DataBase
     * @return true if the token is valid, false otherwise
     * @throws AuthorizationException if the token in not valid
     */
    protected boolean validateAuth(long userId, String authToken) throws AuthorizationException {

        if (authorizeToken(userId, authToken)) {
            return authorizeDevToken(userId, authToken);
        } else throw new AuthorizationException("Could not verify your auth token!");

    }

    /**
     * Executes a DB query for the provided userId and authToken to see if a entry exists
     * @param userId user to authenticate with
     * @param authToken token to validate through the DataBase
     * @return true if the token and user exist in the DB, false otherwise
     */
    protected boolean authorizeToken(long userId, String authToken) {
        try (Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

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


    /**
     * Clone of {@link TokenAuth#authorizeToken(long, String)}, but has an additional check for a "dev_auth" boolean in the DB
     * @param userId user to authenticate with
     * @param devToken token to validate through the DataBase
     * @return true if the token and user exist in the DB along with a True value for "dev_auth", false otherwise
     */
    protected boolean authorizeDevToken(long userId, @Nonnull String devToken) {
        try {
            Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
