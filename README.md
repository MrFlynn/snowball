# snowball
Java-based web scraper created for CS172: Information Retrieval.

## Usage
Before continuing, you will need a text file with a list of URLs (seeds). 
Create a file called `seeds.txt` and add one URL per line. Make sure to include
the protocol (i.e. `http://` or `https://`).

The recommended method for using this application is with Docker. To run the
application in it's most basic configuration run the following commands:

```bash
$ mkdir /output
$ docker run \
  -v $(pwd)/seeds.txt:/seeds.txt \
  -v $(pwd)/output:/output \
  mrflynn/snowball
```

To see a full list of options, simply use the `--help` flag like so:
```bash
$ docker run \
  -v $(pwd)/seeds.txt:/seeds.txt \
  -v $(pwd)/output:/output \
  mrflynn/snowball --help
```
