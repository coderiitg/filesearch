package filesearchengineui.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class DocumentWrapper {
    protected String data;
    protected String filePath; //full path of document

    public DocumentWrapper(String filePath) {
        if (filePath == null) {
            throw new RuntimeException("file path cannot be null");
        }
        this.filePath = filePath;
    }
    

    @Override
    public String toString(){
        return filePath;
    }
    
    /**
     *
     * @param filePath
     * @return
     */
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

    public String getData() {
        //Read the file if data is not already set and the file is not binary
        if(data == null){
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

    public String getFilePath() {
        return filePath;
    }
}
