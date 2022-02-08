package ru.kmoiseev.archive.shortener;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public interface UrlShortener {
    String shortenUrl(final String fullUrl);
    String resolveShortenedUrl(final String shortenedUrl);
}
