package snowball;

import org.jsoup.Connection;
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
    private final LinkedBlockingQueue<URLTransaction<URL>> input;
    private final LinkedBlockingQueue<URLTransaction<String>> output;
    private final Path outputDir;

    private final static Logger log = Logger.getLogger(Worker.class.getName());

    public Worker(LinkedBlockingQueue<URLTransaction<URL>> input,
                  LinkedBlockingQueue<URLTransaction<String>> output,
                  Path outputDir) {
        this.input = input;
        this.output = output;
        this.outputDir = outputDir;
    }

    public Optional<Document> getDocument(URL url) {
        try {
            Connection.Response resp = Jsoup.connect(url.toString()).timeout(5000).execute();
            return Optional.ofNullable(resp.parse());
        } catch (Exception e) {
            log.warning(String.format("Could not get document %s because %s", url, e));
        }

        return Optional.empty();
    }

    public void saveFile(Document doc, URLTransaction<URL> inputURL) {
        String fileName = createFileName(inputURL);
        Path finalPath = this.outputDir.resolve(fileName);
        try {
            Files.write(finalPath, doc.html().getBytes());
        } catch (IOException e) {
            log.warning(String.format("Could not write %s because %s", finalPath, e));
        }
    }

    public String createFileName(URLTransaction<URL> inputURL){
        String url = inputURL.url.toString();
        return url.replaceAll("/", "|");
    }

    public List<String> getLinks(Document doc) {
        List<String> urls = new ArrayList<>();

        for (Element link : doc.select("a[href]")) {
            urls.add(link.attr("abs:href"));
        }

        return urls;
    }

    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                URLTransaction<URL> inputURL = this.input.take();

                Optional<Document> doc = this.getDocument(inputURL.url);
                if (doc.isPresent()) {
                    for (String url : this.getLinks(doc.get())) {
                        URLTransaction<String> out = new URLTransaction<>(url, Optional.of(inputURL.ttl + 1));
                        this.output.put(out);
                    }

                    this.saveFile(doc.get(), inputURL);
                }
            }
        } catch (InterruptedException e) {
            log.info("Thread exiting...");
        }
    }
}
