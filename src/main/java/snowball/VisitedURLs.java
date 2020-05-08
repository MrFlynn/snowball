package snowball;

import java.util.concurrent.*;
import java.util.Map;
import java.net.URL;
import java.math.BigInteger;

class VisitedURLs {

    public VisitedURLs() {

        map = new ConcurrentHashMap<String, Integer>();
        totalSize = BigInteger.ZERO;
    }

    public VisitedURLs(int initialCapacity) {

        map = new ConcurrentHashMap<String, Integer>(initialCapacity);
        totalSize = BigInteger.ZERO;
    }

    // Returns the freq of URL after the insertion
    public Integer add(String url) {

        totalSize = totalSize.add(BigInteger.ONE);

        int freq = map.getOrDefault(url, 0);
        map.put(url, freq + 1);
        return freq + 1;
    }

    // Returns true if the url was properly removed,
    // false if the url wasn't in the map
    public boolean remove(String url) {

        if (!map.containsKey(url)) return false;

        Integer freq = map.get(url);
        assert freq != null
            : "map doesn't contain \"" + url.toString() + "\"";
        assert freq > 0
            : "map.get(\"" + url.toString() +
              ") returned negative value " + freq.toString();

        totalSize = totalSize.subtract(BigInteger.ONE);

        if (freq == 1)
            map.remove(url);
        else
            map.put(url, freq - 1);

        return true;
    }

    // includeDuplicates=true  to count _all_ url's seen,
    // includeDuplicates=false to only count distinct urls
    public long size(boolean includeDuplicates) {

        return includeDuplicates ? totalSize.longValue() : (long) map.size();
    }

    public Map<String, Integer> map;
    BigInteger totalSize;
}