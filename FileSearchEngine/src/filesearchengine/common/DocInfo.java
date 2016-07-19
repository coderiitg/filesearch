package filesearchengine.common;


public class DocInfo {
    //docId associated with this Doc
    private int docId;
    //Full path of this document
    private String filePath;
    //Last modification date of this document in millis
    private long lastModifiedDate;
    
    public void setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public DocInfo(int docId, String filePath){
        this.docId = docId;
        this.filePath = filePath;
    }

    public int getDocId() {
        return docId;
    }

    public String getFilePath() {
        return filePath;
    }
}
