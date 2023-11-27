package dev.jacrispys.JavaBot.utils.mysql;

import dev.jacrispys.JavaBot.api.libs.utils.async.AsyncHandlerImpl;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.utils.SecretData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Allows for async retrieval of a MySQL Connection instance
 */
public class SqlInstanceManager extends AsyncHandlerImpl {

    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(SqlInstanceManager.class);
    private static final SqlInstanceManager INSTANCE = new SqlInstanceManager();

    private final Thread thread = new Thread(this::completeMethod);
    protected SqlInstanceManager() {
        thread.start();
        provideConnection();
        try {
            SecretData.initLoginInfo();
        } catch (IOException e) {
            logger.warn("{} - IOException occurred while creating SQL instance.", getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    public static SqlInstanceManager getInstance() {
        return INSTANCE;
    }

    private Connection getConnection() {
        return this.connection;
    }

    public Thread getConnectionThread() {
        return this.thread;
    }

    private void provideConnection() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    this.connection = SqlInstanceManager.getInstance().getConnectionAsync().get();
                    MySqlStats.getInstance().obtainConnection(this.connection);
                    MySQLConnection.getInstance().obtainConnection(this.connection);
                    Thread.sleep(3600 * 1000);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("{} - Error occurred in thread while obtaining SQL connection. \n" + e.getMessage(), getClass().getSimpleName());
                }
            }
        });
        thread.start();
    }

    /**
     * @return a completed future with the connection object inside
     */
    public CompletableFuture<Connection> getConnectionAsync() {
        CompletableFuture<Connection> cf = new CompletableFuture<>();
        this.methodQueue.add(new MethodRunner(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    cf.complete(resetConnection("inside_agent_bot"));
                } else cf.complete(connection);
            } catch (SQLException e) {
                logger.error("{} - Error occurred in thread while obtaining SQL connection. \n" + e.getMessage(), getClass().getSimpleName());
                throw new RuntimeException(e);
            }
        }, cf));
        return cf;
    }

    /**
     * Closes existing connections and creates a new one
     * @param dataBase database within mysql to target
     * @return Connection to the database
     * @throws SQLException if a database error occurs
     */
    private Connection resetConnection(String dataBase) throws SQLException {
        if (this.connection == null || !connection.isValid(10)) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                String userName = "Jacrispy";
                String db_password = SecretData.getDataBasePass();

                String url = "jdbc:mariadb://" + SecretData.getDBHost() + ":3306/" + dataBase + "?autoReconnect=true";
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                connection = DriverManager.getConnection(url, userName, db_password);
                return connection;


            } catch (SQLException e) {
                logger.error("{} - Error occurred in thread while obtaining SQL connection. \n" + e.getMessage(), getClass().getSimpleName());
                throw new SQLException("Could not connect to the given database!");
            }
        }
        return this.connection;
    }

}
