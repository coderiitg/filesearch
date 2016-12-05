package com.fileSearch.fileSearchEngine.common;
import java.util.Map;


public class CorpusType {
    
    /*
     * Key - Index associated with it
     * Value - DocInfo of the document
     */
    Map<Integer, DocInfo> docIdInfoMap = null;
    
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
    Map<String, Double> idfVector = null;
    
    int totalDocs = 0;

    public int getTotalDocs() {
        if(termDocCountMap != null){
            return termDocCountMap.keySet().size();
        }
        return totalDocs;        
    }

    public CorpusType(Map<Integer, DocInfo> docIdInfoMap,
                      Map<String, Map<Integer, Integer>> invertedIndex, Map<String, Integer> termDocCountMap, Map<String, Double> idfVector) {
        this.docIdInfoMap = docIdInfoMap;
        this.invertedIndex = invertedIndex;
        this.termDocCountMap = termDocCountMap;  
        this.idfVector = idfVector;
    }
    
    
    public Map<Integer, DocInfo> getDocIdInfoMap() {
        return docIdInfoMap;
    }

    public Map<String, Map<Integer, Integer>> getInvertedIndex() {
        return invertedIndex;
    }

    public Map<String, Integer> getTermDocCountMap() {
        return termDocCountMap;
    }
    
    public Map<String, Double> getIdfVector() {
        return idfVector;
    }
}
