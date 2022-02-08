package ru.kmoiseev.live;

import ru.kmoiseev.live.dto.BackEndInstance;
import ru.kmoiseev.live.dto.Strategy;

/**
 * @author konstantinmoiseev
 * @since 08.02.2022
 */
public interface LoadBalancer {
    void register(final BackEndInstance backEndInstance);
    BackEndInstance get(final Strategy strategy);
}
