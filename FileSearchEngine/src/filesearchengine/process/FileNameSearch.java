package filesearchengine.process;

import filesearchengine.common.DocInfo;
import static filesearchengine.common.SearchEngineConstants.FILENAME_PATTERN;
import static filesearchengine.common.SearchEngineConstants.RECURSIVE_SEARCH;
import static filesearchengine.common.SearchEngineConstants.SKIP_HIDDEN_ITEMS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class FileNameSearch {
    private boolean skipHidden = false;
    private boolean recurse = true;

    private String fileNamePattern = null;

    public FileNameSearch() {
        super();
    }

    List<DocInfo> results = new ArrayList<DocInfo>();

    private void iterateFolder(File rootDir) throws FileNotFoundException, IOException {
        //Fetch all files in current folder
        File[] listFiles = rootDir.listFiles();

        if (listFiles != null) {
            for (File childFile : listFiles) {
                //skip processing if skiphidden files is set and childFile is hidden
                if (skipHidden && childFile.isHidden()) {
                    continue;
                }
                if (!childFile.isDirectory()) {
                    String fileName = childFile.getName();

                    //Ignore if the current file name doesn't match the pattern
                    if (!Pattern.compile(Pattern.quote(fileNamePattern),
                                         Pattern.CASE_INSENSITIVE).matcher(fileName).find()) {
                        continue;
                    }

                    //Create a DocInfo, with default doc id
                    DocInfo docInfo = new DocInfo(-1, childFile.getCanonicalPath());
                    //Get the last modification date of child file
                    long lastModifiedDate = childFile.lastModified();

                    //Set the lastModifiedDate in DocInfo
                    docInfo.setLastModifiedDate(lastModifiedDate);
                    //Set FileSize
                    docInfo.setFileSize(childFile.length());

                    //Add this file to the result
                    results.add(docInfo);
                } else if (recurse) { //recurse further
                    iterateFolder(childFile);
                }
            }
        }
    }

    public List<DocInfo> getFilesWithPattern(String rootDirFullPath,
                                             Map<String, Object> searchParams) throws FileNotFoundException,
                                                                                      IOException {
        //should recursive search be performed
        this.recurse = (Boolean) searchParams.get(RECURSIVE_SEARCH);
        this.skipHidden = (Boolean) searchParams.get(SKIP_HIDDEN_ITEMS);
        //If the fileName parameter is not provided, this will be NULL
        this.fileNamePattern = (String) searchParams.get(FILENAME_PATTERN);

        //Call read Files which actually iterates through folder
        File rootDir = new File(rootDirFullPath);
        iterateFolder(rootDir);
        return results;
    }
}
