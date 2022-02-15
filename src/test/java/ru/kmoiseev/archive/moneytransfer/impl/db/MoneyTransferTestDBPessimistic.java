package ru.kmoiseev.archive.moneytransfer.impl.db;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;
import ru.kmoiseev.archive.moneytransfer.impl.db.MoneyTransferDBPessimistic;
import ru.kmoiseev.archive.moneytransfer.impl.inmemory.MoneyTransferInMemoryPessimistic;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_DATABASE;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestDBPessimistic extends MoneyTransferTestAbstract {
    public MoneyTransferTestDBPessimistic() {
        super(new MoneyTransferDBPessimistic(), MULTIPLIER_DATABASE);
    }
}
