package snowball;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Worker implements Runnable {
    private final LinkedBlockingQueue<URL> input;
    private final LinkedBlockingQueue<String> output;
    private final Path outputDir;

    private final static Logger log = Logger.getLogger(Worker.class.getName());

    public Worker(LinkedBlockingQueue<URL> input, LinkedBlockingQueue<String> output, Path outputDir) {
        this.input = input;
        this.output = output;
        this.outputDir = outputDir;
    }

    private Optional<Document> getDocument(URL url) {
        try {
            return Optional.ofNullable(Jsoup.connect(url.toString()).get());
        } catch (Exception e) {
            log.warning(String.format("Could not get document %s because %s", url, e));
        }

        return Optional.empty();
    }

    public void saveFile(Document doc) {
        Path finalPath = this.outputDir.resolve(doc.location());
        try {
            Files.writeString(finalPath, doc.html());
        } catch (IOException e) {
            log.warning(String.format("Could not write %s because %s", finalPath, e));
        }
    }

    public List<String> getLinks(Document doc) {
        List<String> urls = new ArrayList<>();

        for (Element link : doc.select("a[href]")) {
            urls.add(link.attr("href"));
        }

        return urls;
    }

    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                URL url = this.input.take();

                Optional<Document> doc = this.getDocument(url);
                if (doc.isPresent()) {
                    for (String u : this.getLinks(doc.get())) {
                        this.output.put(u);
                    }

                    this.saveFile(doc.get());
                }
            }
        } catch (InterruptedException e) {
            log.info("Thread exiting...");
        }
    }
}
