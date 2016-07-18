package filesearchengine.common;

/*
 * Filter only certain types of files
 */
import java.io.File;
import java.io.FileFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class CustomFileFilter implements FileFilter {
    public CustomFileFilter() {
        super();
    }
    
    private static final Set<String> acceptedTypes =
        new HashSet<String>(Arrays.asList(".txt", ".csv", ".xml", ".rtf", ".doc", ".dat"));
    
    @Override
    public boolean accept(File file) {
        //If it's a directory, return true
        if(file.isDirectory()){
            return true;
        }
        //get the canonical path
        String fileName = file.getName();
        //get the index where extension begins
        int beginIndexExtn = fileName.lastIndexOf('.');
        
        if(beginIndexExtn > 0){
            String extension = (fileName.substring(beginIndexExtn)).toLowerCase();
            //check if extension is present in acceptedTypes
            if(acceptedTypes.contains(extension)){
                return true;
            }
        }
        return false;
    }
}
