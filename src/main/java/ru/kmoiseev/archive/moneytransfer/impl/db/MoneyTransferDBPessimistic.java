package ru.kmoiseev.archive.moneytransfer.impl.db;

import lombok.SneakyThrows;
import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.common.InputValidator;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.ConnectionThreadSafeHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferDBPessimistic extends ConnectionThreadSafeHolder implements MoneyTransfer {

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

        final Long amountBeforeDeposit = selectAccountAmountForUpdate(toAccount);
        if (amountBeforeDeposit == null) {
            return false;
        }

        final PreparedStatement statement = getConnection().prepareStatement("UPDATE accounts SET amount = ? where id = ?;");
        statement.setLong(1, amountBeforeDeposit + amount);
        statement.setString(2, toAccount);
        statement.executeUpdate();

        getConnection().commit();

        return true;
    }

    @SneakyThrows
    @Override
    public boolean transfer(String fromAccount, String toAccount, Long amount) {
        if (!validator.checkAccountInput(fromAccount) ||
                !validator.checkAccountInput(toAccount) ||
                !validator.checkAmountInput(amount)) {
            return false;
        }


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

        {
            final PreparedStatement statementToUpdateFrom =
                    getConnection().prepareStatement("UPDATE accounts SET amount = ? where id = ?;");
            statementToUpdateFrom.setLong(1, amountOnFrom - amount);
            statementToUpdateFrom.setString(2, fromAccount);
            statementToUpdateFrom.executeUpdate();
        }

        {
            final PreparedStatement statementToUpdateTo =
                    getConnection().prepareStatement("UPDATE accounts SET amount = amount + ? where id = ?;");
            statementToUpdateTo.setLong(1, amount);
            statementToUpdateTo.setString(2, toAccount);
            statementToUpdateTo.executeUpdate();
        }

        getConnection().commit();

        return true;
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
                        "amount bigint not null" +
                        ");"
        ).execute();
        getConnection().commit();
    }

    @SneakyThrows
    @Override
    public void shutdown() {
        getConnection().prepareStatement(
                "drop TABLE accounts;"
        ).execute();
        getConnection().commit();
    }
}
