package ru.kmoiseev.archive.balancer;

import ru.kmoiseev.archive.balancer.impl.strategy.BalanceType;

/**
 * Req:
 * - Up to 20 entries
 * -
 */
public interface LoadBalancer {
    boolean registerUrl(String url);
    String getUrl(BalanceType balanceType);
}
