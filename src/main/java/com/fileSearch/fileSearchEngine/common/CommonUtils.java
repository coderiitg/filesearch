package com.fileSearch.fileSearchEngine.common;

import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.imageio.ImageIO;


public class CommonUtils {

    //private static final String specialChars[] = {"\"","'",",",".","?","(",")","#","@","$","{","}"};
    //private static final char specialChars[] = {'"','\"',',','.','?','(',')','#','!','@','{','}'});
    //TODO: add special characters
    //"',.!@$&()?{}#;:
    private static final String SPECIALCHARS = "\"',.!@$&()?{}#;:";

    public CommonUtils() {
        super();
    }

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static double round(Double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     *given a word strip the unnecessary characters and return a normalized token(s)
     * @param word
     * @return
     */
    /*
    public static Object getNormalizedTokens(String word){
        if(word != null){
            //connvert the word to upper case
            word = word.toUpperCase();

            //get the word as array of characters
            char[] wordChars = word.toCharArray();
            //the refined chars will be stored here
            char[] strippedWordChars = new char[wordChars.length];

            int strippedCharCount = 0;

            for(char wordChar : wordChars){
                //whether wordChar is in SPECIALCHAR string
                if(SPECIALCHARS.indexOf(wordChar) != -1){
                    continue;
                }
                strippedWordChars[strippedCharCount++] = wordChar;
            }
            if(strippedCharCount > 0){
                String strippedWord = new String(strippedWordChars, 0, strippedCharCount);
                return strippedWord;
            }
        }
        return null;
    }
    */

    /**
     *
     * @param values
     * @return
     */
    public static Double getMod(Collection<Double> values) {
        Double sumSquares = 0d;
        for (Double val : values) {
            sumSquares += val * val;
        }

        return Math.sqrt(sumSquares);
    }

    /**
     *Get the similarity score as cosine angle between a vector and b vector i.e (a.b)/(|a|*|b|)
     * @param a
     * @param b
     * @return
     */
    public static Double getSimilarityScore(Map<String, Double> a, Map<String, Double> b) {
        double dotProduct = 0;
        if (a != null && b != null && !a.isEmpty() && !b.isEmpty()) {
            for (String aKey : a.keySet()) {
                Double aKeyVal = a.get(aKey);
                Double bKeyVal = b.get(aKey);
                if (bKeyVal == null) {
                    //indicates that aKey is not present in b
                    continue;
                }
                dotProduct += aKeyVal * bKeyVal;
            }
            Collection<Double> aValues = a.values();
            Collection<Double> bValues = b.values();

            if (dotProduct > 0) {
                Double aMod = getMod(aValues);
                Double bMod = getMod(bValues);
                return (dotProduct / (aMod * bMod));
            }
            return 0d;
        }
        return null;
    }

    /**
     *Sort a map based on value in descending order and return the top few values based on fetchTopValues
     * @param <K>
     * @param <V>
     * @param map
     * @param fetchTopValues - the number of top few values to be returned, null if complete map has to be returned
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, Integer fetchTopValues) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        int i = 0;
        Map<K, V> result = new LinkedHashMap<K, V>();

        //Return complete map if fecthTopValues is null
        if (fetchTopValues == null) {
            for (Map.Entry<K, V> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
        } else {
            for (Map.Entry<K, V> entry : list) {
                if (++i > fetchTopValues)
                    break;
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     *Returns all key values pair submap whose key starts with prefix
     * @param <V>
     * @param sortedMap
     * @param prefix
     * @return
     */
    private static <V> Map<String, V> getPrefixedKeyValues(SortedMap<String, V> sortedMap, String prefix) {
        if (prefix != null && prefix.length() > 0) {
            int prefixLen = prefix.length();
            char nextOfLastChar = (char) (prefix.charAt(prefixLen - 1) + 1);
            String prefixEnd = prefix.substring(0, prefixLen - 1) + nextOfLastChar;
            return sortedMap.subMap(prefix, prefixEnd);
        }
        return sortedMap;
    }

    /**
     *
     * @param fileDocIdMap
     * @param dirPath
     * @param recursiveSearch
     * @return
     */
    public static Map<String, Integer> fetchFileDocIdSubMap(SortedMap<String, Integer> fileDocIdMap, String dirPath,
                                                            boolean recursiveSearch) {
        //Returns the submap which contains information about all the files and sub directories
        Map<String, Integer> fileDocIdSubMap = CommonUtils.getPrefixedKeyValues(fileDocIdMap, dirPath);

        if (!recursiveSearch) {
            if (fileDocIdSubMap != null && !fileDocIdSubMap.isEmpty()) {
                //we are only concerned with files in current directory in recursive mode
                // hence filtering further
                
                int dirLastIndex = dirPath.lastIndexOf(File.separator);
                String prefixEnd = null;
                for (String filePath : fileDocIdSubMap.keySet()) {
                    //The Index of last file separator char in filePath is not same
                    //as the one for dirPath indicates that we are in sub directory
                    
                    if (filePath.lastIndexOf(File.separator) != dirLastIndex) {
                        prefixEnd = filePath;
                        //No need to proceed further as we are in sub directory now
                        break;
                    }
                }
                //If prefix end is not null, indicates that there is atleast one file from sub directory
                //so filtering further
                if(prefixEnd != null){
                    return ((SortedMap<String, Integer>)fileDocIdSubMap).subMap(dirPath, prefixEnd);
                }
            }
        }
        return fileDocIdSubMap;
    }
    
    /**
     * Extracts docIdInfoSubMap corresponding to docIds
     * @param allDocInfoMap
     * @param docIds - set of DocIds whose key value pairs have to be returned
     * @return
     */
    public static Map<Integer, DocInfo> fetchDocIdInfoSubMap(Map<Integer, DocInfo> allDocInfoMap,
                                                        Set<Integer> docIds) {
        //A shallo copy of subMap of allDocInfoMap that contains information related to docIds alone
        Map<Integer, DocInfo> docIdInfoSubMap = new HashMap<Integer, DocInfo>();
        if (docIds != null) {
            for (int docId : docIds) {
                //Put the docId and DocInfo in the new map
                docIdInfoSubMap.put(docId, allDocInfoMap.get(docId));
            }
        }
        return docIdInfoSubMap;
    }
    
    /**
     *Returns the base file name from fileName
     * @param filePath
     * @return
     */
    public static String getBaseFileName(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            int lastSeperatorPos = filePath.lastIndexOf(File.separatorChar);
            if (lastSeperatorPos != -1) {
                return filePath.substring(lastSeperatorPos + 1);
            }
        }
        return filePath;
    }
    
    /**
     *Get the extension from file path
     * @param fileName
     * @return null if filePath is null or empty or doesn't have extension
     */
    public static String getFileExtension(String fileName){
        if(fileName != null && !fileName.isEmpty()){
            int lastPeriodPos = fileName.lastIndexOf('.');
            if(lastPeriodPos != -1){
                return fileName.substring(lastPeriodPos + 1).toLowerCase();
            }
        }
        return null;
    }
    
    
    /**
     *
     * @param millis
     * @param dateFormat
     * @return
     */
    public static String getFormattedDate(long millis, String dateFormat){
        if(dateFormat == null){
            dateFormat = "dd/MM/yyyy hh:mm:ss a";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(new Date(millis));
    }
    
    /**
     *Check whether the block is binary or text
     * @param block
     * @return
     */
    public static boolean isBlockBinary(String block) {
        if (block != null) {
            char[] buffer = block.toCharArray();
            boolean isBlockBinary = false;
            int numWeirdChars = 0;
            int charsRead = block.length();
            if (charsRead != 0) {
                for (int i = 0; i < charsRead; i++) {
                    char ch = buffer[i];
                    //Return as binary if a null character is encountered
                    if (ch == '\0') {
                        isBlockBinary = true;
                        break;
                    }
                    if (ch == '\n' || ch == '\r' || ch == '\b' || ch == '\t') {
                        continue;
                    }
                    //check if char is not in ascii
                    if (ch > 127 || ch < 32)
                        numWeirdChars++;
                }
            }
            if (!isBlockBinary) {
                //what is the ratio of weird chars to block size
                float ratio = (float) numWeirdChars / charsRead;
                if (ratio < 30) {
                    isBlockBinary = false;
                }
            }

            return isBlockBinary;
        }
        return false;
    }
    
    /**
     *Takes a snap of frame and stores it in the prescribed location
     * @param frame
     * @param location
     */
    public static void takeSanpOfFrame(Frame frame, String location){
        try {
            int w = frame.getWidth(), h = frame.getHeight();
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            frame.paint(g2);
            g2.dispose();
            ImageIO.write(image, "png", new File(location));
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
}
