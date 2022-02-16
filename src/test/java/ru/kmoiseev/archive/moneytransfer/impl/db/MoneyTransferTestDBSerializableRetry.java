package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestDBSerializableRetry extends MoneyTransferTestAbstract {
    public MoneyTransferTestDBSerializableRetry() {
        super(new MoneyTransferDBSerializableRetry(), MULTIPLIER_DATABASE);
    }
}
