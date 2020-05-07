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

    private boolean getRobots(URL url) {
        URL robotsURL;
        try {
            robotsURL = new URL(String.format("%s://%s/robots.txt", url.getProtocol(), url.getHost()));
            RobotsTxt robots = RobotsTxt.read(robotsURL.openStream());

            this.handlers.put(robotsURL, Optional.of(robots));
            return true;
        } catch (MalformedURLException e) {
          return false;
        } catch (IOException e) {
            this.handlers.put(url, Optional.empty());
            return false;
        }
    }

    public boolean validate(URL url) {
        if (!this.handlers.containsKey(url)) {
            if (!this.getRobots(url)) {
                return false;
            }
        }

        Optional<RobotsTxt> handler = this.handlers.get(url);
        return handler.map(robotsTxt -> robotsTxt.query(null, url.toString())).orElse(true);
    }
}
