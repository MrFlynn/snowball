package snowball;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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
        this.doc = Jsoup.parse(testFile, "UTF-8", "example.com");
    }

    @Test
    void getLinks() {
        List<String> urls = Arrays.asList("default.com", "inlined.com", "#test", "parameter.com?param=test");
        assertEquals(this.worker.getLinks(this.doc), urls);
    }

    @Test
    void saveDocument() {
        worker.saveFile(this.doc);

        File savedFile = outputDir.resolve("example.com").toFile();
        assertTrue(savedFile.exists() && !savedFile.isDirectory());
    }
}
