package snowball;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ManagerTest {

    @Test
    void sanitize() throws MalformedURLException {
        List<String> unsanitizedURLS = Arrays.asList(
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "https://en.wikipedia.org/wiki/Kobe_Bryant#Three-peat_(1999%E2%80%932002)",
                "http://www.domain.com/search?query=hello#name");

        List<String> sanitizedURLS = Arrays.asList(
                "https://www.youtube.com/watch",
                "https://en.wikipedia.org/wiki/Kobe_Bryant",
                "http://www.domain.com/search");

        for(int i =0; i < unsanitizedURLS.size(); i++){
            assertEquals(sanitizedURLS.get(i), Manager.sanitize(
                    new URLTransaction<String>(unsanitizedURLS.get(i), Optional.of(0))));
        }

    }



}