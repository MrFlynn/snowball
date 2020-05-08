package snowball;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class WorkerTest {
    private Worker worker;
    private Document doc;

    @TempDir
    static Path outputDir;

    @BeforeEach
    void setup() throws IOException {
        this.worker = new Worker(
                new LinkedBlockingQueue<>(),
                new LinkedBlockingQueue<>(),
                outputDir
        );

        String testHtmlPath = "./src/test/test.html";
        File testFile = new File(testHtmlPath);
        this.doc = Jsoup.parse(testFile, "UTF-8", "https://example.com");
    }

    @Test
    void getLinks() throws IllegalAccessException {
        List<String> urls = Arrays.asList("https://default.com", "https://inlined.com", "https://example.com#test", "https://parameter.com?param=test");
        assertEquals(this.worker.getLinks(this.doc), urls);
    }

    @Test // TODO will fix later
    void saveDocument() throws MalformedURLException {
        URLTransaction urlTransaction = new URLTransaction<>(new URL("https://default.com/blah"), Optional.of(0));

        worker.saveFile(this.doc, urlTransaction);
        String filename = this.worker.createFileName(urlTransaction);
        File savedFile = outputDir.resolve(filename).toFile();

        assertTrue(savedFile.exists() && !savedFile.isDirectory());
    }

    @Test
    void InvalidContentType() throws IOException {
        URL invalidContent = new URL("https://web.stanford.edu/~jurafsky/slp3/3.pdf");
        assertEquals(Optional.empty(), this.worker.getDocument(invalidContent));

    }
}
