package filesearchengine.common;

import java.util.ArrayList;
import java.util.List;

public class TokenNormalizer {
    public TokenNormalizer() {
        super();
    }
    
    /**
     *given a word strip the unnecessary characters and return a normalized token(s)
     * @param line
     * @return
     */
    public static List<String> getNormalizedTokens(String line){
        if(line != null){
            //connvert the word to upper case
            line = line.toUpperCase();
            int wordLen = line.length();
            
            //Assuming each word is on average 6, assigning an inital capacity
            List<String> normalizedTokens = new ArrayList<String>(wordLen/6);
            for(String word : line.split("[\\p{Punct}\\p{Space}]")){
                if(("").equals(word))//ignore
                    continue;
                normalizedTokens.add(word);
            }
            return normalizedTokens;
        }
        return null;
    }
}
