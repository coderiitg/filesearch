package filesearchengine;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.DocInfo;

import filesearchengine.process.IndexBuilder;
import filesearchengine.process.MainQueryProcess;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;

public class Startup {
    public Startup() {
        super();
    }
    
    private static void displayResult(Map<Integer, Float> docScoreMap, Map<Integer, DocInfo> docIdFileMap){
        System.out.println("*****************Displaying the Final result*****************");
        if(docScoreMap != null && !docScoreMap.isEmpty()){
            for(Integer docId : docScoreMap.keySet()){
                System.out.println((docIdFileMap.get(docId)).getFilePath() + " => " + docScoreMap.get(docId));
            }
        }
    }
    
    public static void main(String[] args) {
        IndexBuilder obj = new IndexBuilder();
        CorpusType corpusInfo = null;
        try {
            corpusInfo = obj.getCorpusInfo("C:\\Users\\gunsrini.ORADEV\\Desktop\\TexFilesDir");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //obj.displayIndex();
        
        MainQueryProcess mainProc = new MainQueryProcess(corpusInfo);
        
        //Query with search terms
        Map<Integer, Float> docScoreMap = mainProc.searchQuery("rajiv india");
        //Display result in order of relevance
        Startup.displayResult(CommonUtils.sortByValue(docScoreMap, 2/*fetch top results*/), corpusInfo.getDocIdInfoMap());
    }
}
