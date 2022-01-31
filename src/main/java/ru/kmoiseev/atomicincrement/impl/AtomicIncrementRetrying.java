package ru.kmoiseev.atomicincrement.impl;

import ru.kmoiseev.atomicincrement.AtomicIncrement;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author konstantinmoiseev
 * @since 29.01.2022
 */
public class AtomicIncrementRetrying implements AtomicIncrement {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void increment() {
        int value;
        do {
            value = atomicInteger.get();
        } while (!atomicInteger.compareAndSet(value, value + 1));
    }

    @Override
    public void increaseByTwo() {
        int value;
        do {
            value = atomicInteger.get();
        } while (!atomicInteger.compareAndSet(value, value + 2));
    }

    @Override
    public int get() {
        return atomicInteger.get();
    }
}
