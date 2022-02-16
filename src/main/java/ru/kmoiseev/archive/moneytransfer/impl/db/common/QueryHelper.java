package ru.kmoiseev.archive.moneytransfer.impl.db.common;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author konstantinmoiseev
 * @since 16.02.2022
 */
@RequiredArgsConstructor
public class QueryHelper {

    private final ConnectionThreadSafeHolder holder;

    @SneakyThrows
    public Long selectAmount(final String account) {
        final PreparedStatement statement = holder.getConnection()
                .prepareStatement("SELECT amount from accounts WHERE id = ?");
        statement.setString(1, account);

        final ResultSet resultSet = statement.executeQuery();
        if (!resultSet.next()) {
            return null;
        }
        final long result = resultSet.getLong(1);
        resultSet.close();

        return result;
    }

    @SneakyThrows
    public Long selectAmountForUpdate(final String account) {
        final PreparedStatement statement = holder.getConnection().prepareStatement("select amount from accounts where id=? for update;");
        statement.setString(1, account);
        final ResultSet resultSet = statement.executeQuery();
        if (!resultSet.next()) {
            return null;
        }
        return resultSet.getLong(1);
    }

    @SneakyThrows
    public boolean checkAccountExists(final String account) {

        final PreparedStatement statement = holder.getConnection()
                .prepareStatement("select count(*) from accounts where id=?;");
        statement.setString(1, account);

        final ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        final boolean exists = resultSet.getInt(1) == 1;
        resultSet.close();

        return exists;
    }

    @SneakyThrows
    public void createAccount(final String account) {

        final PreparedStatement statement = holder.getConnection()
                .prepareStatement("INSERT INTO accounts VALUES(?, ?);\n");
        statement.setString(1, account);
        statement.setLong(2, 0L);

        statement.executeUpdate();
    }

    @SneakyThrows
    public boolean updateAmountWithDelta(final String account, Long delta) {
        final PreparedStatement statement =
                holder.getConnection().prepareStatement("UPDATE accounts SET amount = amount + ? where id = ?;");
        statement.setLong(1, delta);
        statement.setString(2, account);

        return statement.executeUpdate() != 0;
    }

    @SneakyThrows
    public boolean updateAmount(final String account, Long newAmount) {
        final PreparedStatement statement =
                holder.getConnection().prepareStatement("UPDATE accounts SET amount = ? where id = ?;");
        statement.setLong(1, newAmount);
        statement.setString(2, account);

        return statement.executeUpdate() != 0;
    }

    @SneakyThrows
    public boolean updateAmountOptimistic(final String account, final Long oldAmount, final Long newAmount) {
        final PreparedStatement statement = holder.getConnection()
                        .prepareStatement("UPDATE accounts SET amount = ? where id = ? and amount = ?;");
        statement.setLong(1, newAmount);
        statement.setString(2, account);
        statement.setLong(3, oldAmount);

        return statement.executeUpdate() != 0;
    }
}
