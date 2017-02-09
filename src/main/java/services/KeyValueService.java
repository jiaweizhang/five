package services;

import model.FileSetMetadata;
import model.UrlMetadata;

import java.util.List;
import java.util.Set;

/**
 * Created by jiaweizhang on 1/29/2017.
 */
public interface KeyValueService {

    /**
     * Access a URL and provide statistics and download link if file exists
     *
     * @param url
     * @return
     */
    public UrlMetadata accessUrl(String url);

    /**
     * Store a newly created URL and accompanying metadata
     *
     * @param url
     * @param expirationInSeconds
     * @param files
     * @return
     */
    public boolean storeUrl(String url, int expirationInSeconds, List<FileSetMetadata> files);

    /**
     * Determine whether a file exists for a certain URL
     *
     * @param url
     * @return
     */
    public boolean fileExists(String url, String fileName);

    /**
     * Determine whether a URL exists
     *
     * @param url
     * @return
     */
    public boolean urlExists(String url);

    /**
     * Add 1 to the download count
     *
     * @param url
     * @param fileName
     * @return
     */
    public boolean addDownloadCount(String url, String fileName);

    /**
     * Finds an expired URL whose files can be deleted
     *
     * @return the URL if found, null if not found
     */
    public Set<String> findExpiredUrlsIfExist();

    /**
     * Delete all references of a URL and allow it to be reused
     *
     * @param url
     */
    public void deleteUrl(String url);

}
