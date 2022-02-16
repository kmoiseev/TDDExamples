package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestDBRepeatableReadReadLockWithRetry extends MoneyTransferTestAbstract {
    public MoneyTransferTestDBRepeatableReadReadLockWithRetry() {
        super(new MoneyTransferDBRepeatableReadLockWithRetry(), MULTIPLIER_DATABASE);
    }
}
