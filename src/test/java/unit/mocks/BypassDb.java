package unit.mocks;

import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import org.mockito.Mockito;

import java.sql.ResultSet;

public class BypassDb {

    public static MySQLConnection mockSqlConnection() {
        MySQLConnection connection = Mockito.mock(MySQLConnection.class);
        try {
            Mockito.when(connection.registerGuild(Mockito.any(), Mockito.any())).thenAnswer(invocationOnMock -> true);
            Mockito.when(connection.getMusicChannel(Mockito.any())).thenAnswer(invocationOnMock -> 0L);
            Mockito.when(connection.queryCommand(Mockito.anyString())).thenAnswer(invocationOnMock -> mockResultSet());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static ResultSet mockResultSet() {
        ResultSet set = Mockito.mock(ResultSet.class);

        Mockito.doAnswer(invocationOnMock -> null).when(set);

        return set;
    }
}
