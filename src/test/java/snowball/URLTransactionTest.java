package snowball;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class URLTransactionTest {
    @Test
    void transactionWithoutTTL() {
        URLTransaction<String> transaction = new URLTransaction<>("example.com", Optional.empty());
        assertAll("Default URLTransaction",
                () -> assertEquals(transaction.ttl, 0),
                () -> assertEquals(transaction.url, "example.com")
        );
    }

    @Test
    void transactionWithTTL() {
        URLTransaction<String> transaction = new URLTransaction<>("example.com", Optional.of(2));
        assertAll("URLTransaction with TTL",
                () -> assertEquals(transaction.ttl,2),
                () -> assertEquals(transaction.url, "example.com")
        );
    }

    @Test
    void testInvalidTypeHandling() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new URLTransaction<>(0, Optional.empty())
        );
    }

    @Test
    void testCopyToURL() throws MalformedURLException {
        URLTransaction<String> original = new URLTransaction<>("http://example.com", Optional.empty());
        URLTransaction<URL> copied = original.copyToURL();

        assertAll("Validate original was cloned to copied and type was properly cast",
                () -> assertEquals(copied.ttl, original.ttl),
                () -> assertEquals(copied.url.toString(), original.url),
                () -> assertEquals(copied.url.getClass(), URL.class)
        );
    }

    @Test
    void testCopyToString() throws MalformedURLException {
        URLTransaction<URL> original = new URLTransaction<>(new URL("http://example.com"), Optional.empty());
        URLTransaction<String> copied = original.copyToString();

        assertAll("Validate original was cloned to copied and type was properly cast",
                () -> assertEquals(copied.ttl, original.ttl),
                () -> assertEquals(copied.url, original.url.toString()),
                () -> assertEquals(copied.url.getClass(), String.class)
        );
    }
}
