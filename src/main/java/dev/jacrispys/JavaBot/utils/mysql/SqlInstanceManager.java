package dev.jacrispys.JavaBot.utils.mysql;

import dev.jacrispys.JavaBot.JavaBotMain;
import dev.jacrispys.JavaBot.api.libs.utils.async.AsyncHandler;
import dev.jacrispys.JavaBot.api.libs.utils.async.AsyncHandlerImpl;
import dev.jacrispys.JavaBot.utils.SecretData;
import org.apache.hc.core5.concurrent.CompletedFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SqlInstanceManager extends AsyncHandlerImpl {

    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(SqlInstanceManager.class);
    private static final SqlInstanceManager INSTANCE = new SqlInstanceManager();

    protected SqlInstanceManager() {
        Thread thread = new Thread(this::completeMethod);
        thread.start();
        try {
            SecretData.initLoginInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SqlInstanceManager getInstance() {
        return INSTANCE;
    }

    private Connection getConnection() {
        return this.connection;
    }

    public CompletableFuture<Connection> getConnectionAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Connection> cf = new CompletableFuture<>();
        this.methodQueue.add(new MethodRunner(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    cf.complete(resetConnection("inside_agent_bot"));
                } else cf.complete(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, cf));
        return cf;
    }

    private Connection resetConnection(String dataBase) throws SQLException {
        try {
            String userName = "Jacrispys";
            String db_password = SecretData.getDataBasePass();

            String url = "jdbc:mysql://" + SecretData.getDBHost() + ":3306/" + dataBase + "?autoReconnect=true";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            connection = DriverManager.getConnection(url, userName, db_password);
            return connection;


        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Could not connect to the given database!");
        }
    }

}
