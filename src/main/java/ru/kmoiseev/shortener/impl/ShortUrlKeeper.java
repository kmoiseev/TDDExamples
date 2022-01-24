package ru.kmoiseev.shortener.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class ShortUrlKeeper {

    private final Map<String,String> fullUrlByShortUrl = new HashMap<>();

    void keep(final String shortUrl, final String fullUrl) {
        fullUrlByShortUrl.put(shortUrl, fullUrl);
    }

    String retrieve(final String shortUrl) {
        return fullUrlByShortUrl.get(shortUrl);
    }
}
