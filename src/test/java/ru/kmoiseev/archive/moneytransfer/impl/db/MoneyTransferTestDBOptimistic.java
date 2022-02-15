package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;
import ru.kmoiseev.archive.moneytransfer.impl.db.MoneyTransferDBAtomicUpdate;
import ru.kmoiseev.archive.moneytransfer.impl.db.MoneyTransferDBOptimistic;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestDBOptimistic extends MoneyTransferTestAbstract {
    public MoneyTransferTestDBOptimistic() {
        super(new MoneyTransferDBOptimistic(), MULTIPLIER_DATABASE);
    }
}
