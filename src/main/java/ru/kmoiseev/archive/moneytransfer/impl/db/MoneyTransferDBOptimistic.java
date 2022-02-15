package ru.kmoiseev.archive.moneytransfer.impl.db;

import lombok.SneakyThrows;
import org.postgresql.util.PSQLException;
import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.common.InputValidator;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.ConnectionThreadSafeHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Objects.isNull;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferDBOptimistic extends ConnectionThreadSafeHolder implements MoneyTransfer {

    private final InputValidator validator = new InputValidator();

    @SneakyThrows
    @Override
    protected void modifyConnection(Connection connection) {
        super.modifyConnection(connection);

        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }

    @SneakyThrows
    private boolean checkAccountExists(final String account) {

        final PreparedStatement statement = getConnection().prepareStatement("select count(*) from accounts where id=?;");
        statement.setString(1, account);
        final ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        final boolean exists = resultSet.getInt(1) == 1;
        resultSet.close();

        return exists;
    }

    @SneakyThrows
    private Long selectAccountAmountForUpdate(final String account) {
        final PreparedStatement statement = getConnection().prepareStatement("select amount from accounts where id=? for update;");
        statement.setString(1, account);
        final ResultSet resultSet = statement.executeQuery();
        if (!resultSet.next()) {
            return null;
        }
        return resultSet.getLong(1);
    }

    @SneakyThrows
    @Override
    public boolean createAccount(String account) {
        if (!validator.checkAccountInput(account) || checkAccountExists(account)) {
            return false;
        }

        final PreparedStatement statement = getConnection().prepareStatement("INSERT INTO accounts VALUES(?, ?);\n");

        statement.setString(1, account);
        statement.setLong(2, 0L);

        statement.executeUpdate();

        getConnection().commit();

        return true;
    }

    @SneakyThrows
    @Override
    public boolean deposit(String toAccount, Long amount) {
        if (!validator.checkAccountInput(toAccount) ||
                !validator.checkAmountInput(amount)) {
            return false;
        }

        while (true) {
            try {
                final Long amountBeforeDeposit = selectAccountAmountForUpdate(toAccount);
                if (amountBeforeDeposit == null) {
                    return false;
                }

                setAmountRetryable(toAccount, amountBeforeDeposit + amount);

                getConnection().commit();

                return true;
            } catch (PSQLException e) {
                getConnection().rollback();
                e.printStackTrace();
            } catch (SQLException e) {
                getConnection().rollback();
                return false;
            }
        }
    }

    @SneakyThrows
    @Override
    public boolean transfer(String fromAccount, String toAccount, Long amount) {
        if (!validator.checkAccountInput(fromAccount) ||
                !validator.checkAccountInput(toAccount) ||
                !validator.checkAmountInput(amount)) {
            return false;
        }



        while (true) {
            try {
                final boolean firstLockOnFrom = fromAccount.compareTo(toAccount) > 0;

                final Long amountOnFrom;
                final Long amountOnTo;

                if (firstLockOnFrom) {
                    amountOnFrom = selectAccountAmountForUpdate(fromAccount);
                    amountOnTo = selectAccountAmountForUpdate(toAccount);
                } else {
                    amountOnTo = selectAccountAmountForUpdate(toAccount);
                    amountOnFrom = selectAccountAmountForUpdate(fromAccount);
                }

                if (amountOnFrom == null || amountOnTo == null || amountOnFrom < amount) {
                    getConnection().rollback();
                    return false;
                }

                if (firstLockOnFrom) {
                    setAmountRetryable(fromAccount, amountOnFrom - amount);
                    setAmountRetryable(toAccount, amountOnTo + amount);
                } else {
                    setAmountRetryable(toAccount, amountOnTo + amount);
                    setAmountRetryable(fromAccount, amountOnFrom - amount);
                }
                getConnection().commit();

                return true;
            } catch (PSQLException e) {
                getConnection().rollback();
                e.printStackTrace();
            } catch (SQLException e) {
                try {
                    e.printStackTrace();
                    getConnection().rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                return false;
            }
        }
    }

    private void setAmountRetryable(final String account, Long newValue) throws SQLException {
        final PreparedStatement statement =
                getConnection().prepareStatement("UPDATE accounts SET amount = ? where id = ?;");
        statement.setLong(1, newValue);
        statement.setString(2, account);
        if (statement.executeUpdate() == 0) {
            throw new SQLException("No row updated");
        }
    }

    @SneakyThrows
    @Override
    public Long getAmount(String account) {
        if (!validator.checkAccountInput(account)) {
            return null;
        }

        final PreparedStatement statement = getConnection().prepareStatement("SELECT amount from accounts WHERE id = ?");

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
    @Override
    public void start() {
        getConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS accounts(" +
                        "id varchar(256) primary key," +
                        "amount int not null check ( amount >= 0 )" +
                        ");"
        ).execute();
        getConnection().commit();
    }
}
