package filesearchengineui.model;


public class FileSizeWrapper implements Comparable<FileSizeWrapper>{
    
    private static final int KB = 1024; //1 Kilo byte
    private static final int MB = 1024*KB; //1 Mega byte
    private static final int GB = 1024*MB; //1 Giga byte
    
    public FileSizeWrapper(long fileSize) {
        this.fileSize = fileSize;
    }
    
    Long fileSize;
    
    /**
     *Returns the file size in GB/MB/KB
     * @param fileSize
     * @return
     */
    @Override
    public String toString(){
         if(fileSize /GB != 0){
             return String.format("%.2f GB", (float)fileSize/GB);
         }
         if(fileSize/MB != 0){
             return String.format("%.2f MB", (float)fileSize/MB);
         }
         return String.format("%.2f KB", (float)fileSize/KB);
    }

    public Long getFileSize() {
        return fileSize;
    }


    @Override
    public int compareTo(FileSizeWrapper fileSizeWrapper) {
        return fileSize.compareTo(fileSizeWrapper.getFileSize());
    }
    
    @Override
    public int hashCode(){
        return fileSize.hashCode();
    }
    
    @Override 
    public boolean equals(Object o){
        if(o == null || !(o instanceof FileSizeWrapper))
            return false;
        return fileSize.equals(((FileSizeWrapper)o).getFileSize());
    }
}
