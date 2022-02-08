package ru.kmoiseev.archive.balancerlive;

import ru.kmoiseev.archive.balancerlive.dto.BackEndInstance;
import ru.kmoiseev.archive.balancerlive.dto.Strategy;

/**
 * @author konstantinmoiseev
 * @since 08.02.2022
 */
public interface LoadBalancer {
    void register(final BackEndInstance backEndInstance);
    BackEndInstance get(final Strategy strategy);
}
