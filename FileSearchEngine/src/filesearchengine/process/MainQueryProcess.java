package filesearchengine.process;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.DocInfo;
import filesearchengine.common.TokenNormalizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MainQueryProcess {
    
    CorpusType corpusInfo = null;
    Map<String, Double> queryWeightVector;
    Set<String> queryTerms = new HashSet<String>();
    Map<String, Map<Integer, Integer>> invertedIndex = null;
    Integer totalDocs = null;
    Integer totalTerms = null;
    
    Map<String, Double> idfVector;    

    /*
     * key - term
     * value - number of documents containing this term
     */
    Map<String, Integer> termDocCountMap = null;
    
    /*
     * key - DocId
     * Value - document vector
     * This is accessed across multiple threads, hence the need for concurrency
     * */    
    Map<Integer, Double> docScoreMap = new ConcurrentHashMap<Integer, Double>();
    
    
    /**
     *Get the Weight vector corresponding to the query
     * @param queryTerms - query
     * @param normalizedTokens - result will be returned in this parameter as set of normalized tokens
     * @return
     */
    public Map<String, Double> getQueryWeightVector(String query){
        
        //weightVector has to be sorted based on Term value
        Map<String, Double> queryWeightVector = new HashMap<String, Double>();
        
        //tokenize the query and store in normalized Tokens
        List<String> tokens = TokenNormalizer.getNormalizedTokens(query);
        
        //term mapped with its frequency
        Map<String, Integer> termFreqMap = new HashMap<String, Integer>();
        for(String token: tokens){
            if(termFreqMap.containsKey(token)){
                //Increment the frequency
                termFreqMap.put(token, termFreqMap.get(token) + 1);
            }
            else{//encountering this for the first time
                termFreqMap.put(token, 1);
            }
        }
        
        //The term freq map need not be normalized as query vector is common for all documents
        
        //Construct the query vector
        for (String term : termFreqMap.keySet()) {
            if (termDocCountMap.containsKey(term)) { //check if this term is present in corpus
                Double weight = idfVector.get(term);
                //weight = tf*idf
                queryWeightVector.put(term, termFreqMap.get(term)*weight);
            }
        }
        
        //set the global variable query terms
        this.queryTerms.addAll(termFreqMap.keySet());
        
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
        
        Double getScore(int docId){
            Map<String, Integer> docTermFreqMap = new HashMap<String, Integer>();
            //Construct the normalized term freq
            
            int sumFreqSquares = 0;
            
            for(String term : queryTerms){
                Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);
                if(docIdFreqMap != null){
                    //Indicates that this term is present in the corpus
                    Integer termFreqInDoc = docIdFreqMap.get(docId);    
                    //check whether this term is present in the document
                    if(termFreqInDoc != null){
                        //Add it to sum of squares
                        sumFreqSquares += termFreqInDoc*termFreqInDoc;
                        docTermFreqMap.put(term, termFreqInDoc);
                    }
                }
            }
            
            
            if(!docTermFreqMap.isEmpty()){//check if atleast one term is present in the document
                Double euclideanNorm = Math.sqrt(sumFreqSquares);
                Map<String, Double> docTermWeightMap = new HashMap<String, Double>();
                //Multiply docTermFreq vector with IDF
                //TF*IDF
                for(String term : docTermFreqMap.keySet()){
                    docTermWeightMap.put(term, ((docTermFreqMap.get(term)*idfVector.get(term))/euclideanNorm));    
                }
                
                
                Double similarityScore = CommonUtils.getSimilarityScore(queryWeightVector, docTermWeightMap);
                return similarityScore;
            }
            return null;
        }
        
        @Override
        public void run(){
            for(Integer docId : docIdInfoMap.keySet()){
                //Each docId has to be processed by a unique thread
                if(docId % totalThreadCount == threadIndex){
                    Double score = getScore(docId);
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
    
    public Map<Integer, Double> triggerQuery(String query){
        //Get the weight vector corresponding to the query
        queryWeightVector = getQueryWeightVector(query);
        
        if(queryWeightVector == null || queryWeightVector.isEmpty()){
            //Query is not present anywhere in the corpus
            System.out.println("Query is not present anywhere in the corpus");
            return null;
        }
        
        //TODO: change this based on configuration
        int maxThreadCount = 4;
        
        List<Thread> childThreads = new ArrayList<Thread>();
        
        for(int i=0;i<maxThreadCount;i++){
            QueryThread t = new QueryThread(maxThreadCount, i);
            childThreads.add(t);
            t.start();
        }
        //Each thread will work on different set of documents and calculate the similarity score
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
