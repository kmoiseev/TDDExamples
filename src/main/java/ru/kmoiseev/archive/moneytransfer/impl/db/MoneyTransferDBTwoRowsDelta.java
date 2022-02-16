package ru.kmoiseev.archive.moneytransfer.impl.db;

import lombok.SneakyThrows;
import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.common.InputValidator;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.ConnectionThreadSafeHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ----- DEPOSIT -----
 *
 * BEGIN
 *  UPDATE amount SET amount = amount + DELTA WHERE id = XXX;
 *  COMMIT;
 * END
 *
 * ----- TRANSFER -----
 *
 * BEGIN
 *  # order updates based on id
 *  UPDATE accounts SET amount = amount - DELTA WHERE id = FROM_ID;
 *  UPDATE accounts SET amount = amount + DELTA WHERE id = TO_ID;
 *
 *  COMMIT;
 * END
 *
 */
public class MoneyTransferDBTwoRowsDelta extends ConnectionThreadSafeHolder implements MoneyTransfer {

    private final InputValidator validator = new InputValidator();

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

        final PreparedStatement statement = getConnection().prepareStatement("UPDATE accounts SET amount = amount + ? where id = ?;");
        statement.setLong(1, amount);
        statement.setString(2, toAccount);
        if (statement.executeUpdate() == 0) {
            return false;
        } else {
            getConnection().commit();
        }

        return true;
    }

    @Override
    public boolean transfer(String fromAccount, String toAccount, Long amount) {
        if (!validator.checkAccountInput(fromAccount) ||
                !validator.checkAccountInput(toAccount) ||
                !validator.checkAmountInput(amount)) {
            return false;
        }

        try {
            final boolean firstLockOnFrom = fromAccount.compareTo(toAccount) > 0;

            if (firstLockOnFrom) {
                updateAmountAtomically(fromAccount, -amount);
                updateAmountAtomically(toAccount, amount);
            } else {
                updateAmountAtomically(toAccount, amount);
                updateAmountAtomically(fromAccount, -amount);
            }

            getConnection().commit();
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        }

        return true;
    }

    private void updateAmountAtomically(final String account, Long delta) throws SQLException {
        final PreparedStatement statement =
                getConnection().prepareStatement("UPDATE accounts SET amount = amount + ? where id = ?;");
        statement.setLong(1, delta);
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
