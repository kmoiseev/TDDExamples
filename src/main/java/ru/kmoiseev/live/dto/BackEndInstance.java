package ru.kmoiseev.live.dto;

import java.util.Objects;

/**
 * @author konstantinmoiseev
 * @since 08.02.2022
 */
public class BackEndInstance {

    private final String url;

    public BackEndInstance(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BackEndInstance that = (BackEndInstance) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
