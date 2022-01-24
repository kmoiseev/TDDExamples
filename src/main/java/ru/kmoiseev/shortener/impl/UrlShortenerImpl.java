package ru.kmoiseev.shortener.impl;

import ru.kmoiseev.shortener.UrlShortener;

import static java.util.Objects.isNull;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class UrlShortenerImpl implements UrlShortener {

    private final String prefix = "https://mois-shortener.ru/";

    private final ShortUrlGenerator generator = new ShortUrlGenerator();
    private final ShortUrlKeeper keeper = new ShortUrlKeeper();

    @Override
    public String shortenUrl(String fullUrl) {
        if (isNull(fullUrl) || fullUrl.isBlank()) {
            return null;
        }
        final String urlShort = generator.generateNext();
        keeper.keep(urlShort, fullUrl);
        return prefix + urlShort;
    }

    @Override
    public String resolveShortenedUrl(String shortenedUrl) {
        if (isNull(shortenedUrl) || shortenedUrl.isBlank()) {
            return null;
        }
        final String urlShortenedNoPrefix = shortenedUrl.replace(prefix,"");
        return keeper.retrieve(urlShortenedNoPrefix);
    }
}
