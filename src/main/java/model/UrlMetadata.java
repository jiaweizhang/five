package model;

import java.util.List;

/**
 * Created by jiaweizhang on 1/29/2017.
 */
public class UrlMetadata {
    private int accessCount;
    private long expirationTimestamp;
    private List<FileGetMetadata> files;

    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public List<FileGetMetadata> getFiles() {
        return files;
    }

    public void setFiles(List<FileGetMetadata> files) {
        this.files = files;
    }
}
