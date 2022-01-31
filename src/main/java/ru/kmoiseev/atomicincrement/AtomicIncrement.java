package ru.kmoiseev.atomicincrement;

/**
 * @author konstantinmoiseev
 * @since 29.01.2022
 */
public interface AtomicIncrement {
    void increment();
    void increaseByTwo();
    int get();
}
