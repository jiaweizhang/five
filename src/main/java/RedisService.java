import model.UrlMetadata;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.Map;

/**
 * Created by jiaweizhang on 1/29/2017.
 */
public class RedisService implements KeyValueService {

    private Jedis jedis;

    public RedisService() {
        jedis = new Jedis("localhost", 6379);
        System.out.println("Jedis started");
    }

    @Override
    public UrlMetadata accessUrl(String url) {
        Map<String, String> metadataMap = jedis.hgetAll(url);
        UrlMetadata urlMetadata = new UrlMetadata();
        // if is empty, then exists will be false by default
        if (!metadataMap.isEmpty()) {
            urlMetadata.setExists(true);
            urlMetadata.setAccessCount(Integer.parseInt(metadataMap.get("ac")));
            urlMetadata.setDownloadCount(Integer.parseInt(metadataMap.get("dc")));
            urlMetadata.setFileName(metadataMap.get("fn"));
            urlMetadata.setExpirationTimestamp(new Date().getTime() / 1000 + jedis.ttl(url));

            // add 1 to access count
            jedis.hincrBy(url, "ac", 1);
        }
        return urlMetadata;
    }

    @Override
    public boolean storeUrl(String url, int expirationInSeconds, String hash) {
        return false;
    }

    @Override
    public boolean urlExists(String url) {
        return jedis.hexists(url, "ac");
    }

    @Override
    public boolean addDownloadCount(String url) {
        if (jedis.hexists(url, "dc")) {
            jedis.hincrBy(url, "dc", 1);
            return true;
        }
        return false;
    }

}
