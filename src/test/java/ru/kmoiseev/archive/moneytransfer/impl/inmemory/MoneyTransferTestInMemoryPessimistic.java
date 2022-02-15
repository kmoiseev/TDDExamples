package ru.kmoiseev.archive.moneytransfer.impl.inmemory;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_IN_MEMORY;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestInMemoryPessimistic extends MoneyTransferTestAbstract {
    public MoneyTransferTestInMemoryPessimistic() {
        super(new MoneyTransferInMemoryPessimistic(), MULTIPLIER_IN_MEMORY);
    }
}
