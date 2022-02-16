package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestDBTwoRowsDelta extends MoneyTransferTestAbstract {
    public MoneyTransferTestDBTwoRowsDelta() {
        super(new MoneyTransferDBTwoRowsDelta(), MULTIPLIER_DATABASE);
    }
}
