package filesearchengineui.view;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.CustomFileFilter;
import filesearchengine.common.DocInfo;
import static filesearchengine.common.SearchEngineConstants.EXTNS_SEARCH;
import static filesearchengine.common.SearchEngineConstants.FILENAME_PATTERN;
import static filesearchengine.common.SearchEngineConstants.RECURSIVE_SEARCH;
import static filesearchengine.common.SearchEngineConstants.SKIP_HIDDEN_ITEMS;

import filesearchengine.process.IndexBuilder;
import filesearchengine.process.MainQueryProcess;

import filesearchengineui.model.DocumentWrapper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class FileSearchUI {

    public static void main(String[] args) {

        new filesearchengineui.view.FileSearchUI();
    }

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

                JFrame frame = new JFrame("File Search");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocation(100, 100);
                frame.setSize(1200, 700);
                frame.setLayout(new BorderLayout());
                frame.add(new MainSearchPane());
                //frame.pack();
                //frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    
    class ResultTableModel extends DefaultTableModel{
        
        public ResultTableModel(Object[] header, int rows){
            super(header, rows);    
        }
        
        @Override
        public boolean isCellEditable(int row, int column){  
            return false;  
        }
    }

    class ExtnWrapperRenderer implements ListCellRenderer
    {
        JCheckBox checkBox;
      
        public ExtnWrapperRenderer()
        {
            checkBox = new JCheckBox();

        }
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {

            ExtnWrapper extnCheckBox = (ExtnWrapper)value;
            checkBox.setText(extnCheckBox.extension);
            checkBox.setSelected(extnCheckBox.chosen);
            return checkBox;
        }
    }
      
    class ExtnWrapper
    {
        String extension;
        Boolean chosen;
      
        public ExtnWrapper(String extension, Boolean chosen)
        {
            this.extension = extension;
            this.chosen = chosen;
        }

        public String getExtension() {
            return extension;
        }

        public Boolean isChosen() {
            return chosen;
        }
    }
    
    public class MainSearchPane extends JPanel {

        JTextField findText = new JTextField(20);
        JTextField dirPathText = new JTextField(20);
        JTextField fileNameText = new JTextField(20);
        JCheckBox recursiveCheckBox = new JCheckBox("Search subfolders");
        JCheckBox hiddenFileCheckBox = new JCheckBox("Skip hidden items");
        JButton searchBtn = new JButton("Search");
        JButton browseBtn = new JButton("Browse");
        JFileChooser fileChooser = new JFileChooser();

        private DefaultTableModel resultTableModel;
        private final JTextArea fileContent = new JTextArea(5, 40);
        private IndexBuilder indexBuilder = new IndexBuilder();
        ExtnWrapper[] extnWrappers;
        
        public MainSearchPane() {
            
            //Get the set of extensions supported
            Set<String> suppExtns = CustomFileFilter.allSuppExtns;
            if(suppExtns == null || suppExtns.isEmpty()){
                throw new RuntimeException("fatal error, supported extensions list is empty!!");
            }
            
            //Create an array first
            extnWrappers = new ExtnWrapper[suppExtns.size()];
            //Construct extnWrappers array
            int i = 0;
            for(String extn : suppExtns){
                extnWrappers[i] = new ExtnWrapper(extn, true);
                i++;
            }
            
            setLayout(new BorderLayout());
            JPanel searchPane = new JPanel();
            GroupLayout layout = new GroupLayout(searchPane);
            searchPane.setLayout(layout);

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
            
            JLabel findLabel = new JLabel("Search Text:");
            JLabel fileNameLabel = new JLabel("File Name:");
            JLabel dirLabel = new JLabel("Starting Folder:");
            JLabel fileTypeLabel = new JLabel("Extensions:");
            
            //Initialize a combo box with the extension wrappers
            JComboBox extnCombo = new JComboBox(extnWrappers);
            
            
            extnCombo.setMaximumSize(new Dimension(1,25));
            //Set the renderer
            extnCombo.setRenderer(new ExtnWrapperRenderer());
            extnCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    ExtnWrapper extnWrapper = (ExtnWrapper) cb.getSelectedItem();
                    ExtnWrapperRenderer extnWrapperRenderer = (ExtnWrapperRenderer) cb.getRenderer();
                    extnWrapperRenderer.checkBox.setSelected(extnWrapper.chosen = !extnWrapper.chosen);
                }
            });
            
            layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(findLabel)
                    .addComponent(dirLabel)
                    .addComponent(fileNameLabel))
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(findText, 0, 600, Short.MAX_VALUE)
                    .addComponent(dirPathText, 0, 600, Short.MAX_VALUE)
                    .addComponent(fileNameText, 0, 600, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(searchBtn)
                    .addComponent(browseBtn))
                .addGroup(layout.createParallelGroup(LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(recursiveCheckBox)
                        .addComponent(hiddenFileCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileTypeLabel)
                        .addComponent(extnCombo)))
            );
            
            //Ensure that text fields stay the same size
            
            layout.linkSize(SwingConstants.HORIZONTAL, findText, dirPathText);
            layout.linkSize(SwingConstants.HORIZONTAL, findText, fileNameText);
            
            
            //Ensure that the buttons stay the same size
            layout.linkSize(SwingConstants.HORIZONTAL, searchBtn, browseBtn);
            
            
            layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(findLabel)
                    .addComponent(findText)
                    .addComponent(searchBtn))
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(dirLabel)
                    .addComponent(dirPathText)
                    .addComponent(browseBtn)
                    .addGroup(layout.createParallelGroup(BASELINE)
                        .addComponent(recursiveCheckBox)
                        .addComponent(hiddenFileCheckBox)))
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(fileNameLabel)
                    .addComponent(fileNameText)
                    .addGroup(layout.createParallelGroup(BASELINE)
                        .addComponent(fileTypeLabel)
                        .addComponent(extnCombo)))
            );
            
            add(searchPane, BorderLayout.NORTH);

            resultTableModel = new ResultTableModel(new String[]{"Name", "Path", "Type", "Size", "Modified Date"}, 0);

            final JTable resultTable = new JTable(resultTableModel);
            resultTable.setAutoCreateRowSorter(true);
            resultTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        DocumentWrapper docWrapper = (DocumentWrapper)resultTable.getValueAt(resultTable.getSelectedRow(), 1);
                        
                        //Set the file Content in the Content pane
                        if (docWrapper != null) {
                            fileContent.setText(docWrapper.getData());
                        } else {
                            fileContent.setText("");
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

            fileContent.setEditable(false);
            JScrollPane contentPane = new JScrollPane(fileContent);
            //TitledBorder for ContentPane
            TitledBorder contentPaneTitle =
                BorderFactory.createTitledBorder(UIManager.getBorder("ScrollPane.border"), "File Content",
                                                 TitledBorder.CENTER, TitledBorder.TOP,
                                                 new Font("SansSerif", Font.BOLD, 14));
            contentPane.setBorder(contentPaneTitle);

            add(contentPane, BorderLayout.EAST);
            QueryBtnHandler queryHandler = new QueryBtnHandler();

            searchBtn.addActionListener(queryHandler);
            findText.addActionListener(queryHandler);
        }

        private void displayError(String errorMsg) {
            JOptionPane.showMessageDialog(MainSearchPane.this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }

        private void addListToModel(Set<Integer> docIdSet, Map<Integer, DocInfo> docIdFileMap) {
            for (Integer docId : docIdSet) {
                DocInfo docInfo = docIdFileMap.get(docId);
                //{"Name", "Location", "Type", "Size", "Modified Date"}
                String filePath = docInfo.getFilePath();
                DocumentWrapper docWrapper = new DocumentWrapper(filePath);
                String baseFileName = docInfo.getBaseFileName();
                String fileType = docInfo.getFileType();
                long fileSize = docInfo.getFileSize();
                long lastModifiedDate = docInfo.getLastModifiedDate();
                resultTableModel.addRow(new Object[] {
                                        baseFileName, docWrapper, fileType, CommonUtils.getFormattedFileSize(fileSize),
                                        CommonUtils.getFormattedDate(lastModifiedDate, null)
                });                
            }
        }
        
        private Set<String> fetchSelectedExtns(){
            Set<String> selectedExtns = new HashSet<String>();
            for(ExtnWrapper extnWrapper : extnWrappers){
                //If the extension was selected
                if(extnWrapper.isChosen()){
                    selectedExtns.add(extnWrapper.getExtension());
                }
            }
            return selectedExtns;
        }
        
        public class QueryBtnHandler implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
               
                String searchText = findText.getText();
                String dirPath = dirPathText.getText();
                String fileNamePattern = fileNameText.getText();
                
                if (searchText == null || searchText.isEmpty()) {
                    displayError("Search Text cannot be empty!");
                    return;
                }
                if (dirPath == null || dirPath.isEmpty()) {
                    displayError("Starting Folder cannot be empty!");
                    return;
                }
                
                //Get the selected extension list
                Set<String> selectedExtns = fetchSelectedExtns();
                //throw an error if atleast one extension is not selected
                
                if(selectedExtns.isEmpty()){
                    displayError("You must select atleast one extension");
                    return;
                }
                
                //Remove earlier elements if any
                resultTableModel.setRowCount(0);

                //Holds the Corpus information related to the dirPath
                CorpusType projCorpusInfo = null;

                //Get the corpus info pertaining to the current directory alone
                try {
                    //Should subfolders be checked
                    boolean recursiveSearch = recursiveCheckBox.isSelected();
                    //should hidden items be skipped
                    boolean skipHiddenItems = hiddenFileCheckBox.isSelected();
                          
                    
                    //Build the search parameter map
                    Map<String, Object> searchParams = new HashMap<String, Object>();
                    searchParams.put(RECURSIVE_SEARCH, recursiveSearch);
                    searchParams.put(SKIP_HIDDEN_ITEMS, skipHiddenItems);
                    searchParams.put(EXTNS_SEARCH, selectedExtns);
                    //Set the file name pattern only if it's not set
                    if(fileNamePattern != null){
                        searchParams.put(FILENAME_PATTERN, fileNamePattern);
                    }
                    //Get the dirPath sepcific Corpus Info
                    projCorpusInfo = indexBuilder.getCorpusInfo(dirPath, searchParams);
                
                } catch (FileNotFoundException ex) {
                    displayError(ex.getMessage());
                } catch (IOException ex) {
                    displayError(ex.getMessage());
                }
                //Initialize the query process
                MainQueryProcess mainProc = new MainQueryProcess(projCorpusInfo);

                //Query with search text and get the score for each document
                Map<Integer, Float> docScoreMap = mainProc.triggerQuery(searchText);

                if (docScoreMap == null || docScoreMap.isEmpty()) {
                    //TODO: Display one element indicating that no results could be found
                    ;
                } else {

                    //Sort and fetch top few results
                    Map<Integer, Float> sortedDocScoreMap =
                        CommonUtils.sortByValue(docScoreMap, 10 /*fetch top results*/);
                    //add the search results iteratively
                    addListToModel(sortedDocScoreMap.keySet(), projCorpusInfo.getDocIdInfoMap());
                }
            }
        }
    }
}
