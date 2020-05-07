import java.util.concurrent.Callable;
import picocli.CommandLine.*;
import picocli.CommandLine;

// Documentation at https://picocli.info/quick-guide.html

@Command(
    name = "CLI",   // for lack of a better name :/
    mixinStandardHelpOptions = true
)
class CLI implements Callable<Integer> {

    @Option(
        names = { "--max-depth", "--hops-away", "-h" },
        description = "Maximum crawler depth. Defaults to 6 if not specified."
    )
    Integer maxDepth = 6;

    @Option(
        names = { "--max-pages", "--num-pages", "-p" },
        description = "Maximum pages to crawl. Defaults to 10000 if not specified."
    )
    Integer maxPages = 10000;

    @Option(
        names = "--output-dir",
        description = "Output directory. Required.",
        required = true
    )
    String outputDir;

    @Option(
        names = "--threads",
        description = "Total number of threads, including the manager thread. Defaults to 4 if not specified."
    )
    Integer threads = 4;

    @Option(
        names = { "--seed", "-s", "--seed-file" },
        description = "Path to the seed.txt file. Required.",
        required = true
    )
    String seedFile;

    @Option(
        names = "--help",
        usageHelp = true,
        description = "Display this help and exit."
    )
    boolean help;

    @Override
    public Integer call() throws Exception {

        System.out.println("CLI class called!");
        return 0;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Hello world!");
        int exitCode = (new CommandLine(new CLI())).execute(args);
    }
}