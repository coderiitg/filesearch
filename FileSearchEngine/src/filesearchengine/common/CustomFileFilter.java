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
    
    //All the extensions supported by the file search engine
    public static final Set<String> allSuppExtns =
        new HashSet<String>(Arrays.asList("txt", "csv", "xml", "rtf", "doc", "dat"));
    
    //The set of extensions selected by the user when submitting search
    private Set<String> selectedExtns = null;
    
    public CustomFileFilter(Set<String> selectedExtns) {
        //set the selectedExtns field
        this.selectedExtns = selectedExtns;
    }
    
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
            String extension = (fileName.substring(beginIndexExtn + 1)).toLowerCase();
            //check if extension is present in selectedExtns by the user
            if(selectedExtns.contains(extension)){
                return true;
            }
        }
        return false;
    }
}
