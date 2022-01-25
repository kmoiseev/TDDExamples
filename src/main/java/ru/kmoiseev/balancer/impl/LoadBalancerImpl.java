package ru.kmoiseev.balancer.impl;

import ru.kmoiseev.balancer.LoadBalancer;
import ru.kmoiseev.balancer.impl.strategy.BalanceType;
import ru.kmoiseev.balancer.impl.strategy.Context;
import ru.kmoiseev.balancer.impl.strategy.Strategy;
import ru.kmoiseev.balancer.impl.strategy.StrategyEmptyUrls;
import ru.kmoiseev.balancer.impl.strategy.StrategyRandom;
import ru.kmoiseev.balancer.impl.strategy.StrategyRoundRobin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static ru.kmoiseev.balancer.impl.strategy.BalanceType.RANDOM;
import static ru.kmoiseev.balancer.impl.strategy.BalanceType.ROUND_ROBIN;

/**
 * @author konstantinmoiseev
 * @since 22.01.2022
 */
public class LoadBalancerImpl implements LoadBalancer {

    private final Long limit;
    private final Map<BalanceType, Function<Context,Strategy>> strategiesCreators = Map.of(
            ROUND_ROBIN, StrategyRoundRobin::new,
            RANDOM, StrategyRandom::new
    );
    private volatile List<String> urls = emptyList();
    private volatile Map<BalanceType,Strategy> strategies = Map.of(
            ROUND_ROBIN, new StrategyEmptyUrls(),
            RANDOM, new StrategyEmptyUrls());

    public LoadBalancerImpl(Long limit) {
        this.limit = limit;
    }

    @Override
    public synchronized boolean registerUrl(String url) {
        if (urls.size() >= limit) {
            return false;
        }

        if (urls.contains(url)) {
            return false;
        }

        urls = new ArrayList<>(urls);
        urls.add(url);

        final Context context = new Context(urls);


        strategies = strategiesCreators.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        (entry) -> entry.getValue().apply(context)
                ));

        return true;
    }

    @Override
    public String getUrl(BalanceType balanceType) {
        if (balanceType == null) {
            return null;
        }
        return strategies.get(balanceType).getNext();
    }
}
