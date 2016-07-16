package filesearchengine.common;

import java.util.Map;


public class CorpusType {

    /*
     * Key - Full file path of a Document
     * Value - Index associated with it
     */
    Map<String, Integer> fileDocIdMap = null;
    
    /*
     * Key - Index associated with it
     * Value - DocInfo of the document
     */
    Map<Integer, DocInfo> docIdFileMap = null;
    
    /*
    Key - Term
    Value - Map
              |-> Key - DocumentId
              |-> Value - Term frequency in the document 
    */
    Map<String, Map<Integer, Integer>> invertedIndex = null;
    
    /*
    key - Term
    value - number of Documents containing this Term
    */
    Map<String, Integer> termDocCountMap = null;

    /*
     * key - term
     * Value - Inverted Document Frequency value
     */
    Map<String, Float> idfVector = null;
    
    int totalDocs = 0;

    public int getTotalDocs() {
        if(termDocCountMap != null){
            return termDocCountMap.keySet().size();
        }
        return totalDocs;        
    }

    public CorpusType(Map<String, Integer> fileDocIdMap, Map<Integer, DocInfo> docIdFileMap,
                      Map<String, Map<Integer, Integer>> invertedIndex, Map<String, Integer> termDocCountMap, Map<String, Float> idfVector) {
        this.fileDocIdMap = fileDocIdMap;
        this.docIdFileMap = docIdFileMap;
        this.invertedIndex = invertedIndex;
        this.termDocCountMap = termDocCountMap;  
        this.idfVector = idfVector;
    }
    
    public Map<String, Integer> getFileDocIdMap() {
        return fileDocIdMap;
    }
    
    public Map<Integer, DocInfo> getDocIdFileMap() {
        return docIdFileMap;
    }

    public Map<String, Map<Integer, Integer>> getInvertedIndex() {
        return invertedIndex;
    }

    public Map<String, Integer> getTermDocCountMap() {
        return termDocCountMap;
    }
    
    public Map<String, Float> getIdfVector() {
        return idfVector;
    }
}
