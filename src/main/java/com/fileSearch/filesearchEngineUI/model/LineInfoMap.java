package com.fileSearch.filesearchEngineUI.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LineInfoMap {
    
    private int lineNo;
    private String lineText;
    private Map<String, List<Integer>> termIndicesMap = new HashMap<String, List<Integer>>();
    
    public LineInfoMap(int lineNo, String lineText) {
        this.lineNo = lineNo;
        this.lineText = lineText;
    }
    
    public void addTermIndex(String term, int index){
        if(termIndicesMap.containsKey(term)){//the term is already present
            termIndicesMap.get(term).add(index);
        }
        else{
            List<Integer> termIndices = new ArrayList<Integer>();
            termIndices.add(index);
        }
    }


    public int getLineNo() {
        return lineNo;
    }

    public void setLineText(String lineText) {
        this.lineText = lineText;
    }

    public String getLineText() {
        return lineText;
    }

    public Map<String, List<Integer>> getTermIndicesMap() {
        return termIndicesMap;
    }

    public void searchTerms(Set<String> terms){
        
    }
}
