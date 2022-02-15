package ru.kmoiseev.archive.moneytransfer.impl.db.common;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public abstract class ConnectionThreadSafeHolder {
    private final ThreadLocal<Connection> connection = new ThreadLocal<>();

    @SneakyThrows
    protected Connection getConnection() {
        if (connection.get() == null) {
            connection.set(
                    DriverManager.getConnection(
                            "jdbc:postgresql://localhost:5432/testdb",
                            "testuser",
                            "testpassword"
                    )
            );
            modifyConnection(connection.get());
        }
        return connection.get();
    }

    @SneakyThrows
    protected void modifyConnection(final Connection connection) {
        connection.setAutoCommit(false);
    }

    @SneakyThrows
    public void shutdown() {
        closeConnection();
        getConnection().prepareStatement(
                "drop TABLE accounts;"
        ).execute();
        getConnection().commit();
    }

    @SneakyThrows
    public void closeConnection() {
        if (connection.get() != null) {
            connection.get().close();
            connection.set(null);
        }
    }
}
