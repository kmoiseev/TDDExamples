package ru.kmoiseev.archive.shortener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kmoiseev.archive.shortener.impl.UrlShortenerImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class UrlShortenerTest {
    private UrlShortener urlShortener;

    @BeforeEach
    void beforeEach() {
        urlShortener = new UrlShortenerImpl();
    }

    @Test
    void correctFullUrlIsReturned() {
        final String fullUrl = "https://i.am.full.url";

        final String shortUrl = urlShortener.shortenUrl(fullUrl);
        final String fullUrlResolved = urlShortener.resolveShortenedUrl(shortUrl);

        assertEquals(fullUrl, fullUrlResolved, "Resolved url is not the same");
    }

    @Test
    void testSameUrlsCanBeShortenedAndCorrectReturned() {
        final String fullUrl = "https://i.am.full.url";

        final List<String> urlsShortened = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> urlShortener.shortenUrl(fullUrl))
                .collect(toUnmodifiableList());

        urlsShortened.forEach(urlShortened ->
                assertEquals(fullUrl, urlShortener.resolveShortenedUrl(urlShortened), "Resolved url is not the same"));
    }

    @Test
    void testMultipleDifferentUrlsCanBeShortened() {
        final List<String> urlsFull = IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> "https://i.am.full.url/" + i)
                .collect(toUnmodifiableList());

        final Map<String,String> urlFullByUrlShort = urlsFull.stream()
                .collect(Collectors.toUnmodifiableMap(
                        urlFull -> urlShortener.shortenUrl(urlFull),
                        urlFull -> urlFull
                ));

        urlFullByUrlShort.forEach((key, value) -> assertEquals(value, urlShortener.resolveShortenedUrl(key)));
    }

    @Test
    void testManyUrlsAreStoredWithDifferentShortenedValues() {
        assertEquals(
                IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> "https://i.am.full.url/" + i)
                .map(urlShortener::shortenUrl)
                .distinct()
                .count(),
                1000,
                "Expecting 1000 distinct url shortens");
    }

    public static class FullAndShortedUrls {
        private final String fullUrl;
        private final String shortenedUrl;

        public FullAndShortedUrls(String fullUrl, String shortenedUrl) {
            this.fullUrl = fullUrl;
            this.shortenedUrl = shortenedUrl;
        }
    }
    @Test
    void testManyAsyncUrlRegistrationsWorkCorrect() {
        final List<String> urlsFull = IntStream.rangeClosed(1, 100000)
                .mapToObj(i -> "https://i.am.full.url/" + i)
                .collect(toUnmodifiableList());

        final ExecutorService executorService = Executors.newFixedThreadPool(25);

        urlsFull.stream()
                .map(urlFull ->
                    CompletableFuture.supplyAsync(() -> new FullAndShortedUrls(
                            urlFull,
                            urlShortener.shortenUrl(urlFull)
                    ), executorService)
                )
        .map(CompletableFuture::join)
        .forEach(fullAndShortedUrls ->
                assertEquals(fullAndShortedUrls.fullUrl, urlShortener.resolveShortenedUrl(fullAndShortedUrls.shortenedUrl)));
    }
}
