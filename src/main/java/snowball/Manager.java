package snowball;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.io.File;

import com.google.common.net.InternetDomainName;
import org.apache.commons.io.FileUtils;

import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class Manager {
    static LinkedBlockingQueue<URLTransaction<URL>> input = new LinkedBlockingQueue<>(); //potentially unsafe urls
    static LinkedBlockingQueue<URLTransaction<String>> output = new LinkedBlockingQueue<>(); //output sanitized urls
    static RobotsHandler robotsHandler = new RobotsHandler();
    static VisitedURLs visitedURLs  = new VisitedURLs();
    private final Integer maxDepth;
    private final Integer maxPages;
    private final Path outputDir;
    private final File seedFile;
    private final Integer maxSize;
    private final Integer threads;
    private final ThreadPoolExecutor threadPoolExecutor;

    private static final Pattern htmlPattern = Pattern.compile("^(.*\\.htm|.*\\.html|^([^.]+))$");

    public Manager(Integer maxDepth, Integer maxPages, Path outputDir, Integer threads,
                   File seedFile, Integer maxSize) {
        this.maxDepth = maxDepth;
        this.maxPages = maxPages;
        this.outputDir = outputDir;
        this.seedFile = seedFile;
        this.maxSize = maxSize;
        this.threads = threads;

        this.threadPoolExecutor = new ThreadPoolExecutor(
            threads,
            threads,
            2,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>()
        );
    }

    private final static Logger log = Logger.getLogger(Worker.class.getName());

    public static void loadSeeds(File path) throws FileNotFoundException {
        Scanner scanner = new Scanner(path);
        while (scanner.hasNextLine()) {
            URLTransaction<String> seed = new URLTransaction<>(scanner.nextLine(), Optional.of(0));
            output.add(seed);
        }
    }

    public static String sanitize(URLTransaction<String> url)  {
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

    public static boolean isHTML(URL url) {
        return htmlPattern.matcher(url.getFile()).find();
    }

    public static boolean isURLValid(String url) {
        try {
            URL currUrl = new URL(url);

            return !currUrl.getProtocol().isEmpty() && !currUrl.getHost().isEmpty()
                    && isEduPage(currUrl) && isHTML(currUrl);
        } catch (MalformedURLException e) {
            log.info(String.format("Invalid URL: %s", url));
            return false;
        }
    }

    public static boolean isEduPage(URL url) {
        try {
            //noinspection UnstableApiUsage
            return InternetDomainName.from(url.getHost()).publicSuffix().toString().equals("edu");
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean crawlSizeLimitNotMet() {
        return ((FileUtils.sizeOf(new File(this.outputDir.toString()))/ 1000000L) <= this.maxSize);
    }

    public boolean continueCrawl(){
        if( (!output.isEmpty() || threadPoolExecutor.getActiveCount() > 0) &&
                Objects.requireNonNull(new File(this.outputDir.toString()).list()).length <= this.maxPages ){
            if (this.maxSize != null ){
                return crawlSizeLimitNotMet();
            }
            return true;
        }

        return false;
    }

    public void beginCrawl() throws InterruptedException, MalformedURLException {
        for (int i = 0; i < this.threads; i++) {
            threadPoolExecutor.execute(new Worker(input, output, this.outputDir));
        }

        while (continueCrawl()) {
            URLTransaction<String> url = output.take();
            String sanitizedURLString = sanitize(url);

            if (isURLValid(sanitizedURLString) && robotsHandler.validate(new URL(sanitizedURLString))
                    && url.ttl < this.maxDepth
                    && !visitedURLs.map.containsKey(new URL(sanitizedURLString).toString())) {
                input.add(new URLTransaction<>(new URL(sanitizedURLString), Optional.of(url.ttl)));
            }

            visitedURLs.add(sanitizedURLString);
        }

        threadPoolExecutor.shutdownNow();
    }

    public void execute()
            throws FileNotFoundException, MalformedURLException, InterruptedException, NoSuchMethodException {
        File outDir = this.outputDir.toFile();
        if (!(outDir.exists() && outDir.isDirectory())) {
            log.warning(String.format("Output directory %s does not exist. Creating...", this.outputDir.toString()));
            if (!outDir.mkdir()) {
                log.throwing(
                        Manager.class.getName(),
                        Manager.class.getMethod("beginCrawl").getName(),
                        new IOException(String.format("Could not create output directory %s", outDir.toString()))
                );
            }
        }

        loadSeeds(this.seedFile);
        log.info("Seed URLs have been loaded");

        beginCrawl();
        
        System.out.println(this.maxSize);
        log.info("Crawl has completed");
    }
}
