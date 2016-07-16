package filesearchengine.common;


public class DocInfo {
    //docId associated with this Doc
    private int docId;
    //Full path of this document
    private String filePath;
    
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
