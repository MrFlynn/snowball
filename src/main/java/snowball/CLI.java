package snowball;

import java.util.concurrent.Callable;
import java.util.Arrays;
import picocli.CommandLine.*;
import picocli.CommandLine;

// Documentation at https://picocli.info/quick-guide.html

@Command(
    name = "CLI",   // for lack of a better name :/
    mixinStandardHelpOptions = true
)
class CLI implements Callable<Integer> {

    private String[] args;

    public CLI(String[] args) {

        this.args = Arrays.copyOf(args, args.length);
        Integer exitCode;

        try {

            exitCode = new CommandLine(this).execute(args);

        } catch (Exception e) {

            exitCode = 1;
            System.err.println(e.toString());
        }
    }

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
        description = "Path to the seed.txt file. Required.",
        required = true
    )
    public String seedFile;

    // @Option(
    //     names = "--help",
    //     usageHelp = true,
    //     description = "Display this help and exit."
    // )
    // boolean help;

    // This is called by CommandLine.execute(),
    // so don't move that function call here
    // unless you like your stack blown up
    @Override
    public Integer call() throws Exception {

        return 0;
    }

    public String showArgs() {

        StringBuilder sb = new StringBuilder();

        sb.append("maxDepth:  " + Integer.toString(maxDepth) + "\n");
        sb.append("maxPages:  " + Integer.toString(maxPages) + "\n");
        sb.append("outputDir: " + outputDir + "\n");
        sb.append("threads:   " + Integer.toString(threads) + "\n");
        sb.append("seedFile:  " + seedFile);

        return sb.toString();
    }

    // Example usage,
    // given here in case flow of control changes
    // public static void main(String[] args) throws Exception {

    //     System.out.println("In CLI.main()");
    //     System.out.println("args: " + Arrays.toString(args));
    //     int exitCode = new CommandLine(new CLI()).execute(args);
    // }
}