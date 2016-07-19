package filesearchengine.process;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.CustomFileFilter;
import filesearchengine.common.DocInfo;
import static filesearchengine.common.SearchEngineConstants.EXTNSSEARCH;
import static filesearchengine.common.SearchEngineConstants.RECURSIVESEARCH;
import static filesearchengine.common.SearchEngineConstants.SKIPHIDDENITEMS;
import filesearchengine.common.TokenNormalizer;

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
import java.util.TreeMap;


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
    Map<String, Float> idfVector = new HashMap<String, Float>();

    public Map<String, Float> getIdfVector() {
        return idfVector;
    }

    /**
     *Insert a term into index
     * @param docId - the Document Id of doc which contains the term
     * @param term
     */
    private void insertTerm(int docId, String term) {


        if (!invertedIndex.containsKey(term)) {
            Map<Integer, Integer> docIdFreqMap = new HashMap<Integer, Integer>();
            //Insert doc info
            docIdFreqMap.put(docId, 1);
            //Insert the map into invertedIndex
            invertedIndex.put(term, docIdFreqMap);
            //Set the Document Count against the term
            termDocCountMap.put(term, 1);
        } else {
            //Iterate through docIdFreqMap
            Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);
            Integer curDocIdFreq = docIdFreqMap.get(docId);
            if (curDocIdFreq != null) {
                docIdFreqMap.put(docId, ++curDocIdFreq);
            } else {
                //Insert doc info
                docIdFreqMap.put(docId, 1);
                //Set the Document Count against the term
                termDocCountMap.put(term, 1);
            }
        }
    }

    /**
     *
     * @param file
     */
    private int readFile(File file) throws FileNotFoundException, IOException {
        BufferedReader is = null;

        try {
            is = new BufferedReader(new FileReader(file));
            String line = null;
            String filePath = file.getCanonicalPath();

            //Check whether file is already indexed
            if (!fileDocIdMap.containsKey(filePath)) {
                //If not create a new index
                fileDocIdMap.put(filePath, ++nextDocIndex);
                //Create a new Doc Information object and put it in a map
                docIdInfoMap.put(nextDocIndex, new DocInfo(nextDocIndex, filePath));
            }

            Integer docId = fileDocIdMap.get(filePath);

            while ((line = is.readLine()) != null) {
                //Get the normalized tokens from line i.e string values that can be considered as words
                List<String> normalizedTokens = TokenNormalizer.getNormalizedTokens(line);
                if (normalizedTokens != null) {
                    for (String token : normalizedTokens) {
                        insertTerm(docId, token);
                    }
                }
            }

            //Set the last modified date in document info

            DocInfo docInfo = docIdInfoMap.get(docId);
            //Get the last modified date of file
            Long lastModifiedDate = file.lastModified();
            docInfo.setLastModifiedDate(lastModifiedDate);
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
     *Removes any deleted files in the system from the corpus
     * @param dirPath
     * @param retainFiles
     * @param recursiveSearch
     */
    /* TODO:// change implementation
    private void cleanupDelFilesInDir(String dirPath, Set<String> retainFiles,
                                     boolean recursiveSearch) {
        //Get the submap of the all the files indexed under dirPath
        final Map<String, Integer> fileDocIdSubMap =
            CommonUtils.fetchFileDocIdSubMap((SortedMap<String, Integer>) fileDocIdMap, dirPath, recursiveSearch);

        if (fileDocIdSubMap != null && !fileDocIdSubMap.isEmpty()) {
            //Get the indexed files list in the dir
            Set<String> indexedFilesOfDir = new TreeSet<String>(fileDocIdSubMap.keySet());
            //Remove the retainFiles list from it as it is sure that they are in storage
            indexedFilesOfDir.removeAll(retainFiles);

            if (indexedFilesOfDir != null && !indexedFilesOfDir.isEmpty()) {
                //deleted files are present in indexedFilesOfDir
                for (String indexedFilePath : indexedFilesOfDir) {
                    remFileFromIndex(indexedFilePath);
                }
            }
        }
    }
    */
    
    private void constructIDFVector() {
        if (termDocCountMap != null && !termDocCountMap.isEmpty()) {
            int totalDocs = docIdInfoMap.keySet().size();

            for (String term : termDocCountMap.keySet()) {
                float termWeight = CommonUtils.round(Math.log(totalDocs / termDocCountMap.get(term)), 2);
                idfVector.put(term, termWeight);
            }
        }
    }


    /**
     *
     * @param rootDirFullPath
     * @param recursiveSearch
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Set<Integer> reBuildIndex(String rootDirFullPath, Map<String, Object> searchParams) throws FileNotFoundException, IOException {
        File rootDir = new File(rootDirFullPath);
        //Stores the list of files to be searched
        Set<Integer> curIndexedFiles = reBuildIndex(rootDir, searchParams);
        
        /*TODO:// decide when to cleanup deleted files
        //Cleanup deleted files if any
        cleanupDelFilesInDir(rootDirFullPath, curIndexedFiles, recursiveSearch);
        */
        
        //Re-construct IDF vector
        constructIDFVector();
        return curIndexedFiles;
    }

    /**
     *
     * @param rootDir
     * @param searchParams search parameters
     * @return set of docids of files that are indexed
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Set<Integer> reBuildIndex(File rootDir, Map<String, Object> searchParams) throws FileNotFoundException, IOException {
        //should recursive search be performed
        boolean recurse = (Boolean)searchParams.get(RECURSIVESEARCH);
        boolean skipHidden = (Boolean)searchParams.get(SKIPHIDDENITEMS);
        Set<String> selectedExtns = (Set<String>)searchParams.get(EXTNSSEARCH);
        //Fetch only the files with selected Extns
        File[] listFiles = rootDir.listFiles(new CustomFileFilter(selectedExtns));
        
        Set<Integer> curIndexedFiles = new HashSet<Integer>();
        if (listFiles != null) {
            for (File childFile : listFiles) {
                //skip processing if skiphidden files is set and childFile is hidden
                if(skipHidden && childFile.isHidden()){
                    continue;
                }
                if (!childFile.isDirectory()) {
                    //This is not a directory
                    String filePath = childFile.getCanonicalPath();

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
                    //add to the list of processed files
                    curIndexedFiles.add(docId);
                } else if (recurse) { //recurse further
                    Set<Integer> childIndexedFiles = reBuildIndex(childFile, searchParams);
                    //Add the list of files indexed in child
                    curIndexedFiles.addAll(childIndexedFiles);
                }
            }
        }
        return curIndexedFiles;
    }

    public void displayIndex() {
        System.out.println("***********BEGIN Inverted Index CONTENT***********");
        if (invertedIndex != null && !invertedIndex.isEmpty()) {
            for (String term : invertedIndex.keySet()) {
                System.out.print(term + " => ");
                //Iterate through docIdFreqMap
                Map<Integer, Integer> docIdFreqMap = invertedIndex.get(term);
                for (Integer docId : docIdFreqMap.keySet()) {
                    System.out.print("{" + docId + ":" + docIdFreqMap.get(docId) + "}, ");
                }
                System.out.println();
            }
        }
        System.out.println("***********END Inverted Index CONTENT***********");
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
        return reBuildIndex(rootDirFullPath, searchParams);
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
        //indexedFiles containts the DocIds if documenrs that have to be searched
        Set<Integer> docIdsToSearch = fetchIndexFromStorage(rootDirFullPath, searchParams);

        //get the docIdInfoSubMap corresponding to indexedFiles
        Map<Integer, DocInfo> docIdInfoSubMap = CommonUtils.fetchDocIdInfoSubMap(docIdInfoMap, docIdsToSearch);

        CorpusType corpusInfo = new CorpusType(docIdInfoSubMap, invertedIndex, termDocCountMap, idfVector);
        return corpusInfo;
    }
}
