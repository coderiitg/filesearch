package com.fileSearch.fileSearchEngine.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class StringSearchUtils {
    public StringSearchUtils() {
        super();
    }

    private static final int P = 7;
    private static final int H = 97;
    
    /**
     *Returns the remainder when a is divided by b, works even when a is negative
     * @param a
     * @param b
     * @return
     */
    private static int int_mod(int a, int b) {
        return (a % b + b) % b;
    }

    /**
     *Checks whether pattern is present in text from the index: textOffset
     * @param text
     * @param pattern
     * @param textOffset
     * @return
     */
    private static boolean strMatches(char[] text, char[] pattern, int textOffset) {
        int patternLen = pattern.length;
        int textLen = text.length;
        if (textOffset <= textLen - patternLen) {
            //Strings need to be compared iff text is long enough to be compared with pattern
            boolean strMatch = true;
            int j = textOffset;
            int k = 0;

            for (; k < patternLen; j++, k++) {
                if (text[j] != pattern[k]) {
                    strMatch = false;
                    break;
                }
            }
            return strMatch;
        }
        return false;
    }

    
    /**
     *Returns the list of indices in text where pattern is present
     * @param text
     * @param pattern
     * @param p
     * @param h
     * @return
     */
    public static List<Integer> searchPattern(String text, String pattern) {
        return searchPattern(text.toCharArray(), pattern.toCharArray(), P, H);
    }

    /**
     *Returns the list of indices in text where pattern is present
     * @param text
     * @param pattern
     * @param p
     * @param h
     * @return
     */
    private static List<Integer> searchPattern(char[] text, char[] pattern, int p, int h) {

        int textLen = text.length;
        int patternLen = pattern.length;
        List<Integer> termIndices = new ArrayList<Integer>();

        if (patternLen <= textLen) {
            //search only if text is bigger than pattern

            //Calculating the pattern's hash
            int patternHash = 0;

            for (int i = 0; i < patternLen; i++) {
                patternHash = int_mod(patternHash * p + pattern[i], h);
            }

            //Calculating the hash of the first window
            int windowHash = 0;

            for (int i = 0; i < patternLen; i++) {
                windowHash = int_mod(windowHash * p + text[i], h);
            }

            //Check if the first sub-string is same as pattern
            if (patternHash == windowHash) {
                if (strMatches(text, pattern, 0)) {
                    //Match found at: 0
                    termIndices.add(0);
                }
            }
            
            //Holds the value p^(patternLen-1) % h
            int highCharWeight = int_mod((int) Math.pow(p, patternLen - 1), h);

            for (int i = patternLen; i < textLen; i++) {
                //Recalculate the Window hash after sliding the window to one character right
                windowHash = int_mod(windowHash - int_mod(highCharWeight * text[i - patternLen], h), h);
                windowHash = int_mod(windowHash * p, h);
                windowHash = int_mod(windowHash + text[i], h);

                if (patternHash == windowHash) {
                    //If there is match in hashes, compare the strings

                    int textOffset = i - patternLen + 1; //the start of window
                    //Do the sub-strings match
                    if (strMatches(text, pattern, textOffset)) {
                        //Match found at: textOffset;
                        termIndices.add(textOffset);
                    }
                }
            }

        }

        return termIndices;
    }

    /**
     *Searches for patterns in the text
     * @param text
     * @param patterns
     * @return a map whose key is one of the pattern and value is the list of indices where pattern is found in text
     */
    public static Map<String, List<Integer>> searchPatterns(String text, Set<String> patterns) {
        //Trim extra spaces
        text.trim();
        
        //Proceed only if it's non-empty
        if(text.isEmpty()){
            return null;
        }
        
        char[] textArr = text.toCharArray();

        Set<char[]> patternSet = new HashSet<char[]>(patterns.size());

        for (String pattern : patterns) {
            patternSet.add(pattern.toCharArray());
        }

        return searchPatterns(textArr, patternSet, P, H);
    }
    
    /**
     *
     * @param text
     * @param patterns
     * @param p
     * @param h
     * @return
     */
    private static Map<String, List<Integer>> searchPatterns(char[] text, Set<char[]> patterns, int p, int h) {
        Map<String, List<Integer>> termIndicesMap = new HashMap<String, List<Integer>>();
        int textLen = text.length;

        //Holds the minimum length among the patterns
        int minPatternLen = Integer.MAX_VALUE;

        //Calculate the least length among all Search Patterns
        for (char[] pattern : patterns) {
            if (minPatternLen > pattern.length) {
                minPatternLen = pattern.length;
            }
        }

        if (minPatternLen <= textLen) {
            //search only if text is bigger than pattern
            //Set to store all pattern hashes
            Set<Integer> patternHashes = new HashSet<Integer>();
    
            //Cacluate the hashes of all the patterns upto minPattern characters
            for (char[] pattern : patterns) {
                int patternHash = 0;
                for (int i = 0; i < minPatternLen; i++) {
                    patternHash = int_mod(patternHash * p + pattern[i], h);
                }
                //Add it to the patternHashes
                patternHashes.add(patternHash);
            }
    
            //Calculating the hash of the first window
            int windowHash = 0;
    
            for (int i = 0; i < minPatternLen; i++) {
                windowHash = int_mod(windowHash * p + text[i], h);
            }
    
            //Check if the first sub-string matches any of the patterns
            if (patternHashes.contains(windowHash)) {
                //check if it matches any of the pattern
                for (char[] pattern : patterns) {
                    if (strMatches(text, pattern, 0)) {
                        String term = new String(pattern);
                        List<Integer> termIndices = new ArrayList<Integer>();
                        termIndices.add(0);
                        termIndicesMap.put(term, termIndices);
    
                        //no need to proceed further as a match has been found
                        break;
                    }
                }
            }
    
            //Holds the value p^(patternLen-1) % h
            int highCharWeight = int_mod((int) Math.pow(p, minPatternLen - 1), h);
    
            for (int i = minPatternLen; i < textLen; i++) {
                windowHash = int_mod(windowHash - int_mod(highCharWeight * text[i - minPatternLen], h), h);
                windowHash = int_mod(windowHash * p, h);
                windowHash = int_mod(windowHash + text[i], h);
    
                if (patternHashes.contains(windowHash)) {
                    for (char[] pattern : patterns) {
                        int textOffset = i - minPatternLen + 1;//the start of window
                        //Is the pattern present from the textOffset
                        boolean strMatch = strMatches(text, pattern, textOffset);
    
                        if (strMatch) {
                            //Match found at: textOffset
                            //Convert patternArray to String
                            String term = new String(pattern);
                            if (termIndicesMap.containsKey(term)) {
                                //pattern was already found to be present in text, just apending the index
                                termIndicesMap.get(term).add(textOffset);
                            } else {
                                //Adding the index for the first time
                                List<Integer> termIndices = new ArrayList<Integer>();
                                termIndices.add(textOffset);
                                termIndicesMap.put(term, termIndices);
                            }
                        }
                    }
                }
            }
        }
        return termIndicesMap;
    }

    /**
     *Performs a naive search of pattern in text
     * @param text
     * @param pattern
     * @return
     */
    public static List<Integer> naiveSearch(String text, String pattern) {
        
        List<Integer> termIndices = new ArrayList<Integer>();

        int i = text.indexOf(pattern);

        while (i != -1) {
            termIndices.add(i);
            //"Match found at i;
            i = text.indexOf(pattern, i + 1);
        }
        return termIndices;
    }

    public static void main(String[] args) {

        Set<String> patterns = new HashSet<String>();
        patterns.add("sachin");
        patterns.add("tendulkar");

        System.out.println(StringSearchUtils.searchPatterns("this is sachin tendulkar india's finest sachin", patterns));
        System.out.println(StringSearchUtils.searchPattern("this is sachin tendulkar india's finest sachin", "sachin"));
    }
}

