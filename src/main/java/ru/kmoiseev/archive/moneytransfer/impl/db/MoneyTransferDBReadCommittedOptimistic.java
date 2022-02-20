package ru.kmoiseev.archive.moneytransfer.impl.db;

import lombok.SneakyThrows;
import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.AmountWithVersion;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.InputValidator;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.ConnectionThreadSafeHolder;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.QueryHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * begin
 * UPDATE msdmsd;fs,lf's
 * commit;
 * end;
 * <p>
 * ----- DEPOSIT -----
 * <p>
 * BEGIN
 * SELECT amount, version FROM accounts WHERE id = XXX;
 * UPDATE amount SET amount = NEW_AMOUNT, version = NEW_VERSION WHERE id = XXX AND version = OLD_VERSION;
 * -- ROLLBACK + RETRY IF NOTHING HAS BEEN UPDATED !
 * COMMIT;
 * <p>
 * ----- TRANSFER -----
 * <p>
 * BEGIN
 * <p>
 * SELECT version, amount FROM accounts WHERE id = FROM_ID;
 * SELECT version, amount FROM accounts WHERE id = TO_ID;
 * <p>
 * # order updates based on id
 * UPDATE accounts SET amount = NEW_AMOUNT_FROM, version = NEW_VERSION_FROM WHERE id = FROM_ID and version = PREVIOUS_VERSION_FROM;
 * UPDATE accounts SET amount = NEW_AMOUNT_TO, version = NEW_VERSION_TO WHERE id = TO_ID and version = PREVIOUS_VERSION_TO;
 * <p>
 * -- ROLLBACK IF BOTH ROWS HAVE NOT BEEN UPDATED
 * <p>
 * COMMIT;
 */
public class MoneyTransferDBReadCommittedOptimistic extends ConnectionThreadSafeHolder implements MoneyTransfer {

    private final InputValidator validator = new InputValidator();
    private final QueryHelper queryHelper = new QueryHelper(this);

    @SneakyThrows
    @Override
    protected void modifyConnection(Connection connection) {
        super.modifyConnection(connection);

        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    @SneakyThrows
    @Override
    public boolean createAccount(String account) {
        if (!validator.checkAccountInput(account) ||
                queryHelper.checkAccountExists(account)) {
            return false;
        }

        queryHelper.createAccount(account);

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
            final Long amountBefore = queryHelper.selectAmount(toAccount);
            if (amountBefore == null) {
                return false;
            }

            if (queryHelper.updateAmountOptimistic(
                    toAccount, amountBefore, amountBefore + amount)) {
                getConnection().commit();
                return true;
            } else {
                // getConnection().rollback();
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
            final Long amountFrom = queryHelper.selectAmount(fromAccount);
            final Long amountTo = queryHelper.selectAmount(toAccount);

            if (amountFrom == null ||
                    amountTo == null ||
                    amountFrom < amount) {
                // getConnection().rollback();
                return false;
            }

            final boolean fromUpdated;
            final boolean toUpdated;

            final boolean firstLockOnFrom = fromAccount.compareTo(toAccount) > 0;

            if (firstLockOnFrom) {
                fromUpdated = queryHelper.updateAmountOptimistic(fromAccount,
                        amountFrom,
                        amountFrom - amount);

                toUpdated = queryHelper.updateAmountOptimistic(toAccount,
                        amountTo,
                        amountTo + amount);
            } else {
                toUpdated = queryHelper.updateAmountOptimistic(toAccount,
                        amountTo,
                        amountTo + amount);

                fromUpdated = queryHelper.updateAmountOptimistic(fromAccount,
                        amountFrom,
                        amountFrom - amount);
            }

            if (fromUpdated && toUpdated) {
                getConnection().commit();
                return true;
            } else {
                getConnection().rollback();
            }
        }
    }

    @SneakyThrows
    @Override
    public Long getAmount(String account) {
        if (!validator.checkAccountInput(account)) {
            return null;
        }

        return queryHelper.selectAmount(account);
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

}
