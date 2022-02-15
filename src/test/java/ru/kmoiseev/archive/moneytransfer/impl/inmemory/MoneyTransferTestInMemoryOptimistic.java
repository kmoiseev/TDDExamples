package ru.kmoiseev.archive.moneytransfer.impl.inmemory;

import ru.kmoiseev.archive.moneytransfer.MoneyTransferTestAbstract;
import ru.kmoiseev.archive.moneytransfer.impl.inmemory.MoneyTransferInMemoryOptimistic;
import ru.kmoiseev.archive.moneytransfer.impl.inmemory.MoneyTransferInMemoryPessimistic;

import static ru.kmoiseev.archive.moneytransfer.Constants.MULTIPLIER_IN_MEMORY;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class MoneyTransferTestInMemoryOptimistic extends MoneyTransferTestAbstract {
    public MoneyTransferTestInMemoryOptimistic() {
        super(new MoneyTransferInMemoryOptimistic(), MULTIPLIER_IN_MEMORY);
    }
}
