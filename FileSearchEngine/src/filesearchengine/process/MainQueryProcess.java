package filesearchengine.process;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.DocInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MainQueryProcess {
    
    CorpusType corpusInfo = null;
    Map<String, Float> queryWeightVector;
    Set<String> queryTerms = new HashSet<String>();
    Map<String, Map<Integer, Integer>> invertedIndex = null;
    Integer totalDocs = null;
    Integer totalTerms = null;
    
    Map<String, Float> idfVector;    

    /*
     * key - term
     * value - number of documents containing this term
     */
    Map<String, Integer> termDocCountMap = null;
    
    /*
     * key - DocId
     * Value - document vector
     * */    
    Map<Integer, Float> docScoreMap = new ConcurrentHashMap<Integer, Float>();
    

        
    /**
     *Get the Weight vector corresponding to the query
     * @param queryTerms - query terms as an array
     * @param normalizedTokens - result will be returned in this parameter as set of normalized tokens
     * @return
     */
    public Map<String, Float> getQueryWeightVector(String[] queryTerms, Set<String> normalizedTokens){
        
        //weightVector has to be sorted based on Term value
        Map<String, Float> queryWeightVector = new HashMap<String, Float>();
        
        //tokenize each term in the query and store in strippedTokens
        
        for(String queryTerm : queryTerms){
            Object tokens = CommonUtils.getNormalizedTokens(queryTerm);
            if(tokens != null){
                if(tokens instanceof String){
                    queryTerm = (String)tokens;
                    normalizedTokens.add(queryTerm);
                }
            }
        }
        
        //Construct the query vector
        for (String term : normalizedTokens) {
            if (termDocCountMap.containsKey(term)) { //check if this term is present in corpus
                float weight = idfVector.get(term);
                queryWeightVector.put(term, weight);
            }
        }
        
        return queryWeightVector;
    }
    
    public class QueryThread extends Thread{
        Map<Integer, DocInfo> docIdInfoMap = corpusInfo.getDocIdInfoMap();
        int totalThreadCount = 0;
        int threadIndex = -1;
        
        QueryThread(int totalThreadCount, int threadIndex){
            this.totalThreadCount = totalThreadCount;
            this.threadIndex = threadIndex;
        }
        
        Float getScore(int docId){
            Map<String, Float> docWeightVector = new HashMap<String, Float>();
            
            for(String term : queryTerms){
                Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);
                if(docIdFreqMap != null){
                    //Indicates that this term is present in the corpus
                    Integer termFreqInDoc = docIdFreqMap.get(docId);    
                    //check whether this term is present in the document
                    if(termFreqInDoc != null){
                        //weight = tf*idf
                        float termWeightDoc = idfVector.get(term)*termFreqInDoc;
                        docWeightVector.put(term, termWeightDoc);
                    }
                }
            }
            if(!docWeightVector.isEmpty()){
                Float similarityScore = CommonUtils.getSimilarityScore(queryWeightVector, docWeightVector);
                return similarityScore;
            }
            return null;
        }
        
        @Override
        public void run(){
            for(Integer docId : docIdInfoMap.keySet()){
                //Each docId has to be processed by a unique thread
                if(docId % totalThreadCount == threadIndex){
                    Float score = getScore(docId);
                    if(score != null){
                        docScoreMap.put(docId, score);
                    }
                }
            }
        }
    }
    
    public MainQueryProcess(CorpusType corpusInfo) {
        if(corpusInfo == null){
            throw new RuntimeException("CorpusInfo parameter cannot be null");
        }
        this.corpusInfo = corpusInfo;
        //set the state variables
        totalDocs = corpusInfo.getTotalDocs();
        invertedIndex = corpusInfo.getInvertedIndex();
        totalTerms = (invertedIndex.keySet()).size();
        termDocCountMap = corpusInfo.getTermDocCountMap();
        idfVector = corpusInfo.getIdfVector();
    }
    
    public Map<Integer, Float> searchQuery(String query){
        //Get the weight vector corresponding to the query
        queryWeightVector = getQueryWeightVector(query.split("\\s+"), this.queryTerms);
        
        if(queryWeightVector == null || queryWeightVector.isEmpty()){
            //Query is not present anywhere in the corpus
            System.out.println("Query is not present anywhere in the corpus");
        }
        
        //TODO: change this based on configuration
        int maxThreadCount = 4;
        
        List<Thread> childThreads = new ArrayList<Thread>();
        
        for(int i=0;i<maxThreadCount;i++){
            QueryThread t = new QueryThread(maxThreadCount, i);
            childThreads.add(t);
            t.start();
        }
        
        //wait for all the child threads to finish
        for(Thread childThread : childThreads){
            try {
                childThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return docScoreMap;
    }
}
