package filesearchengine.process;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;

import filesearchengine.common.DocInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;


public class IndexBuilder {
    public IndexBuilder() {
        super();
    }
    
    //Indicates the next index that can be assigned to a new Document
    int nextDocIndex = 0;
    
    /*
     * Key - Full file path of a Document
     * Value - Index associated with it
     */
    Map<String, Integer> fileDocIdMap = new HashMap<String, Integer>();
    
    /*
     * Key - Index associated with it
     * Value - DocInfo of the document
     */
    Map<Integer, DocInfo> docIdFileMap = new HashMap<Integer, DocInfo>();
    
    /*
    Key - Term
    Value - Map
              |-> Key - DocumentId
              |-> Value - Term frequency in the document 
    */
    Map<String, Map<Integer, Integer>> invertedIndex = new HashMap<String, Map<Integer, Integer>>();
    
    /*
    key - Term
    value - number of Documents containing this Term
    */
    Map<String, Integer> termDocCountMap = new HashMap<String, Integer>();
    
    /*
     * key - term
     * Value - Inverted Document Frequency value
     */
    Map<String, Float> idfVector = new HashMap<String, Float>();
    
    public Map<String, Float> getIdfVector() {
        return idfVector;
    }
    
    
    private void insertTerm(String filePath, String term){
        if(!fileDocIdMap.containsKey(filePath)){
            fileDocIdMap.put(filePath, ++nextDocIndex);
            //Create a new Doc Information object and put it in a map
            docIdFileMap.put(nextDocIndex, new DocInfo(nextDocIndex, filePath));
        }
        
        Integer curDocId = fileDocIdMap.get(filePath);
        
        if(!invertedIndex.containsKey(term)){
            Map<Integer, Integer> docIdFreqMap = new HashMap<Integer, Integer>();
            //Insert doc info
            docIdFreqMap.put(curDocId, 1);
            //Insert the map into invertedIndex
            invertedIndex.put(term, docIdFreqMap);
            //Set the Document Count against the term
            termDocCountMap.put(term, 1);
        }
        else{
            //Iterate through docIdFreqMap
            Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);
            Integer curDocIdFreq = docIdFreqMap.get(curDocId);
            if(curDocIdFreq != null){
                docIdFreqMap.put(curDocId, ++curDocIdFreq);
            }
            else{
                //Insert doc info
                docIdFreqMap.put(curDocId, 1);
                //Set the Document Count against the term
                termDocCountMap.put(term, 1);
            }
        }
    }
    
    /**
     *
     * @param file
     */
    private void readFile(File file){
        BufferedReader is = null;

        try {
            is = new BufferedReader(new FileReader(file));
            String line = null;
            
            while((line = is.readLine()) != null){
                //split string around whitespaces
                String[] words = line.split("\\s+");
                for(String word : words){
                    //tokenize this word
                    Object tokens = CommonUtils.getNormalizedTokens(word);
                    if(tokens == null){
                        //skip this
                        continue;
                    }
                    if(tokens instanceof String){
                        insertTerm(file.getPath(), (String)tokens);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found at : " + file.getPath());
        }
        catch(IOException e){
            System.out.println("Exception while reading : " + file.getPath());
        }
        finally{
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }
    
    private void constructIDFVector(){
        if(termDocCountMap != null && !termDocCountMap.isEmpty()){
            int totalDocs = docIdFileMap.keySet().size();
            
            for(String term : termDocCountMap.keySet()){
                float termWeight = CommonUtils.round(Math.log(totalDocs/termDocCountMap.get(term)), 2);
                idfVector.put(term, termWeight);
            }
        }
    }
    
    /**
     *
     * @param rootDirFullPath
     */
    private void reBuildIndex(String rootDirFullPath) {
        File dir = new File(rootDirFullPath);
        
        File[] listFiles = dir.listFiles();
        if(listFiles != null){
            for(File childFile : listFiles){
                if(!childFile.isDirectory()){
                    //This is not a directory
                    readFile(childFile);
                }
            }
        }
        constructIDFVector();
    }
    
    public void displayIndex(){
        System.out.println("***********BEGIN Inverted Index CONTENT***********");
        if(invertedIndex != null && !invertedIndex.isEmpty()){
            for(String term : invertedIndex.keySet()){
                System.out.print(term + " => ");
                //Iterate through docIdFreqMap
                Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);
                for(Integer docId : docIdFreqMap.keySet()){
                    System.out.print("{" + docId + ":" + docIdFreqMap.get(docId) + "}, ");    
                }
                System.out.println();
            }
        }
        System.out.println("***********END Inverted Index CONTENT***********");
    }
    

    
    //Tries to fetch Index from secondary storage memory into main memory
    private void fetchIndexFromStorage(String rootDirFullPath){
        //TODO: First check whether the index is present in storage
        //need not build the index always
        reBuildIndex(rootDirFullPath);
        
    }
    
    public CorpusType getCorpusInfo(String rootDirFullPath){
        //Re-populate all the index related fields
        fetchIndexFromStorage(rootDirFullPath);
        CorpusType corpusInfo = new CorpusType(fileDocIdMap, docIdFileMap, invertedIndex, termDocCountMap, idfVector);
        return corpusInfo;
    }
}
