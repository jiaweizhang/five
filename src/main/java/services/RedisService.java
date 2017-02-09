package services;

import model.FileGetMetadata;
import model.FileSetMetadata;
import model.UrlMetadata;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created by jiaweizhang on 1/29/2017.
 */
public class RedisService implements KeyValueService {

    private Jedis jedis;

    public RedisService() {
        jedis = new Jedis("localhost", 6379);
        System.out.println("Jedis started");
        jedis.flushDB();
    }

    @Override
    public UrlMetadata accessUrl(String url) {
        // can make assumption that it exists because of prior checking
        // add 1 to access count
        jedis.hincrBy("url:" + url, "ac", 1);

        Map<String, String> urlMeta = jedis.hgetAll("url:" + url);
        UrlMetadata urlMetadata = new UrlMetadata();
        urlMetadata.setAccessCount(Integer.parseInt(urlMeta.get("ac")));
        urlMetadata.setExpirationTimestamp(new Date().getTime() / 1000 + jedis.ttl("url:" + url));

        List<FileGetMetadata> fileGetMetadataList = new ArrayList<>();

        for (String fileNumber : urlMeta.get("files").split(",")) {
            Map<String, String> fileMeta = jedis.hgetAll("file:" + fileNumber);
            FileGetMetadata fileGetMetadata = new FileGetMetadata();
            fileGetMetadata.setDownloadCount(Long.parseLong(fileMeta.get("dc")));
            fileGetMetadata.setFileName(fileMeta.get("fn"));
            fileGetMetadata.setHash(fileMeta.get("hash"));
            fileGetMetadataList.add(fileGetMetadata);
        }
        urlMetadata.setFiles(fileGetMetadataList);

        return urlMetadata;
    }

    @Override
    public boolean storeUrl(String url, int expirationInSeconds, List<FileSetMetadata> files) {
        StringBuilder sb = new StringBuilder();
        for (FileSetMetadata fileSetMetadata : files) {
            long fileNumber = jedis.incr("file");
            sb.append(fileNumber).append(",");
            jedis.hset("file:" + fileNumber, "dc", "0");
            jedis.hset("file:" + fileNumber, "hash", fileSetMetadata.getHash());
            jedis.hset("file:" + fileNumber, "fn", fileSetMetadata.getFileName());
            jedis.expire("file:" + fileNumber, expirationInSeconds);

            // map url + fileName to fileNumber
            jedis.set("urlFile:" + url + ":" + fileSetMetadata.getFileName(), Long.toString(fileNumber));
            jedis.expire("urlFile:" + url + ":" + fileSetMetadata.getFileName(), expirationInSeconds);

            // store sorted set for file deletion
            jedis.zadd("ssexpiration", (System.currentTimeMillis() / 1000 + expirationInSeconds), url);
        }
        jedis.hset("url:" + url, "ac", "0");
        jedis.hset("url:" + url, "files", sb.deleteCharAt(sb.length() - 1).toString());
        jedis.expire("url:" + url, expirationInSeconds);

        // add url to existing URLs with no expiration
        jedis.set("urlExists:" + url, "1");
        return true;
    }

    @Override
    public boolean fileExists(String url, String fileName) {
        return jedis.exists("urlFile:" + url + ":" + fileName);
    }

    @Override
    public boolean urlExists(String url) {
        if (jedis.hexists("url:" + url, "ac")) {
            // check deeper
            return jedis.exists("urlExists:" + url);
        }
        return false;
    }

    @Override
    public boolean addDownloadCount(String url, String fileName) {
        String fileNumber = jedis.get("urlFile:" + url + ":" + fileName);
        if (fileNumber != null) { // TODO no real need to check this as it is prevalidated
            // file exists

            jedis.hincrBy("file:" + fileNumber, "dc", 1);
            return true;
        }
        return false;
    }

    @Override
    public Set<String> findExpiredUrlsIfExist() {
        // TODO look into 1-1 long -> double conversion
        return jedis.zrange("ssexpiration", 0, System.currentTimeMillis() / 1000);
    }

    @Override
    public void deleteUrl(String url) {
        // TODO use a lock to ensure no creation takes place while this deletion is occuring
        // delete from sorted set
        jedis.zrem("ssexpiration", url);
        // delete frome exists
        jedis.del("urlExists:" + url);
    }

}
