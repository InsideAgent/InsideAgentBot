package dev.jacrispys.JavaBot.utils.mysql;

import dev.jacrispys.JavaBot.utils.SecretData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlInstanceManager extends Thread {

    private Connection connection;
    private static SqlInstanceManager INSTANCE;

    protected SqlInstanceManager() {
        INSTANCE = this;
        try {
            resetConnection("inside_agent_bot");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.start();
    }

    public static SqlInstanceManager getInstance() {
        return INSTANCE != null ? INSTANCE : new SqlInstanceManager();
    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!connection.isClosed()) {
                    Thread.sleep(28800000L);
                    return;
                }
                resetConnection("inside_agent_bot");

            } catch (SQLException | InterruptedException ex) {
                try {
                    resetConnection("inside_agent_bot");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void resetConnection(String dataBase) throws SQLException {
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


        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Could not connect to the given database!");
        }
    }

}
