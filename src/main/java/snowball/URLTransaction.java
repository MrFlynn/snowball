package snowball;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class URLTransaction<T> {
    public T url;
    public Integer ttl;

    public URLTransaction (T url, Optional<Integer> ttl) {
        if (!(url instanceof String || url instanceof URL)) {
            throw new IllegalArgumentException(
                    String.format("Type %s not one of String, URL", url.getClass())
            );
        }

        this.url = url;
        this.ttl = ttl.orElse(0);
    }

    public URLTransaction<URL> copyToURL() throws MalformedURLException {
        return new URLTransaction<>(new URL((String)this.url), Optional.of(this.ttl));
    }

    public URLTransaction<String> copyToString() throws IllegalArgumentException {
        return new URLTransaction<>(this.url.toString(), Optional.of(this.ttl));
    }
}
