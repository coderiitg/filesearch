package filesearchengineui.view;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.DocInfo;
import static filesearchengine.common.SearchEngineConstants.FILENAME_PATTERN;
import static filesearchengine.common.SearchEngineConstants.MATCH_ALL_TERMS;
import static filesearchengine.common.SearchEngineConstants.RECURSIVE_SEARCH;
import static filesearchengine.common.SearchEngineConstants.SKIP_HIDDEN_ITEMS;

import filesearchengine.process.FileNameSearch;
import filesearchengine.process.IndexBuilder;
import filesearchengine.process.MainQueryProcess;

import filesearchengineui.common.CommonConstants;

import filesearchengineui.model.DocWrapperContentSearch;
import filesearchengineui.model.DocWrapperFileSearch;
import filesearchengineui.model.DocumentWrapper;
import filesearchengineui.model.FileSizeWrapper;
import filesearchengineui.model.ResultTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class FileSearchUI {

    public static void main(String[] args) {

        new filesearchengineui.view.FileSearchUI();
    }

    public final JFrame frame = new JFrame("Swift File Search");
    
    public FileSearchUI() {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException ex) {
                }

                
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocation(100, 100);
                frame.setSize(1200, 700);
                frame.setLayout(new BorderLayout());
                frame.add(new MainSearchPane());
                frame.setVisible(true);
            }
        });
    }
    
    public class MainSearchPane extends JPanel {

        JTextField findText = new JTextField(20);
        JTextField dirPathText = new JTextField(20);
        JTextField fileNameText = new JTextField(20);
        JCheckBox recursiveCheckBox = new JCheckBox("Search subfolders");
        JCheckBox hiddenFileCheckBox = new JCheckBox("Skip hidden items");
        JCheckBox matchAllTermsCheckBox = new JCheckBox("Match all terms");
        JButton searchBtn = new JButton("Search");
        JButton browseBtn = new JButton("Browse");
        JButton resetBtn = new JButton("Reset");
        JFileChooser fileChooser = new JFileChooser();

        private JTable resultTable;
        private final JTextArea fileContent = new JTextArea(5, 40);
        private IndexBuilder indexBuilder = new IndexBuilder();
        //Label to display the status
        private JLabel statusLabel = new JLabel();
        
        /**
         * Adds the Search Pane with all the components at the NORTH of the Main Pane
         */
        private void constructSearchPane(){
            JPanel searchPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
            
            JPanel searchInputPane = new JPanel();
            searchPane.add(searchInputPane);
            searchInputPane.setBorder(BorderFactory.createTitledBorder(""));
            //searchInputPane.setBackground(Color.white);
            //GrouopLayout for SearchPane
            GroupLayout layout = new GroupLayout(searchInputPane);
            searchInputPane.setLayout(layout);

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            //set the Action Listener on browSeBtn
            browseBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    // For Directory
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    // For File
                    //fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                    fileChooser.setAcceptAllFileFilterUsed(false);

                    int rVal = fileChooser.showOpenDialog(null);
                    if (rVal == JFileChooser.APPROVE_OPTION) {
                        dirPathText.setText(fileChooser.getSelectedFile().toString() + File.separator);
                    }
                }
            });

            //Disable the edit property on dirPathText field
            dirPathText.setEditable(false);

            //check recursiveCheckBox by default
            recursiveCheckBox.setSelected(true);
            
            //check hidden item check by default
            hiddenFileCheckBox.setSelected(false);
            
            //match all terms by default
            matchAllTermsCheckBox.setSelected(true);
            
            JLabel findLabel = new JLabel("Search text:");
            JLabel fileNameLabel = new JLabel("File name:");
            JLabel dirLabel = new JLabel("Look in:");
            

            //Defining the horizontal alignment
            layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(findLabel)
                    .addComponent(fileNameLabel)
                    .addComponent(dirLabel))
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(findText, 0, 800, Short.MAX_VALUE)
                    .addComponent(fileNameText, 0, 800, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dirPathText, 0, 300, 725)
                        .addComponent(browseBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(recursiveCheckBox)
                        .addComponent(hiddenFileCheckBox)
                        .addComponent(matchAllTermsCheckBox)))
            );
            
            //Ensure that text fields stay the same size
            
            //layout.linkSize(SwingConstants.HORIZONTAL, findText, dirPathText);
            layout.linkSize(SwingConstants.HORIZONTAL, findText, fileNameText);
            
            
            //Ensure that the buttons stay the same size
            //layout.linkSize(SwingConstants.HORIZONTAL, searchBtn, browseBtn);
            
            //Defining the Vertical alignment
            layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(findLabel)
                    .addComponent(findText))
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(fileNameLabel)
                    .addComponent(fileNameText))
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(dirLabel)
                    .addGroup(layout.createParallelGroup(BASELINE)
                        .addComponent(dirPathText)
                        .addComponent(browseBtn)))
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(recursiveCheckBox)
                    .addComponent(hiddenFileCheckBox)
                    .addComponent(matchAllTermsCheckBox))
            );
            
            //Define Action Listeners on the Search button
            QueryBtnHandler queryHandler = new QueryBtnHandler();

            searchBtn.addActionListener(queryHandler);
            
            //findText.addActionListener(queryHandler);
            searchPane.add(searchBtn);
            
            //Define Action Listener on the Reset button
            resetBtn.addActionListener(new ResetBtnHandler());
            searchPane.add(resetBtn);
            add(searchPane, BorderLayout.NORTH);
        }
        
        /**
         * Adds the Result Pane with all the components at the CENTER of the Main Pane
         */
        private void constructResultPane(){
            DefaultTableModel resultTableModel = new ResultTableModel(new String[]{"Name", "Path", "Type", "Size", "Modified Date"}, 0);
            
            resultTable = new JTable(resultTableModel);
            
            TableCellRenderer dateCellRenderer = new DefaultTableCellRenderer() {

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

                public Component getTableCellRendererComponent(JTable table,
                        Object value, boolean isSelected, boolean hasFocus,
                        int row, int column) {
                    if( value instanceof Date) {
                        value = sdf.format(value);
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);
                }
            };
            
            //Setting the cell renderer on the Date's column so that date value will be formatted appropriately
            resultTable.getColumnModel().getColumn(4).setCellRenderer(dateCellRenderer);
            resultTable.setAutoCreateRowSorter(true);
            resultTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selectedRow = resultTable.getSelectedRow();//Get the selected row index
                        //Proceed only if it's greater than -1
                        if(selectedRow > -1){
                            DocumentWrapper docWrapper = (DocumentWrapper)resultTable.getValueAt(selectedRow, 1);
                            
                            //Set the file Content in the Content pane
                            if (docWrapper != null) {
                                String data = docWrapper.getData();
                                if(data == null){
                                    //Check if the docWrapper is an instance of DocWrapperFileSearch
                                    if((docWrapper instanceof DocWrapperFileSearch) && ((DocWrapperFileSearch)docWrapper).isFileBinary()){
                                        //If the file is of Binary type, then display an error message
                                        displayError("The file format cannot be read by Swift File Search");
                                    }
                                }
                                else{
                                    //Set the data
                                    fileContent.setText(data);
                                    //Highlight the query terms
                                    if (docWrapper instanceof DocWrapperContentSearch) {
                                        //Get the indices of terms
                                        Map<String, List<Integer>> termIndicesMap =
                                            ((DocWrapperContentSearch) docWrapper).getTermIndicesMap();
                                        Highlighter highlighter = fileContent.getHighlighter();
                                        HighlightPainter painter =
                                            new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
                                        
                                        //Remove all the earlier highlights
                                        highlighter.removeAllHighlights();

                                        for(String term : termIndicesMap.keySet()){
                                            int termLen = term.length();
                                            for(int termIndex : termIndicesMap.get(term)){
                                                try {
                                                    highlighter.addHighlight(termIndex, termIndex + termLen, painter);
                                                } catch (BadLocationException f) {
                                                    ;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                fileContent.setText("");
                            }
                        }
                    }
                }
            });

            JScrollPane resultPane = new JScrollPane(resultTable);
            //TitledBorder for resultPane
            TitledBorder resultPaneTitle =
                BorderFactory.createTitledBorder(UIManager.getBorder("ScrollPane.border"), "Results",
                                                 TitledBorder.CENTER, TitledBorder.TOP,
                                                 new Font("SansSerif", Font.BOLD, 14));
            resultPane.setBorder(resultPaneTitle);
            add(resultPane, BorderLayout.CENTER);
        }
        
        /**
         * Adds the Content Pane with all the components at the EAST of the Main Pane
         */
        private void constructContentPane(){
            fileContent.setEditable(false);
            JScrollPane contentPane = new JScrollPane(fileContent);
            //TitledBorder for ContentPane
            TitledBorder contentPaneTitle =
                BorderFactory.createTitledBorder(UIManager.getBorder("ScrollPane.border"), "File Content",
                                                 TitledBorder.CENTER, TitledBorder.TOP,
                                                 new Font("SansSerif", Font.BOLD, 14));
            contentPane.setBorder(contentPaneTitle);

            add(contentPane, BorderLayout.EAST);
        }
        
        private void constructStatusPane(){
            JPanel statusPane = new JPanel();
            
            //Uncomment this code block if sanp has to be taken
            statusLabel.addMouseListener(new MouseListener(){

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    takeSnap();
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    // TODO Implement this method
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    // TODO Implement this method
                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    // TODO Implement this method
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    // TODO Implement this method
                }
            });
            
            statusPane.add(statusLabel);
            add(statusPane, BorderLayout.SOUTH);
        }
        
        public MainSearchPane() {
            
            setLayout(new BorderLayout());
            
            constructSearchPane(); //Add the Search Pane at the NORTH

            constructResultPane(); //Add the Result Pane at the CENTER
            
            constructContentPane(); //Add the Result Pane at the EAST
            
            constructStatusPane();
        }

        private void displayError(String errorMsg) {
            JOptionPane.showMessageDialog(MainSearchPane.this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }

        private void takeSnap(){
            CommonUtils.takeSanpOfFrame(frame, "C:\\Users\\gunsrini.ORADEV\\Desktop\\filesearch.png");
        }

        private void clearTable(DefaultTableModel tableModel){
            //Get the current row count
            int rowCount = tableModel.getRowCount();
            for(int i=rowCount-1;i>=0;i--){
                tableModel.removeRow(i);
            }
        }
        
        public class QueryBtnHandler implements ActionListener {

            
            private boolean fileOnlySearch = false;
            //The query terms as a set
            private Set<String> queryTerms = null;
            
            @Override
            public void actionPerformed(ActionEvent e) {
               
                String searchText = findText.getText().trim();
                String dirPath = dirPathText.getText();
                String fileNamePattern = fileNameText.getText().trim();
                
                if (searchText.isEmpty() && fileNamePattern.isEmpty()) {
                    displayError("Either one of Search Text and File Name is mandatory");
                    return;
                }
                if (dirPath == null || dirPath.isEmpty()) {
                    displayError("Starting Folder cannot be empty!");
                    return;
                }


                
                //Remove earlier search results
                clearTable((DefaultTableModel)(resultTable.getModel()));
                //clear the file content area
                fileContent.setText("");
                
                //Get the corpus info pertaining to the current directory alone
                try {
                    //Should subfolders be checked
                    boolean recursiveSearch = recursiveCheckBox.isSelected();
                    //should hidden items be skipped
                    boolean skipHiddenItems = hiddenFileCheckBox.isSelected();
                    //Should all terms be matched
                    boolean matchAllTerms = matchAllTermsCheckBox.isSelected();
                    
                    //Build the search parameter map
                    Map<String, Object> searchParams = new HashMap<String, Object>();
                    searchParams.put(RECURSIVE_SEARCH, recursiveSearch);
                    searchParams.put(SKIP_HIDDEN_ITEMS, skipHiddenItems);
                    searchParams.put(MATCH_ALL_TERMS, matchAllTerms);
                    
                    //Set the file name pattern only if it's not set
                    if(fileNamePattern != null){
                        searchParams.put(FILENAME_PATTERN, fileNamePattern);
                    }
                    
                    //Index Re-building and query handling is handled only if SearchText is actually provided
                    if(!searchText.isEmpty()){
                        //re-set the global variable
                        fileOnlySearch = false;
                        //Get the dirPath sepcific Corpus Info
                        CorpusType projCorpusInfo = indexBuilder.getCorpusInfo(dirPath, searchParams);    
                        //Initialize the query process
                        MainQueryProcess mainProc = new MainQueryProcess(projCorpusInfo);

                        //Query with search text and get the score for each document
                        Map<Integer, Double> docScoreMap = mainProc.triggerQuery(searchText, searchParams);
                        this.queryTerms = mainProc.getQueryTerms();
                        if (docScoreMap == null || docScoreMap.isEmpty()) {
                            //TODO: Display one element indicating that no results could be found
                            statusLabel.setText("The search returned 0 results");
                        } else {

                            //Sort and fetch top few results
                            Map<Integer, Double> sortedDocScoreMap =
                                CommonUtils.sortByValue(docScoreMap,
                                                        CommonConstants.MAX_DISP_RESULTS /*fetch top results*/);
                            //add the search results iteratively
                            addSearchResultsToModel(sortedDocScoreMap.keySet(), projCorpusInfo.getDocIdInfoMap());
                            int numDocsReturned = docScoreMap.keySet().size();
                            if (numDocsReturned <= CommonConstants.MAX_DISP_RESULTS) {
                                statusLabel.setText("Found: " + numDocsReturned);
                            } else {
                                statusLabel.setText("Found: " + numDocsReturned + ", displaying the top " +
                                                    CommonConstants.MAX_DISP_RESULTS);
                            }
                        }
                    }
                    else{//It's a file name only search
                        //Set the global variables
                        fileOnlySearch = true;
                        //reset the query terms as this is filename only search
                        this.queryTerms = null;
                        
                        FileNameSearch fileNameSearchProc = new FileNameSearch();
                        //Get results through FileName only search
                        List<DocInfo> results = fileNameSearchProc.getFilesWithPattern(dirPath, searchParams);
                        if(results.isEmpty()){
                            //TODO: Display one element indicating that no results could be found
                            statusLabel.setText("The search returned 0 results");
                        }
                        else {
                            //add search results to model
                            addSearchResultsToModel(results);
                            int numDocsReturned = results.size();
                            if (numDocsReturned <= CommonConstants.MAX_DISP_RESULTS) {
                                statusLabel.setText("Found: " + numDocsReturned);
                            } else {
                                statusLabel.setText("Found: " + numDocsReturned + ", displaying " +
                                                    CommonConstants.MAX_DISP_RESULTS + " of them");
                            }
                        }
                    }
                    
                
                } catch (FileNotFoundException ex) {
                    displayError(ex.getMessage());
                } catch (IOException ex) {
                    displayError(ex.getMessage());
                }
                
            }
            
            /**
             *Add a search result to ResultTableModel
             * @param docInfo
             */
            private void addSearchResultToModel(DocInfo docInfo){
                //{"Name", "Location", "Type", "Size", "Modified Date"}
                //Get the full file path
                String filePath = docInfo.getFilePath();
                //Get the file name
                String baseFileName = docInfo.getBaseFileName();
                //get the file extension if any
                String fileType = docInfo.getFileType();
                //get the file size in bytes
                long fileSize = docInfo.getFileSize();
                //get the last modified date in millis
                long lastModifiedDateMillis = docInfo.getLastModifiedDate();
                DefaultTableModel resultTableModel = (DefaultTableModel) resultTable.getModel();
                
                //Creating a different instance of DocumentWrapper for different search modes
                if(!fileOnlySearch){
                    //This is a content based search
                    resultTableModel.addRow(new Object[] {
                                            baseFileName, new DocWrapperContentSearch(filePath, queryTerms), fileType,
                                            new FileSizeWrapper(fileSize), new Date(lastModifiedDateMillis)
                    });
                }
                else{
                    //If it's a file only search
                    resultTableModel.addRow(new Object[] {
                                            baseFileName, new DocWrapperFileSearch(filePath), fileType,
                                            new FileSizeWrapper(fileSize), new Date(lastModifiedDateMillis)
                    });
                }
                
            }
            
            /**
             *Adds a search results to ResultTableModel(to be used when content search is performed)
             * @param docIdSet
             * @param docIdFileMap
             */
            private void addSearchResultsToModel(Set<Integer> docIdSet, Map<Integer, DocInfo> docIdFileMap) {
                for (Integer docId : docIdSet) {
                    DocInfo docInfo = docIdFileMap.get(docId);
                    if(docInfo != null){
                        addSearchResultToModel(docInfo);
                    }
                }
            }
            
            /**
             *Adds a search results to ResultTableModel(to be used when a FileName only search is performed)
             * @param results
             */
            private void addSearchResultsToModel(List<DocInfo> results) {
                int resultSize = results.size();
                for (int i = 0; i < resultSize && i < CommonConstants.MAX_DISP_RESULTS; i++) {
                    addSearchResultToModel(results.get(i));
                }
            }
        }
    
        public class ResetBtnHandler implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //Clear the results table
                clearTable((DefaultTableModel)(resultTable.getModel()));
                //clear the file content area
                fileContent.setText("");
                //Reset the status label
                statusLabel.setText("");
            }
        }
    }
}
