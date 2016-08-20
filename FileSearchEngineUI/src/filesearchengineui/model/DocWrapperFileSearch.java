package filesearchengineui.model;

import filesearchengine.common.CommonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DocWrapperFileSearch extends DocumentWrapper {
    public DocWrapperFileSearch(String string) {
        super(string);
    }
    
    //Is the file binary
    private boolean fileBinary = false;
    
    public boolean isFileBinary() {
        return fileBinary;
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
        try{
            reader = new BufferedReader(new FileReader(new File(filePath)));
            
            String line = null;
            
            while(((line = reader.readLine()) != null) && (sb.length()<4096)){
                sb.append(line);
                //Appending a new line character
                sb.append("\n");
            }
            
            int blockEnd = Math.min(sb.length(), 4096);
            //Check whether the block is binary
            fileBinary = CommonUtils.isBlockBinary(sb.substring(0, blockEnd));
            
            //If the file is binary then, data cannot be displayed on UI
            if(fileBinary){
                //file cannot be read
                return null;
            }
            
            //If there is still some content to be read, append ...........
            if(sb.length() >= 4096){
                sb.append("\n.....................................................................");
                sb.append("\n.....................................................................");
            }
        }
        finally{
            if(reader != null){
                try{
                    reader.close();
                }
                catch(IOException ex){
                    ;
                }
            }
        }
        return sb.toString();
    }
    
    @Override
    public String getData() {
        //Read the file if data is not already set and the file is not binary
        if(data == null && !isFileBinary()){
            try {
                data = readFile(filePath);
            } catch (FileNotFoundException e) {
                data = e.getMessage();
            } catch (IOException e) {
                data = e.getMessage();
            }
        }
        return data;
    }
}
