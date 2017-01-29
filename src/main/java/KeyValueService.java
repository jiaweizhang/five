import model.UrlMetadata;

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
     * @param hash
     * @return
     */
    public boolean storeUrl(String url, int expirationInSeconds, String hash);

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
     * @return
     */
    public boolean addDownloadCount(String url);

}
