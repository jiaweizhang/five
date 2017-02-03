package model;

/**
 * Created by jiaweizhang on 2/2/2017.
 */
public class FileGetMetadata {
    private long downloadCount;
    private String hash;
    private String fileName;

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
