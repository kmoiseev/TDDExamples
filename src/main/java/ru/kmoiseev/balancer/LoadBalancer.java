package ru.kmoiseev.balancer;

/**
 * Req:
 * - Up to 20 entries
 * -
 */
public interface LoadBalancer {
    boolean registerUrl(String url);
    String roundRobin();
    String random();
}
