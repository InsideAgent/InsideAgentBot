package dev.jacrispys.JavaBot.api.analytics.objects;

import java.sql.SQLException;

/**
 * Parent class to all statistics
 */
public interface Stats {

    /**
     * @return number of total uses
     * @throws SQLException if a database error occurs
     */
    long getTotalUses() throws SQLException;

}
