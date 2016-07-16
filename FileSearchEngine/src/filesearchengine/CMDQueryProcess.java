package filesearchengine;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.DocInfo;

import filesearchengine.process.IndexBuilder;
import filesearchengine.process.MainQueryProcess;

import java.util.Map;

/*
 * This class can be used to used the search enfine from command line
 */
public class CMDQueryProcess {
    public CMDQueryProcess() {
        super();
    }
    
    private static void displayResult(Map<Integer, Float> docScoreMap, Map<Integer, DocInfo> docIdFileMap){
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
        CorpusType corpusInfo = obj.getCorpusInfo(args[0]);
        //obj.displayIndex();
        
        MainQueryProcess mainProc = new MainQueryProcess(corpusInfo);
        
        //Query with search terms
        Map<Integer, Float> docScoreMap = mainProc.searchQuery(args[1]);
        //Display result in order of relevance
        CMDQueryProcess.displayResult(CommonUtils.sortByValue(docScoreMap, 2/*fetch top results*/), corpusInfo.getDocIdFileMap());
    }
}
