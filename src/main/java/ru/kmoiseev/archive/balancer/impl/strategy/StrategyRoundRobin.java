package ru.kmoiseev.archive.balancer.impl.strategy;

import lombok.Value;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author konstantinmoiseev
 * @since 22.01.2022
 */
@Value
public class StrategyRoundRobin implements Strategy {

    List<String> urls;
    Integer size;
    AtomicInteger index;

    public StrategyRoundRobin(Context context) {
        this.size = context.getUrls().size();
        this.urls = context.getUrls();
        this.index = new AtomicInteger(0);
    }

    @Override
    public String getNext() {
        return urls.get(this.index.getAndUpdate(i -> i + 1 >= size ? 0 : i + 1));
    }
}
