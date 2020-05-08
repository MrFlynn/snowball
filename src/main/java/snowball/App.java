package snowball;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "snowball",
    mixinStandardHelpOptions = true
)
class App implements Callable<Integer> {
    @Option(
        names = { "--max-depth", "--hops-away" },
        description = "Maximum crawler depth. Defaults to 6 if not specified."
    )
    private Integer maxDepth = 6;

    @Option(
        names = { "--max-pages", "--num-pages", "-p" },
        description = "Maximum pages to crawl. Defaults to 10000 if not specified."
    )
    private Integer maxPages = 10000;

    @Option(
        names = "--output-dir",
        description = "Output directory. Required.",
        required = true
    )
    private Path outputDir;

    @Option(
        names = "--threads",
        description = "Total number of threads, including the manager thread. Defaults to 4 if not specified."
    )
    private Integer threads = 4;

    @Option(
        names = { "--seed", "-s", "--seed-file" },
        description = "Path to the seed.txt file. Defaults to seeds.txt if not specified."
    )
    private File seedFile = new File("./seeds.txt");

    @Option(
        names = "--max-size",
        description = "Maximum size of document collection in MB. Defaults to 20480 MB."
    )
    private Integer maxSize = 20480;

    @Override
    public Integer call() throws Exception {
        Manager manager = new Manager(maxDepth, maxPages, outputDir, threads, seedFile, maxSize);
        manager.execute();

        return 0;
    }

    public static void main(String ...args) {
        int exitCode;

        try {
            exitCode = new CommandLine(new App()).execute(args);
        } catch (Exception e) {
            exitCode = 1;
            System.err.println(e.toString());
        }

        System.exit(exitCode);
    }
}