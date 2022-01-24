package ru.kmoiseev.balancer.impl.strategy;

import lombok.Value;

import java.util.List;
import java.util.Random;

/**
 * @author konstantinmoiseev
 * @since 22.01.2022
 */
@Value
public class StrategyRandom implements Strategy {

    List<String> urls;
    Integer size;

    public StrategyRandom(final Context context) {
        this.urls = context.getUrls();
        this.size = context.getUrls().size();
    }

    @Override
    public String getNext() {
        return urls.get(new Random().nextInt(size));
    }
}
