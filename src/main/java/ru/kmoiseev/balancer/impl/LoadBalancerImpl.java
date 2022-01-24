package ru.kmoiseev.balancer.impl;

import ru.kmoiseev.balancer.LoadBalancer;
import ru.kmoiseev.balancer.impl.strategy.Context;
import ru.kmoiseev.balancer.impl.strategy.Strategy;
import ru.kmoiseev.balancer.impl.strategy.StrategyEmptyUrls;
import ru.kmoiseev.balancer.impl.strategy.StrategyRandom;
import ru.kmoiseev.balancer.impl.strategy.StrategyRoundRobin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * @author konstantinmoiseev
 * @since 22.01.2022
 */
public class LoadBalancerImpl implements LoadBalancer {

    private final Long limit;
    private List<String> urls = emptyList();
    private Strategy random = new StrategyEmptyUrls();
    private Strategy roundRobin = new StrategyEmptyUrls();

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

        random = new StrategyRandom(context);
        roundRobin = new StrategyRoundRobin(context);

        return true;
    }

    @Override
    public String roundRobin() {
        return roundRobin.getNext();
    }

    @Override
    public String random() {
        return random.getNext();
    }
}
