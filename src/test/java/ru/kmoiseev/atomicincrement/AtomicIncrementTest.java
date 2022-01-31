package ru.kmoiseev.atomicincrement;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.kmoiseev.atomicincrement.impl.AtomicIncrementAndGet;
import ru.kmoiseev.atomicincrement.impl.AtomicIncrementRetrying;

import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author konstantinmoiseev
 * @since 29.01.2022
 */
public class AtomicIncrementTest {

    private static Stream<AtomicIncrement> createIncrements() {
        return Stream.of(new AtomicIncrementAndGet(), new AtomicIncrementRetrying());
    }

    @ParameterizedTest
    @MethodSource("createIncrements")
    void testAtomicIncrements(final AtomicIncrement increment) {

        final int amountOfIncrements = 40000000;

        final Runnable task = () -> {
            int localCounter = 0;
            while (++localCounter <= amountOfIncrements) {
                increment.increment();
            }
        };

        final Thread thread1 = new Thread(task);
        final Thread thread2 = new Thread(task);
        final Thread thread3 = new Thread(task);

        final long timeBefore = currentTimeMillis();

        thread1.start();
        thread2.start();
        thread3.start();


        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long timeTaken = currentTimeMillis() - timeBefore;

        System.out.println("Time taken is " + timeTaken);
        assertEquals(3 * amountOfIncrements, increment.get());
    }

    @ParameterizedTest
    @MethodSource("createIncrements")
    void testAtomicIncrementsBy2(final AtomicIncrement increment) {

        final int amountOfIncrements = 40000000;

        final Runnable task = () -> {
            int localCounter = 0;
            while (++localCounter <= amountOfIncrements) {
                increment.increaseByTwo();
            }
        };

        final Thread thread1 = new Thread(task);
        final Thread thread2 = new Thread(task);
        final Thread thread3 = new Thread(task);

        final long timeBefore = currentTimeMillis();

        thread1.start();
        thread2.start();
        thread3.start();


        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long timeTaken = currentTimeMillis() - timeBefore;

        System.out.println("Time taken is " + timeTaken);
        assertEquals(6 * amountOfIncrements, increment.get());
    }

}
