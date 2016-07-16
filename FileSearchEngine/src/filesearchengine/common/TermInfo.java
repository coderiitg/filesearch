package filesearchengine.common;


/**
 * This class reprsents the DataStructure of the key that is stored in Inverted Index
 */

public class TermInfo {
    
    private String term;
    //Indicates the number of document this term is contained in
    private Integer count = 0;
    
    public TermInfo(String term){
        //term cannot be NULL
        if(term == null)
            throw new RuntimeException("The parameter term cannot be null");
        this.term = term;
    }
    
    public void incrementCount(){
        this.count++;
    }

    public String getTerm() {
        return term;
    }

    public Integer getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }
        //else get the term contained in o
        String compTerm = (((TermInfo)o).getTerm());
        
        return term.equals(compTerm); 
    }
    
    @Override
    public int hashCode(){
        return term.hashCode();
    }
}
