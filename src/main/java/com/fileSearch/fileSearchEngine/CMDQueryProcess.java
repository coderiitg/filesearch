package com.fileSearch.fileSearchEngine;

import com.fileSearch.fileSearchEngine.common.CommonUtils;
import com.fileSearch.fileSearchEngine.common.CorpusType;
import com.fileSearch.fileSearchEngine.common.DocInfo;
import com.fileSearch.fileSearchEngine.process.IndexBuilder;
import com.fileSearch.fileSearchEngine.process.MainQueryProcess;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

/*
 * This class can be used to used the search enfine from command line
 */
public class CMDQueryProcess {
    public CMDQueryProcess() {
        super();
    }
    
    private static void displayResult(Map<Integer, Double> docScoreMap, Map<Integer, DocInfo> docIdFileMap){
        System.out.println("*****************Displaying the Final result*****************");
        if(docScoreMap != null && !docScoreMap.isEmpty()){
            for(Integer docId : docScoreMap.keySet()){
                System.out.println((docIdFileMap.get(docId)).getFilePath());
            }
        }
    }
    
    public static void main(String[] args) {
        if(args == null || args.length != 2)
            System.out.println("This program accepts two parameters: 1.Full Path of Directory to be searched and 2.String that has to be searched");
        
        IndexBuilder obj = new IndexBuilder();
        CorpusType corpusInfo = null;
        
        try {
            corpusInfo = obj.getCorpusInfo(args[0], new HashMap<String, Object>());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //obj.displayIndex();
        
        MainQueryProcess mainProc = new MainQueryProcess(corpusInfo);
        
        //Query with search terms
        Map<Integer, Double> docScoreMap = mainProc.triggerQuery(args[1], new HashMap<String,Object>());
        //Display result in order of relevance
        CMDQueryProcess.displayResult(CommonUtils.sortByValue(docScoreMap, 2/*fetch top results*/), corpusInfo.getDocIdInfoMap());
    }
}
