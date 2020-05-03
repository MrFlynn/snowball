package snowball;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Worker implements Runnable {
    private final LinkedBlockingQueue<String> input;
    private final LinkedBlockingQueue<String> output;
    private final String outputDir;

    private final static Logger log = Logger.getLogger(Worker.class.getName());

    public Worker(LinkedBlockingQueue<String> input, LinkedBlockingQueue<String> output, String outputDir) {
        this.input = input;
        this.output = output;
        this.outputDir = outputDir;
    }

    private Optional<Document> getDocument(String url) {
        try {
            return Optional.ofNullable(Jsoup.connect(url).get());
        } catch (Exception e) {
            log.warning(String.format("Could not get document %s because %s", url, e));
        }

        return Optional.empty();
    }

    private void saveFile(Document doc) {
        Path finalPath = Paths.get(this.outputDir, doc.location());
        try {
            Files.writeString(finalPath, doc.html());
        } catch (IOException e) {
            log.warning(String.format("Could not write %s because %s", finalPath, e));
        }
    }

    private ArrayList<String> getLinks(Document doc) {
        ArrayList<String> urls = new ArrayList<>();

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
                String url = this.input.take();

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
