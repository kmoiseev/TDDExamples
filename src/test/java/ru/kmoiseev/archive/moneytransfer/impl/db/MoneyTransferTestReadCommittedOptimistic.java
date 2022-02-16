package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 16.02.2022
 */
public class MoneyTransferTestReadCommittedOptimistic extends MoneyTransferTestAbstract {
    public MoneyTransferTestReadCommittedOptimistic() {
        super(new MoneyTransferDBReadCommittedOptimistic(), MULTIPLIER_DATABASE);
    }
}
