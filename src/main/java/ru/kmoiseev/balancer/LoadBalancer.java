package ru.kmoiseev.balancer;

import ru.kmoiseev.balancer.impl.strategy.BalanceType;

/**
 * Req:
 * - Up to 20 entries
 * -
 */
public interface LoadBalancer {
    boolean registerUrl(String url);
    String getUrl(BalanceType balanceType);
}
