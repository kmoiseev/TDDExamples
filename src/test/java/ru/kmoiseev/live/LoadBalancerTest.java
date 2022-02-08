package ru.kmoiseev.live;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kmoiseev.live.dto.BackEndInstance;
import ru.kmoiseev.live.dto.Strategy;
import ru.kmoiseev.live.exception.AddressAlreadyRegisteredException;
import ru.kmoiseev.live.exception.AddressesLimitExceededException;
import ru.kmoiseev.live.exception.NoInstancesPresentException;
import ru.kmoiseev.live.impl.LoadBalancerImpl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.kmoiseev.live.dto.Strategy.RANDOM;
import static ru.kmoiseev.live.dto.Strategy.ROUND_ROBIN;

/**
 * Task 1. Register backend-instance to the Load Balancer
 * Address should be unique, it should not be possible to register the same address two times
 * Load balancer should accept up to 10 addresses
 * <p>
 * Task 2. Develop an algorithm that, when invoking
 * the Load Balancer's get() method multiple times,
 * should return one backend-instance choosing between the registered ones randomly..
 * <p>
 * Task 3. Develop an algorithm that, when invoking multiple times the Load Balancer on its get() method, should
 * return one backend-instance choosing between the registered one sequentially (round-robin)
 */
public class LoadBalancerTest {

    private LoadBalancer loadBalancer;

    @BeforeEach
    void beforeEach() {
        loadBalancer = new LoadBalancerImpl();
    }

    @Test
    void testAddressCanBeAddedWithoutExceptions() {
        loadBalancer.register(new BackEndInstance("http://localhost"));
    }


    @Test
    void testSameAddressCannotBeAddedTwice() {

        final BackEndInstance beiOne = new BackEndInstance("http://localhost");
        final BackEndInstance beiTwo = new BackEndInstance("http://localhost");

        assertThrows(AddressAlreadyRegisteredException.class, () -> {
            loadBalancer.register(beiOne);
            loadBalancer.register(beiTwo);
        });
    }


    @Test
    void test10AddressesCanBeAdded() {
        final String urlPattern = "http://localhost/";

        rangeClosed(1, 10)
                .forEach(i -> loadBalancer.register(new BackEndInstance(urlPattern + i)));
    }

    @Test
    void testMoreThan10AddressesCannotBeAdded() {
        final String urlPattern = "http://localhost/";

        assertThrows(AddressesLimitExceededException.class, () ->
                rangeClosed(1, 11)
                        .forEach(i -> loadBalancer.register(new BackEndInstance(urlPattern + i))));
    }

    @Test
    void testCannotGetRandomBeforeAnyBackEndInstanceAdded() {
        assertThrows(NoInstancesPresentException.class, () -> loadBalancer.get(RANDOM));
    }

    @Test
    void testGetRandomReturnsSameInstanceThatWasAdded() {
        final BackEndInstance backEndInstance = new BackEndInstance("http://localhost");

        loadBalancer.register(backEndInstance);

        assertEquals(backEndInstance, loadBalancer.get(RANDOM));
    }

    @Test
    void testGetRandomReturnsValidInstance() {
        final String urlPattern = "http://localhost/";

        final Set<BackEndInstance> backEndInstances = rangeClosed(1, 10)
                .mapToObj(i -> new BackEndInstance(urlPattern + i))
                .collect(Collectors.toUnmodifiableSet());

        backEndInstances.forEach(loadBalancer::register);

        rangeClosed(1, 1000)
                .forEach(i -> assertTrue(backEndInstances.contains(loadBalancer.get(RANDOM))));
    }

    @Test
    void testGetRandomReturnsRandomInstances() {
        final String urlPattern = "http://localhost/";

        final Set<BackEndInstance> backEndInstances = rangeClosed(1, 10)
                .mapToObj(i -> new BackEndInstance(urlPattern + i))
                .collect(Collectors.toUnmodifiableSet());

        backEndInstances.forEach(loadBalancer::register);

        final Set<BackEndInstance> backEndInstancesReturned =
                rangeClosed(1, 1000)
                        .mapToObj(i -> loadBalancer.get(RANDOM)).collect(Collectors.toUnmodifiableSet());

        assertFalse(backEndInstancesReturned.size() <= 1);
    }

    @Test
    void testRoundRobinCannotReturnBeforeAnyInstanceAdded() {
        assertThrows(NoInstancesPresentException.class, () -> loadBalancer.get(ROUND_ROBIN));
    }

    @Test
    void testRoundRobinReturnsCorrectWhenSingleInstanceAdded() {
        final BackEndInstance backEndInstance = new BackEndInstance("http://localhost");

        loadBalancer.register(backEndInstance);

        assertEquals(backEndInstance, loadBalancer.get(ROUND_ROBIN));
    }

    @Test
    void testRoundRobinReturnsSubsequent() {
        final String urlPattern = "http://localhost/";

        final List<BackEndInstance> backEndInstancesOrdered = range(0, 10)
                .mapToObj(i -> new BackEndInstance(urlPattern + i))
                .collect(Collectors.toUnmodifiableList());

        backEndInstancesOrdered.forEach(loadBalancer::register);

        range(0, 35).forEach(i ->
                assertEquals(backEndInstancesOrdered.get(i % 10), loadBalancer.get(ROUND_ROBIN))
        );
    }
}
