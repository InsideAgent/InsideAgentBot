package dev.jacrispys.JavaBot.api.analytics.objects;

import java.sql.SQLException;

public interface Stats {

    long getTotalUses() throws SQLException;

}
