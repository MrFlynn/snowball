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

import com.google.common.net.InternetDomainName;
import org.apache.commons.io.FileUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


public class Manager {
    static LinkedBlockingQueue<URLTransaction<URL>> input = new LinkedBlockingQueue<>(); //potentially unsafe urls
    static LinkedBlockingQueue<URLTransaction<String>> output = new LinkedBlockingQueue<>(); //output sanitized urls
    static RobotsHandler robotsHandler = new RobotsHandler();
    static VisitedURLs visitedURLs  = new VisitedURLs();
    public static AtomicInteger savedFileCounter = new AtomicInteger(0);
    private Integer maxDepth;
    private Integer maxPages;
    private Path outputDir;
    private Integer threads;
    private File seedFile;
    private Integer maxSize;


    static ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(
                    6,
                    10,
                    2,
                    TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>()
            );
    static final String OUTPUTDUMP = "savedFiles";
    static final int CRAWLLIMIT = 20; // in MB

    public Manager(Integer maxDepth, Integer maxPages, Path outputDir, Integer threads,
                   File seedFile, Integer maxSize){
        this.maxDepth = maxDepth;
        this.maxPages = maxPages;
        this.outputDir = outputDir;
        this.threads = threads;
        this.seedFile = seedFile;
        this.maxSize = maxSize;
    }


    private final static Logger log = Logger.getLogger(Worker.class.getName());

    public static void loadSeeds(File path) throws FileNotFoundException {
        Scanner scanner = new Scanner(path);
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

            return !currUrl.getProtocol().isEmpty() && !currUrl.getHost().isEmpty()
                    && isEduPage(currUrl);
        } catch (MalformedURLException e) {
            log.info(String.format("Invalid URL: %s", url));
            return false;
        }
    }

    public static boolean isEduPage(URL url) throws MalformedURLException {
        return InternetDomainName.from(url.getHost()).publicSuffix().toString().equals("edu");
    }
    public static boolean crawlLimitNotMet(){
        return ((FileUtils.sizeOf(new File(OUTPUTDUMP))/Long.valueOf(1000000)) <= CRAWLLIMIT);
    }

    public void beginCrawl() throws InterruptedException, MalformedURLException {

        while ( (!output.isEmpty() || threadPoolExecutor.getActiveCount() > 0)
            && crawlLimitNotMet() /*&& new File(this.outputDir.toString()).list().length */) {
            URLTransaction<String> url = output.take();
            String sanitizedURLstr = sanitize(url);

            if (isURLValid(sanitizedURLstr) && robotsHandler.validate(new URL(sanitizedURLstr))
                    && url.ttl < this.maxDepth && !visitedURLs.map.containsKey( new URL(sanitizedURLstr) )) {
                input.add(new URLTransaction<>(new URL(sanitizedURLstr), Optional.of(url.ttl)));
            }
            visitedURLs.add(sanitizedURLstr);

            threadPoolExecutor.execute(new Worker(input, output, this.outputDir));
        }
        threadPoolExecutor.shutdownNow();
    }

    public void execute() throws FileNotFoundException, MalformedURLException, InterruptedException {
        loadSeeds(this.seedFile);
        log.info("Seed URLs have been loaded");

        beginCrawl();
        log.info("Crawl has completed");
    }
}
