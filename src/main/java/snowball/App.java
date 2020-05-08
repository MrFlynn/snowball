package snowball;

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
    public Integer maxDepth = 6;

    @Option(
        names = { "--max-pages", "--num-pages", "-p" },
        description = "Maximum pages to crawl. Defaults to 10000 if not specified."
    )
    public Integer maxPages = 10000;

    @Option(
        names = "--output-dir",
        description = "Output directory. Required.",
        required = true
    )
    public String outputDir;

    @Option(
        names = "--threads",
        description = "Total number of threads, including the manager thread. Defaults to 4 if not specified."
    )
    public Integer threads = 4;

    @Option(
        names = { "--seed", "-s", "--seed-file" },
        description = "Path to the seed.txt file. Defaults to seeds.txt if not specified."
    )
    public String seedFile = "seeds.txt";

    @Override
    public Integer call() throws Exception {
        System.out.printf("%d, %d, %s, %d, %s", maxDepth, maxPages, outputDir, threads, seedFile);
        return 0;
    }

    public String showArgs() {
        return "maxDepth:  " + Integer.toString(maxDepth) + "\n" +
                "maxPages:  " + Integer.toString(maxPages) + "\n" +
                "outputDir: " + outputDir + "\n" +
                "threads:   " + Integer.toString(threads) + "\n" +
                "seedFile:  " + seedFile;
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