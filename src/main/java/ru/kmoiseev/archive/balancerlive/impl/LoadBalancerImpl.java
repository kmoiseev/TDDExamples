package ru.kmoiseev.archive.balancerlive.impl;

import ru.kmoiseev.archive.balancerlive.LoadBalancer;
import ru.kmoiseev.archive.balancerlive.dto.BackEndInstance;
import ru.kmoiseev.archive.balancerlive.dto.Strategy;
import ru.kmoiseev.archive.balancerlive.exception.AddressAlreadyRegisteredException;
import ru.kmoiseev.archive.balancerlive.exception.AddressesLimitExceededException;
import ru.kmoiseev.archive.balancerlive.exception.NoInstancesPresentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author konstantinmoiseev
 * @since 08.02.2022
 */
public class LoadBalancerImpl  implements LoadBalancer {

    private final Integer ADDRESSES_LIMIT = 10;
    private final ReadWriteLock addressesLock = new ReentrantReadWriteLock();
    private final List<BackEndInstance> addressesRegistered = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public void register(final BackEndInstance backEndInstance) {
        addressesLock.writeLock().lock();
        try {
            if (addressesRegistered.size() >= ADDRESSES_LIMIT){
                throw new AddressesLimitExceededException();
            }

            if (addressesRegistered.contains(backEndInstance)) {
                throw new AddressAlreadyRegisteredException();
            }

            addressesRegistered.add(backEndInstance);
        } finally {
            addressesLock.writeLock().unlock();
        }
    }

    @Override
    public BackEndInstance get(final Strategy strategy) {
        addressesLock.readLock().lock();
        try {
            final int addressesSize = addressesRegistered.size();
            if (addressesSize == 0) {
                throw new NoInstancesPresentException();
            }

            switch (strategy) {
                case RANDOM:
                    return getRandom();
                case ROUND_ROBIN:
                    return getRoundRobin();
                default:
                    throw new UnsupportedOperationException();
            }
        } finally {
            addressesLock.readLock().unlock();
        }
    }

    private BackEndInstance getRandom() {
        return addressesRegistered.get(random.nextInt(addressesRegistered.size()));
    }

    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private BackEndInstance getRoundRobin() {
        return addressesRegistered.get(roundRobinCounter.getAndUpdate(i -> i >= addressesRegistered.size() - 1 ? 0 : i + 1));
    }
}
