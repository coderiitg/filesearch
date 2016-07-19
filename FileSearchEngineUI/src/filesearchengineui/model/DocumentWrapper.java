package filesearchengineui.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class DocumentWrapper {
    private String data;
    private String filePath; //full path of document

    /**
     *
     * @param filePath
     * @return
     */
    private static String readFile(String filePath) throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder(128);
        char[] buffer = new char[1024];
        
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(new File(filePath)));
            int charsRead = -1;
            int i = 0;
            
            
            while((charsRead = reader.read(buffer)) > -1){
                //Iterate only 4 times i.e read 4*1024 characters
                if(i > 4)
                    break;
                sb.append(buffer, 0, charsRead);
                i++;
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

    public DocumentWrapper(String filePath) {
        if (filePath == null) {
            this.filePath = "No results found for the query!!";
            this.data = "No results found for the query!!";
            return;
        }
        this.filePath = filePath;
    }

    public String getData() {
        //Read the file if data is not already set
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
