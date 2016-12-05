package com.fileSearch.fileSearchEngine.process;

import com.fileSearch.fileSearchEngine.common.CommonUtils;
import com.fileSearch.fileSearchEngine.common.CorpusType;
import com.fileSearch.fileSearchEngine.common.CustomFileFilter;
import com.fileSearch.fileSearchEngine.common.DocInfo;
import static com.fileSearch.fileSearchEngine.common.SearchEngineConstants.EXTNS_SEARCH;
import static com.fileSearch.fileSearchEngine.common.SearchEngineConstants.FILENAME_PATTERN;
import static com.fileSearch.fileSearchEngine.common.SearchEngineConstants.MAX_CORPUS_SIZE;
import static com.fileSearch.fileSearchEngine.common.SearchEngineConstants.RECURSIVE_SEARCH;
import static com.fileSearch.fileSearchEngine.common.SearchEngineConstants.SKIP_HIDDEN_ITEMS;
import com.fileSearch.fileSearchEngine.common.TokenNormalizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;


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
    Map<String, Integer> fileDocIdMap = new TreeMap<String, Integer>();

    /*
     * Key - Index associated with it
     * Value - DocInfo of the document
     */
    Map<Integer, DocInfo> docIdInfoMap = new HashMap<Integer, DocInfo>();

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
    Map<String, Double> idfVector = new HashMap<String, Double>();

    /**
     * key - extension
     * value - true if extension is Binary, false otherwise
     */
    Map<String, Boolean> extensionBinary = new HashMap<String, Boolean>();
    
    public Map<String, Double> getIdfVector() {
        return idfVector;
    }

    /**
     *Insert a term into index
     * @param docId - the Document Id of doc which contains the term
     * @param term
     */
    private void insertTerm(int docId, String term) {


        if (!invertedIndex.containsKey(term)) {
            //this term is encountered for the first time in corpus
            Map<Integer, Integer> docIdFreqMap = new HashMap<Integer, Integer>();
            //Set the frequency of term in document
            docIdFreqMap.put(docId, 1);
            //Insert the map into invertedIndex against the term
            invertedIndex.put(term, docIdFreqMap);
            //Set the Document Count against the term
            termDocCountMap.put(term, 1);
        } else {
            //the term was already encountered before
            //get the docIdFreqMap corresponding to the term
            Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);
            //the number of instances of term in this document
            Integer curDocIdFreq = docIdFreqMap.get(docId);
            if (curDocIdFreq != null) {
                docIdFreqMap.put(docId, ++curDocIdFreq);
            } else {
                //this is the first occurrences of term in the document
                docIdFreqMap.put(docId, 1);
                //incremment the number of documents containing this term
                termDocCountMap.put(term, termDocCountMap.get(term) + 1);
            }
        }
    }

    /**
     *
     * @param file
     * @return a unique id for file if it's text file, -1 otherwise
     * @throws FileNotFoundException
     * @throws IOException
     */
    private int readFile(File file) throws FileNotFoundException, IOException {
        BufferedReader is = null;

        try {
            is = new BufferedReader(new FileReader(file));
            String line = null;
            String filePath = file.getCanonicalPath();
            
            //First checks wether the file is binary or text and proceeeds only if it's binary
            //This is done by reading atleast 512 characters
            StringBuilder sb = new StringBuilder();
            
            //indicates whether the file is binary or text
            boolean isFileBinary = false;
            
            while ((line = is.readLine()) != null) {
                sb.append(line);
                if (sb.length() >= 512) {
                    //If length of chars read has already exceeded then check whether the block is text
                    isFileBinary = CommonUtils.isBlockBinary(sb.toString());
                    if (isFileBinary) {
                        break;
                    }
                }
            }
            
            //If the file is binary, then no need to proceed further, so return -1 indicating that file isn't text
            if(isFileBinary){
                return -1;
            }
            
            //Otherwise the file is determined to be text type
            //Check whether file is already indexed
            if (!fileDocIdMap.containsKey(filePath)) {
                //If not create a new index
                fileDocIdMap.put(filePath, ++nextDocIndex);
                //Create a new Doc Information object and put it in a map
                docIdInfoMap.put(nextDocIndex, new DocInfo(nextDocIndex, filePath));
            }

            Integer docId = fileDocIdMap.get(filePath);
            
            //Reinitialize the valaue of line as some of the contents have been already read
            line = sb.toString();
            
            while (line != null) {
                //Get the normalized tokens from line i.e string values that can be considered as words
                List<String> normalizedTokens = TokenNormalizer.getNormalizedTokens(line);
                if (normalizedTokens != null) {
                    for (String token : normalizedTokens) {
                        insertTerm(docId, token);
                    }
                }
                //Read the next line
                line = is.readLine();
            }

            //Set the last modified date in document info

            DocInfo docInfo = docIdInfoMap.get(docId);
            //Get the last modified date of file
            Long lastModifiedDate = file.lastModified();
            docInfo.setLastModifiedDate(lastModifiedDate);
            //Set the file size
            docInfo.setFileSize(file.length());
            return docId;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

    /**
     *Remove the filePath related information from invertedIndex
     * @param filePath
     */
    private void remFileFromIndex(String filePath) {
        if (fileDocIdMap.containsKey(filePath)) { //if the filepath is actaually indexed
            //get the docId
            int docId = fileDocIdMap.get(filePath);

            //remove the file information from inverted index first
            for (String term : invertedIndex.keySet()) {
                Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);

                //Does the document contain this term
                if (docIdFreqMap.containsKey(docId)) {
                    docIdFreqMap.remove(docId);

                    //Decrement the numDocs containing this term
                    int numDocsWithTerm = termDocCountMap.get(term);
                    if (numDocsWithTerm == 1) {
                        /*
                         * If only the current document contains this term, then remove it from termDocCountMap
                        */
                        termDocCountMap.remove(docId);
                    } else {
                        //decrement the count
                        termDocCountMap.put(term, --numDocsWithTerm);
                    }

                    //If docIdFreqMap has become empty because of removing the term, then term itself can be removed
                    if (docIdFreqMap.isEmpty()) {
                        invertedIndex.remove(docId);
                    }
                }
            }
            //remove the docId from Corpus
            docIdInfoMap.remove(docId);

            //remove file path from corpus
            fileDocIdMap.remove(filePath);
        }
    }

    /**
     *This method is to be called when the corpus size goes beond a certain limit,
     * It will remove all the references to the files within the dirPath which are not being immediately used
     * for searching the file pattern, thus reducing the corpus size
     * @param dirPath
     * @param curIndexedFiles
     * @param recursiveSearch
     */
    private void cleanupUnusedFilesInDir(String dirPath, Set<Integer> curIndexedFiles,
                                      boolean recursiveSearch) {
        //Get the submap of the all the files indexed under dirPath
        final Map<String, Integer> fileDocIdSubMap =
            CommonUtils.fetchFileDocIdSubMap((SortedMap<String, Integer>) fileDocIdMap, dirPath, recursiveSearch);

        if (fileDocIdSubMap != null && !fileDocIdSubMap.isEmpty()) {
            
            //clone the fileDocIdSubMap's values
            //This is a set of previoulsy indexed docIds in the directory
            Set<Integer> docIdsToRemove = new HashSet<Integer>(fileDocIdSubMap.values());
            
            //Retain all the currently indexed files
            docIdsToRemove.removeAll(curIndexedFiles);
            
            for (int docIdToRemove : docIdsToRemove) {
                //Get the file path assigned
                String indexedFilePath = docIdInfoMap.get(docIdToRemove).getFilePath();
                //Remove the file from corpus
                remFileFromIndex(indexedFilePath);
            }
        }
    }
    
    private void constructIDFVector() {
        if (termDocCountMap != null && !termDocCountMap.isEmpty()) {
            int totalDocs = docIdInfoMap.keySet().size();

            for (String term : termDocCountMap.keySet()) {
                double termWeight = CommonUtils.round(Math.log(totalDocs / termDocCountMap.get(term)), 4);
                idfVector.put(term, termWeight);
            }
        }
    }

    private class ReBuildIndex{
        private boolean recurse = false;
        private boolean skipHidden = true;
        private Set<String> selectedExtns = CustomFileFilter.allSuppExtns;
        
        //File Name pattern if any
        private Pattern fileNamePattern = null;
        
        //Stores the list of files indexed in this re-build index process
        private Set<Integer> curIndexedFiles = new HashSet<Integer>();
        
        //indicates whether extension binary type has to be cached
        //TODO: in future accpet this parameter to be configured from UI
        private boolean extensionTypeCache = true;
        
        private ReBuildIndex(Map<String, Object> searchParams){
            //should recursive search be performed
            this.recurse = (Boolean)searchParams.get(RECURSIVE_SEARCH);
            this.skipHidden = (Boolean)searchParams.get(SKIP_HIDDEN_ITEMS);
            this.selectedExtns = (Set<String>)searchParams.get(EXTNS_SEARCH);
            //If the fileName parameter is not provided, this will be NULL
            this.fileNamePattern = (Pattern)searchParams.get(FILENAME_PATTERN);
        }
        
        /**
         *
         * @param rootDirFullPath
         * @return
         * @throws FileNotFoundException
         * @throws IOException
         */
        private Set<Integer> reBuild(String rootDirFullPath) throws FileNotFoundException, IOException {
            File rootDir = new File(rootDirFullPath);
            //call the re-build process starting from rootDir
            reBuild(rootDir);
            
            //TODO:// devise a better strategy for cleaning up least frequently accessed files
            if(fileDocIdMap.size() > MAX_CORPUS_SIZE)
                cleanupUnusedFilesInDir(rootDirFullPath, curIndexedFiles, recurse);
            
            
            //Re-construct IDF vector
            constructIDFVector();
            
            return curIndexedFiles;
        }

        /**
         *
         * @param rootDir
         * @throws FileNotFoundException
         * @throws IOException
         */
        private void reBuild(File rootDir) throws FileNotFoundException, IOException {
            //Fetch only the files with selected Extns
            //File[] listFiles = rootDir.listFiles(new CustomFileFilter(selectedExtns));
            File[] listFiles = rootDir.listFiles();
            
            if (listFiles != null) {
                for (File childFile : listFiles) {
                    //skip processing if skiphidden files is set and childFile is hidden
                    if (skipHidden && childFile.isHidden()) {
                        continue;
                    }
                    if (!childFile.isDirectory()) {
                        //This is not a directory
                        String filePath = childFile.getCanonicalPath();
                        String fileName = childFile.getName();
                        //check if file name also has to be matched
                        if (fileNamePattern != null) {
                            //Ignore if the current file doesn't match the pattern
                            /*if (!Pattern.compile(Pattern.quote(fileNamePattern),
                                                 Pattern.CASE_INSENSITIVE).matcher(fileName).find()) {
                                continue;
                            }*/
                            if (!fileNamePattern.matcher(fileName).find()) {
                                continue;
                            }
                        }
                        //check whether the file is binary or text based on file extension
                        String fileExtension = CommonUtils.getFileExtension(fileName);
                        if(fileExtension != null && extensionTypeCache){
                            //check wether the type has been already determined
                            if(extensionBinary.containsKey(fileExtension)){
                                //Indicates that the file is binary
                                //so, shouldn't proceed further
                                continue;
                            }
                        }
                        
                        //Get the docId corresponding to the filePath
                        Integer docId = fileDocIdMap.get(filePath);

                        if (docId != null) { //this file was already indexed
                            //Get the docInfo
                            DocInfo docInfo = docIdInfoMap.get(docId);
                            //Get the last modification date of child file
                            long lastModifiedDate = childFile.lastModified();
                            //Get the last modification date when childFile was indexed
                            long indexLastModifiedDate = docInfo.getLastModifiedDate();
                            //If it is same as the one in current index, no need to index again
                            if (lastModifiedDate == indexLastModifiedDate) {
                                //add to the list of processed files
                                curIndexedFiles.add(docId);
                                //No change in file skipping
                                continue;
                            }
                            //else we need to remove this file from index first
                            remFileFromIndex(filePath);
                        }
                        docId = readFile(childFile);
                        //if docId is -1, then childFile is binary type, so this should be skipped
                        if(docId == -1){
                            //this extension was identified to be a binary
                            //so insert it into the cache
                            if(fileExtension != null && extensionTypeCache){
                                extensionBinary.put(fileExtension, true);
                            }
                            continue;
                        }
                        //add to the list of processed files
                        curIndexedFiles.add(docId);
                    } else if (recurse) { //recurse further
                        reBuild(childFile);
                    }
                }
            }
        }
    
        
    }
    

    /**
     *
     * @param rootDirFullPath
     * @param searchParams
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Set<Integer> fetchIndexFromStorage(String rootDirFullPath, Map<String, Object> searchParams) throws FileNotFoundException, IOException {
        //TODO: First check whether the index is present in storage
        //need not build the index always
        ReBuildIndex reBuildProc = new ReBuildIndex(searchParams);
        //Re-build the index and also return all the file names that are indexed
        return reBuildProc.reBuild(rootDirFullPath);
    }



    /**
     *Re-constructs the index and returns the Corpus information pertaining to rootDirFullPath
     * @param rootDirFullPath
     * @param searchParams - various search parameters as a map
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public CorpusType getCorpusInfo(String rootDirFullPath, Map<String, Object> searchParams) throws FileNotFoundException,
                                                                   IOException {
        //indexedFiles containts the DocIds of documenrs that have to be searched
        Set<Integer> docIdsToSearch = fetchIndexFromStorage(rootDirFullPath, searchParams);

        //get the docIdInfoSubMap corresponding to indexedFiles
        Map<Integer, DocInfo> docIdInfoSubMap = CommonUtils.fetchDocIdInfoSubMap(docIdInfoMap, docIdsToSearch);

        CorpusType corpusInfo = new CorpusType(docIdInfoSubMap, invertedIndex, termDocCountMap, idfVector);
        return corpusInfo;
    }
}
