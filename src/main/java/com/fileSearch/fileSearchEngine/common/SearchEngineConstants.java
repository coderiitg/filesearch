package com.fileSearch.fileSearchEngine.common;


public class SearchEngineConstants {
    public static final String RECURSIVE_SEARCH = "Search subfolders";
    public static final String SKIP_HIDDEN_ITEMS = "Skip hidden items";
    public static final String EXTNS_SEARCH = "Extensions";
    public static final String FILENAME_PATTERN = "File Name";
    public static final String FILENAME_SEARCH_ONLY = "FileName Search Only";
    public static final String MATCH_ALL_TERMS = "Match all terms";
    
    //The maximum number of documents that can be maintained in the corpus's index
    //When this threshold is reached, less frequently used files will be removed from the central corpus
    public static final Integer MAX_CORPUS_SIZE = 1000;
}
