package snowball;

import java.util.concurrent.*;
import java.util.Map;
import java.net.URL;

class VisitedURLs {

    public VisitedURLs() {

        map = new ConcurrentHashMap<URL, Integer>();
    }

    public VisitedURLs(int initialCapacity) {

        map = new ConcurrentHashMap<URL, Integer>(initialCapacity);
    }

    // Returns the freq of URL after the insertion
    public Integer add(URL url) {

        int freq = map.getOrDefault(url, 0);
        map.put(url, freq + 1);
        return freq + 1;
    }

    // Returns true if the url was properly removed,
    // false if the url wasn't in the map
    public boolean remove(URL url) {

        if (!map.containsKey(url)) return false;

        Integer freq = map.get(url);
        assert freq != null
            : "map doesn't contain \"" + url.toString() + "\"";
        assert freq > 0
            : "map.get(\"" + url.toString() +
              ") returned negative value " + freq.toString();

        if (freq == 1)
            map.remove(url);
        else
            map.put(url, freq - 1);

        return true;
    }

    public Integer size() {

        return map.size();
    }

    private Map<URL, Integer> map;
}