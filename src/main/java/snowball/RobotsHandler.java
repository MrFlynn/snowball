package snowball;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

import com.panforge.robotstxt.RobotsTxt;

public class RobotsHandler {
    private final HashMap<URL, Optional<RobotsTxt>> handlers;

    public RobotsHandler() {
        this.handlers = new HashMap<>();
    }

    private URL normalizeURL(URL url) throws MalformedURLException {
        return new URL(String.format("%s://%s/robots.txt", url.getProtocol(), url.getHost()));
    }

    private void getRobots(URL url) {
        try {
            RobotsTxt robots = RobotsTxt.read(url.openStream());

            this.handlers.put(url, Optional.of(robots));
        } catch (IOException e) {
            this.handlers.put(url, Optional.empty());
        }
    }

    public boolean validate(URL url) {
        try {
            URL normalized = this.normalizeURL(url);

            if (!this.handlers.containsKey(normalized)) {
                this.getRobots(normalized);
            }

            Optional<RobotsTxt> handler = this.handlers.get(normalized);
            return handler.map(robotsTxt -> robotsTxt.query(null, url.toString())).orElse(true);
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
