package ru.kmoiseev.archive.balancer.impl.strategy;

/**
 * @author konstantinmoiseev
 * @since 22.01.2022
 */
public class StrategyEmptyUrls implements Strategy {

    @Override
    public String getNext() {
        return null;
    }
}
