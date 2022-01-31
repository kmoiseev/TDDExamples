package ru.kmoiseev.atomicincrement.impl;

import ru.kmoiseev.atomicincrement.AtomicIncrement;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author konstantinmoiseev
 * @since 29.01.2022
 */
public class AtomicIncrementAndGet implements AtomicIncrement {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void increment() {
        atomicInteger.incrementAndGet();
    }

    @Override
    public void increaseByTwo() {
        atomicInteger.getAndAdd(2);
    }

    @Override
    public int get() {
        return atomicInteger.get();
    }
}
