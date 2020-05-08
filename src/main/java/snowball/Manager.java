package snowball;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.io.File;
import org.apache.commons.io.FileUtils;

import java.util.concurrent.*;
import java.util.logging.Logger;


public class Manager {
    static LinkedBlockingQueue<URLTransaction<URL>> input = new LinkedBlockingQueue<>(); //potentially unsafe urls
    static LinkedBlockingQueue<URLTransaction<String>> output = new LinkedBlockingQueue<>(); //output sanitized urls
    static ExecutorService service = Executors.newFixedThreadPool(50);
    static ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(
                    6,
                    6,
                    2,
                    TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>()
            );
    static final String OUTPUTDUMP = "savedFiles";
    static final int CRAWLLIMIT = 20; // in MB


    private final static Logger log = Logger.getLogger(Worker.class.getName());

    public static void loadSeeds(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        while (scanner.hasNextLine()) {
            URLTransaction<String> seed = new URLTransaction<>(scanner.nextLine(), Optional.of(0));
            output.add(seed);
        }
    }

    public static String sanitize(URLTransaction<String> url) throws MalformedURLException {
        String sanitizedUrl = url.url;

        int queryIndex = sanitizedUrl.indexOf("?");
        if( queryIndex != -1){
            sanitizedUrl = sanitizedUrl.substring(0, queryIndex);
        }

        int anchorIndex = sanitizedUrl.indexOf("#");
        if (anchorIndex != -1){
            sanitizedUrl = sanitizedUrl.substring(0,anchorIndex);
        }

        return sanitizedUrl;
    }

    public static boolean isURLValid(String url){
        try {
            URL currUrl = new URL(url);
            return !currUrl.getProtocol().isEmpty() && !currUrl.getHost().isEmpty();
        } catch (MalformedURLException e) {
            log.info(String.format("Invalid URL: %s", url));
            return false;
        }
    }

    public static boolean crawlLimitNotMet(){
        return ((FileUtils.sizeOf(new File(OUTPUTDUMP))/Long.valueOf(1000000)) <= CRAWLLIMIT);
    }

    public static void beginCrawl(Path outputDir) throws InterruptedException, MalformedURLException {
        File f = new File(OUTPUTDUMP);

        while ( (!output.isEmpty() || threadPoolExecutor.getActiveCount() > 0) && crawlLimitNotMet()) {
            URLTransaction<String> url = output.take();
            String sanitizedURL = sanitize(url);
            if (isURLValid(sanitizedURL)) {
                input.add(new URLTransaction<>(new URL(sanitizedURL), Optional.of(0)));
            }
            //TODO check if has been visited - Ryans component
            //TODO check against robots.txt

            threadPoolExecutor.execute(new Worker(input, output, outputDir));
        }

        threadPoolExecutor.shutdownNow();
    }

    public static void main( String[] args ) throws IOException, InterruptedException {

        long startTime = System.nanoTime();

        loadSeeds("seed_file.txt");
        log.info("Seed URLs have been loaded");

        Path outputDir = Paths.get(OUTPUTDUMP);

        beginCrawl(outputDir);
        log.info("Crawl has completed");

        long endTime = System.nanoTime();

        long duration = (endTime - startTime)/1000000000;  // divided by 1000000000 to get sec
        System.out.println("Crawl took: " + duration + " seconds");


    }
}
