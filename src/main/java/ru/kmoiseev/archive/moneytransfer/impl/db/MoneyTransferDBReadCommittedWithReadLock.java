package ru.kmoiseev.archive.moneytransfer.impl.db;

import lombok.SneakyThrows;
import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.InputValidator;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.ConnectionThreadSafeHolder;
import ru.kmoiseev.archive.moneytransfer.impl.db.common.QueryHelper;

/**
 * ----- DEPOSIT -----
 *
 * BEGIN
 *  SELECT amount FROM accounts WHERE id = XXX FOR UPDATE;
 *  UPDATE amount SET amount = NEW_AMOUNT WHERE id = XXX;
 *  COMMIT;
 *
 * ----- TRANSFER -----
 *
 * BEGIN
 *  # order selects based on id
 *  SELECT amount FROM accounts WHERE id = LEFT_LOCK FOR UPDATE;
 *  SELECT amount FROM accounts WHERE id = RIGHT_LOCK FOR UPDATE;
 *
 *  UPDATE accounts SET amount = NEW_AMOUNT_FROM WHERE id = FROM_ID;
 *  UPDATE accounts SET amount = NEW_AMOUNT_TO WHERE id = TO_ID;
 *
 * COMMIT;
 */
public class MoneyTransferDBReadCommittedWithReadLock extends ConnectionThreadSafeHolder implements MoneyTransfer {

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

        final Long amountBeforeDeposit = queryHelper.selectAmountForUpdate(toAccount);
        if (amountBeforeDeposit == null) {
            return false;
        }

        queryHelper.updateAmount(toAccount, amountBeforeDeposit + amount);

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
            amountOnFrom = queryHelper.selectAmountForUpdate(fromAccount);
            amountOnTo = queryHelper.selectAmountForUpdate(toAccount);
        } else {
            amountOnTo = queryHelper.selectAmountForUpdate(toAccount);
            amountOnFrom = queryHelper.selectAmountForUpdate(fromAccount);
        }

        if (amountOnFrom == null || amountOnTo == null || amountOnFrom < amount) {
            getConnection().rollback();
            return false;
        }

        queryHelper.updateAmount(fromAccount, amountOnFrom - amount);
        queryHelper.updateAmount(toAccount, amountOnTo + amount);

        getConnection().commit();

        return true;
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

    @SneakyThrows
    @Override
    public void shutdown() {
        getConnection().prepareStatement(
                "drop TABLE accounts;"
        ).execute();
        getConnection().commit();
    }
}
