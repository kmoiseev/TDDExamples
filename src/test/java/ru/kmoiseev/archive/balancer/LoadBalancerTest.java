package ru.kmoiseev.archive.balancer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kmoiseev.archive.balancer.impl.LoadBalancerImpl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.kmoiseev.archive.balancer.impl.strategy.BalanceType.RANDOM;
import static ru.kmoiseev.archive.balancer.impl.strategy.BalanceType.ROUND_ROBIN;

/**
 * @author konstantinmoiseev
 * @since 22.01.2022
 */
public class LoadBalancerTest {

    private LoadBalancer loadBalancer;

    @BeforeEach
    void beforeEach() {
        loadBalancer = new LoadBalancerImpl(20L);
    }

    @Test
    void limitCanBeAdded() {
        boolean allAdded = true;
        final int limit = 20;

        for (int i = 0; i < limit; ++i) {
            allAdded = allAdded && loadBalancer.registerUrl("uniqueUri#" + i);
        }

        assertTrue(allAdded);
    }

    @Test
    void noMoreThanLimitCanBeAdded() {
        boolean allAdded = true;
        final int limit = 20;

        for (int i = 0; i < limit + 1; ++i) {
            allAdded = allAdded && loadBalancer.registerUrl("uniqueUri#" + i);
        }

        assertFalse(allAdded);
    }

    @Test
    void sameAreNotRegisteredTwice() {
        loadBalancer.registerUrl("1");
        loadBalancer.registerUrl("2");
        final boolean addedSecond = loadBalancer.registerUrl("1");

        assertFalse(addedSecond, "Same uri cannot be registered twice");
        assertEquals("1", loadBalancer.getUrl(ROUND_ROBIN));
        assertEquals("2", loadBalancer.getUrl(ROUND_ROBIN));
        assertEquals("1", loadBalancer.getUrl(ROUND_ROBIN));
    }

    @Test
    void roundRobinStartsAtFirst() {
        loadBalancer.registerUrl("1");
        loadBalancer.registerUrl("2");
        loadBalancer.registerUrl("3");

        assertEquals("1", loadBalancer.getUrl(ROUND_ROBIN), "Expecting the first uri to be round robined first");
    }

    @Test
    void roundRobinReturnsNullWhenNothingRegistered() {
        assertNull(loadBalancer.getUrl(ROUND_ROBIN), "Did not expect anything to return when nothing have been registered");
    }

    @Test
    void roundRobinReturnsInTheSameOrderElementsWereRegisteredWithCycle() {
        loadBalancer.registerUrl("1");
        loadBalancer.registerUrl("2");
        loadBalancer.registerUrl("3");
        loadBalancer.registerUrl("4");

        for (int i = 0; i < 5; ++i) {
            assertEquals("1", loadBalancer.getUrl(ROUND_ROBIN));
            assertEquals("2", loadBalancer.getUrl(ROUND_ROBIN));
            assertEquals("3", loadBalancer.getUrl(ROUND_ROBIN));
            assertEquals("4", loadBalancer.getUrl(ROUND_ROBIN));
        }
    }

    @Test
    void roundRobinKeepsOrderWithMultipleAsyncRequests() {
        loadBalancer.registerUrl("1");
        loadBalancer.registerUrl("2");
        loadBalancer.registerUrl("3");
        loadBalancer.registerUrl("4");

        final ExecutorService executorService = Executors.newFixedThreadPool(25);
        final Runnable runnableRoundRobins = () -> {
            loadBalancer.getUrl(ROUND_ROBIN);
            loadBalancer.getUrl(ROUND_ROBIN);
        };
        IntStream.rangeClosed(1, 10000)
                .parallel()
                .mapToObj(runnable -> runAsync(runnableRoundRobins, executorService))
                .forEach(CompletableFuture::join);


        assertEquals("1", loadBalancer.getUrl(ROUND_ROBIN));
        assertEquals("2", loadBalancer.getUrl(ROUND_ROBIN));
    }

    @Test
    void randomReturnsCorrectValue() {
        final Set<String> urls = Set.of("1", "2", "3", "4");
        urls.forEach(loadBalancer::registerUrl);

        final ExecutorService executorService = Executors.newFixedThreadPool(25);
        IntStream.rangeClosed(1, 10000)
                .parallel()
                .mapToObj(runnable -> runAsync(() -> assertTrue(urls.contains(loadBalancer.getUrl(RANDOM))), executorService))
                .forEach(CompletableFuture::join);
    }

    @Test
    void randomDoesNotReturnSame() {
        final List<String> urls = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        urls.forEach(loadBalancer::registerUrl);

        final List<Integer> results = IntStream.rangeClosed(1, 10000)
                .mapToObj(i -> urls.indexOf(loadBalancer.getUrl(RANDOM)))
                .collect(Collectors.toList());

        assertNotEquals(1, results.stream().distinct().count(), "All results of random returned same url");
    }
}
