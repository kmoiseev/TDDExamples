package ru.kmoiseev.shortener.impl;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class ShortUrlGenerator {

    private final AtomicInteger increment = new AtomicInteger(1);

    String generateNext() {
        final Integer toTranslate = increment.getAndIncrement();
        return translateIntegerToString(toTranslate);
    }

    private String translateIntegerToString(final Integer in) {
        return in.toString();
    }
}
