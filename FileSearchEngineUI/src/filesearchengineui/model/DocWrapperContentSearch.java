package filesearchengineui.model;

import filesearchengine.common.StringSearchUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocWrapperContentSearch extends DocumentWrapper {
    Set<String> queryTerms = null;
    Map<String, List<Integer>> termIndicesMap = new HashMap<String, List<Integer>>();

    public Map<String, List<Integer>> getTermIndicesMap() {
        return termIndicesMap;
    }

    public DocWrapperContentSearch(String string, Set<String> queryTerms) {
        super(string);
        if(queryTerms == null || queryTerms.isEmpty()){
            throw new RuntimeException("The queryTerms parameter cannot be empty or null");
        }
        this.queryTerms = queryTerms;
    }

    private void addToTermIndicesMap(Map<String, List<Integer>> curTermIndicesMap, int offSet) {
        if (curTermIndicesMap != null && !curTermIndicesMap.isEmpty()) {
            for (String term : curTermIndicesMap.keySet()) {

                if (!termIndicesMap.containsKey(term)) {
                    //encountered this term for the first time
                    termIndicesMap.put(term, new ArrayList<Integer>());
                }

                //Get the current termIndices
                List<Integer> curTermIndices = curTermIndicesMap.get(term);

                //Get the global term indices i.e all over the file
                List<Integer> termIndices = termIndicesMap.get(term);

                //Add the curTermIndices to gloabal term Indices
                for (int curTermIndex : curTermIndices) {
                    termIndices.add(curTermIndex + offSet);
                }
            }
        }
    }

    /**
     *
     * @param filePath
     * @return
     */
    @Override
    protected String readFile(String filePath) throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder(128);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(filePath)));

            String line = null;

            int offSet = 0;

            while (((line = reader.readLine()) != null) && (termIndicesMap.keySet().size() != queryTerms.size())) {

                Map<String, List<Integer>> curTermIndicesMap =
                    StringSearchUtils.searchPatterns(line.toUpperCase(), queryTerms);
                //Consider this line iff curTermIndicesMap is non empty
                if (curTermIndicesMap != null && !curTermIndicesMap.isEmpty()) {

                    //Add the termIndicesMap to the global list
                    addToTermIndicesMap(curTermIndicesMap, offSet);
                    //Appending a new line character to the line
                    sb.append(line);
                    sb.append("\n");

                    //Re-calculate the offset
                    offSet += line.length() + 1;
                }
                else{//The terms were not found
                    if(sb.length() < 2048){
                        //If we are still in the first block, append the string directly
                        sb.append(line);
                        sb.append("\n");
                        //Re-calculate the offset
                        offSet += line.length() + 1;
                    }
                    else if(sb.length() < 4096){
                        sb.append("......................................................................................................................................\n");
                        sb.append("......................................................................................................................................\n");
                        //Re-calculate the offset
                        offSet += 270;
                    }
                }
            }

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ;
                }
            }
        }
        return sb.toString();
    }
}
