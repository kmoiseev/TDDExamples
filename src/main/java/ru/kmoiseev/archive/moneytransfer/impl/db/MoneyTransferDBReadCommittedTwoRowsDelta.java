package ru.kmoiseev.archive.moneytransfer.impl.db;

import lombok.SneakyThrows;
import org.postgresql.util.PSQLException;
import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.InputValidator;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.ConnectionThreadSafeHolder;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.QueryHelper;

/**
 * ----- DEPOSIT -----
 * <p>
 * BEGIN
 * UPDATE amount SET amount = amount + DELTA WHERE id = XXX;
 * COMMIT;
 * END
 * <p>
 * ----- TRANSFER -----
 * <p>
 * BEGIN
 * # order updates based on id
 * UPDATE accounts SET amount = amount - DELTA WHERE id = FROM_ID;
 * UPDATE accounts SET amount = amount + DELTA WHERE id = TO_ID;
 * <p>
 * COMMIT;
 * END
 */
public class MoneyTransferDBReadCommittedTwoRowsDelta extends ConnectionThreadSafeHolder implements MoneyTransfer {

    private final InputValidator validator = new InputValidator();
    private final QueryHelper queryHelper = new QueryHelper(this);

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

        if (!queryHelper.updateAmountWithDelta(toAccount, amount)) {
            return false;
        } else {
            getConnection().commit();
        }

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

        try {
            final boolean fromUpdated;
            final boolean toUpdated;
            if (firstLockOnFrom) {
                fromUpdated = queryHelper.updateAmountWithDelta(fromAccount, -amount);
                toUpdated = queryHelper.updateAmountWithDelta(toAccount, amount);
            } else {
                toUpdated = queryHelper.updateAmountWithDelta(toAccount, amount);
                fromUpdated = queryHelper.updateAmountWithDelta(fromAccount, -amount);
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
            if (e.getServerErrorMessage().getConstraint() != null) {
                return false;
            }
            throw e;
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
                        "amount bigint not null check ( amount >= 0 )" +
                        ");"
        ).execute();
        getConnection().commit();
    }

}
