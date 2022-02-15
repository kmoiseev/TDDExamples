package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;
import ru.kmoiseev.archive.moneytransfer.impl.db.MoneyTransferDBAtomicUpdate;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestDBAtomicUpdate extends MoneyTransferTestAbstract {
    public MoneyTransferTestDBAtomicUpdate() {
        super(new MoneyTransferDBAtomicUpdate(), MULTIPLIER_DATABASE);
    }
}
