package snowball;

import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RobotsHandlerTest {
    private RobotsHandler handler = new RobotsHandler();

    @RepeatedTest(2)
    @Order(1)
    void testValidPath() throws MalformedURLException {
        assertTrue(handler.validate(new URL("https://google.com/search/about")));
    }

    @Test
    @Order(2)
    void testInvalidPath() throws MalformedURLException {
        assertFalse(handler.validate(new URL("https://google.com/search/")));
    }

    @Test
    @Order(3)
    void testDomainWithoutRobotsTxt() throws MalformedURLException {
        assertTrue(handler.validate(new URL("https://pleatsikas.me/resume.pdf")));
    }
}
