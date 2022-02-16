package ru.kmoiseev.archive.moneytransfer.impl.db;

import lombok.SneakyThrows;
import org.postgresql.util.PSQLException;
import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.InputValidator;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.ConnectionThreadSafeHolder;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.QueryHelper;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ----- DEPOSIT -----
 * <p>
 * BEGIN
 * SELECT amount FROM accounts WHERE id = XXX;
 * UPDATE amount SET amount = AAA WHERE id = XXX;
 * COMMIT;
 * END
 * -- ROLLBACK + RETRY IF ERROR
 * <p>
 * ----- TRANSFER -----
 * <p>
 * BEGIN
 * SELECT amount FROM accounts WHERE id = LEFT_LOCK;
 * SELECT amount FROM accounts WHERE id = RIGHT_LOCK;
 * <p>
 * # order updates based on id
 * UPDATE accounts SET amount = NEW_AMOUNT_FROM WHERE id = FROM_ID;
 * UPDATE accounts SET amount = NEW_AMOUNT_TO WHERE id = TO_ID;
 * <p>
 * COMMIT;
 * END
 * -- ROLLBACK + RETRY IF ERROR
 */
public class MoneyTransferDBSerializableRetry extends ConnectionThreadSafeHolder implements MoneyTransfer {

    private final InputValidator validator = new InputValidator();
    private final QueryHelper queryHelper = new QueryHelper(this);

    @SneakyThrows
    @Override
    protected void modifyConnection(Connection connection) {
        super.modifyConnection(connection);

        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
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
            try {
                final Long amountBeforeDeposit = queryHelper.selectAmount(toAccount);
                if (amountBeforeDeposit == null) {
                    return false;
                }

                if (!queryHelper.updateAmount(toAccount, amountBeforeDeposit + amount)) {
                    return false;
                }

                getConnection().commit();
                return true;
            } catch (PSQLException e) {
                getConnection().rollback();
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

        final boolean firstLockOnFrom = fromAccount.compareTo(toAccount) > 0;

        while (true) {
            try {

                final Long amountOnFrom = queryHelper.selectAmount(fromAccount);
                final Long amountOnTo = queryHelper.selectAmount(toAccount);

                if (amountOnFrom == null || amountOnTo == null || amountOnFrom < amount) {
                    getConnection().rollback();
                    return false;
                }

                final boolean fromUpdated;
                final boolean toUpdated;
                if (firstLockOnFrom) {
                    fromUpdated = queryHelper.updateAmount(fromAccount, amountOnFrom - amount);
                    toUpdated = queryHelper.updateAmount(toAccount, amountOnTo + amount);
                } else {
                    toUpdated = queryHelper.updateAmount(toAccount, amountOnTo + amount);
                    fromUpdated = queryHelper.updateAmount(fromAccount, amountOnFrom - amount);
                }

                if (toUpdated && fromUpdated) {
                    getConnection().commit();
                    return true;
                } else {
                    getConnection().rollback();
                    return false;
                }
            } catch (PSQLException e) {
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
