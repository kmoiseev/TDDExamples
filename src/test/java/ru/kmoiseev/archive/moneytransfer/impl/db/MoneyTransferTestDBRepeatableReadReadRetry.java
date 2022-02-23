package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestDBRepeatableReadReadRetry extends MoneyTransferTestAbstract {
    public MoneyTransferTestDBRepeatableReadReadRetry() {
        super(new MoneyTransferDBRepeatableReadRetry(), MULTIPLIER_DATABASE);
    }
}
