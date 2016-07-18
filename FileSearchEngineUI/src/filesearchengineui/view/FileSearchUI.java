package filesearchengineui.view;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.DocInfo;
import static filesearchengine.common.SearchEngineConstants.RECURSIVESEARCH;
import static filesearchengine.common.SearchEngineConstants.SKIPHIDDENITEMS;

import filesearchengine.process.IndexBuilder;
import filesearchengine.process.MainQueryProcess;

import filesearchengineui.model.DocumentWrapper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FileSearchUI {

    public static void main(String[] args) {
        new filesearchengineui.view.FileSearchUI();
    }

    public FileSearchUI() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException ex) {
                }

                JFrame frame = new JFrame("File Search");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocation(100, 100);
                frame.setSize(1200, 700);
                frame.setLayout(new BorderLayout());
                frame.add(new TestPane());
                //frame.pack();
                //frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class DocumentWrapperRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean hasFocus) {
            if (value instanceof DocumentWrapper) {
                return super.getListCellRendererComponent(list, ((DocumentWrapper) value).getFilePath(), index,
                                                          isSelected, hasFocus);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        }
    }


    public class TestPane extends JPanel {

        JLabel findLabel = new JLabel("Search Text");
        JLabel dirLabel = new JLabel("Starting Folder");
        JTextField findText = new JTextField(20);
        JTextField dirPathText = new JTextField(20);
        JCheckBox recursiveCheckBox = new JCheckBox("Search subfolders");
        JCheckBox hiddenFileCheckBox = new JCheckBox("Skip hidden items");
        JButton searchBtn = new JButton("Search");
        JButton browseBtn = new JButton("Browse");
        JFileChooser fileChooser = new JFileChooser();

        private DefaultListModel model;
        private final JTextArea fileContent = new JTextArea(5, 40);
        private IndexBuilder indexBuilder = new IndexBuilder();

        public TestPane() {
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
            
            layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(findLabel)
                    .addComponent(dirLabel))
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(findText)
                    .addComponent(dirPathText)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(recursiveCheckBox)
                        .addComponent(hiddenFileCheckBox)))
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(searchBtn)
                    .addComponent(browseBtn))
            );
            
            layout.linkSize(SwingConstants.HORIZONTAL, searchBtn, browseBtn);

            layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(findLabel)
                    .addComponent(findText)
                    .addComponent(searchBtn))
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(dirLabel)
                    .addComponent(dirPathText)
                    .addComponent(browseBtn))
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(recursiveCheckBox)
                    .addComponent(hiddenFileCheckBox))
            );
            
            add(searchPane, BorderLayout.NORTH);

            model = new DefaultListModel<>();

            final JList list = new JList(model);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new DocumentWrapperRenderer());

            list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        DocumentWrapper docWrapper = (DocumentWrapper) list.getSelectedValue();
                        if (docWrapper != null) {
                            fileContent.setText(docWrapper.getData());
                        } else {
                            fileContent.setText("");
                        }
                    }
                }
            });

            JScrollPane resultPane = new JScrollPane(list);
            //TitledBorder for resultPane
            TitledBorder resultPaneTitle =
                BorderFactory.createTitledBorder(UIManager.getBorder("ScrollPane.border"), "Results",
                                                 TitledBorder.CENTER, TitledBorder.TOP,
                                                 new Font("SansSerif", Font.BOLD, 14));
            resultPane.setBorder(resultPaneTitle);

            add(resultPane, BorderLayout.WEST);


            JScrollPane contentPane = new JScrollPane(fileContent);
            //TitledBorder for ContentPane
            TitledBorder contentPaneTitle =
                BorderFactory.createTitledBorder(UIManager.getBorder("ScrollPane.border"), "File Content",
                                                 TitledBorder.CENTER, TitledBorder.TOP,
                                                 new Font("SansSerif", Font.BOLD, 14));
            contentPane.setBorder(contentPaneTitle);

            add(contentPane, BorderLayout.CENTER);
            QueryBtnHandler queryHandler = new QueryBtnHandler();

            searchBtn.addActionListener(queryHandler);
            findText.addActionListener(queryHandler);
        }

        private void displayError(String errorMsg) {
            JOptionPane.showMessageDialog(TestPane.this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }

        private void addListToModel(Set<Integer> docIdSet, Map<Integer, DocInfo> docIdFileMap) {
            for (Integer docId : docIdSet) {
                String filePath = (docIdFileMap.get(docId)).getFilePath();
                model.addElement(new DocumentWrapper(filePath));
            }
        }

        public class QueryBtnHandler implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {

                String searchText = findText.getText();
                String dirPath = dirPathText.getText();

                if (searchText == null || searchText.isEmpty()) {
                    displayError("Find field cannot be empty!");
                    return;
                }
                if (dirPath == null || dirPath.isEmpty()) {
                    displayError("Directory field cannot be empty!");
                    return;
                }
                //Remove earlier elements if any
                model.removeAllElements();

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
                    searchParams.put(RECURSIVESEARCH, recursiveSearch);
                    searchParams.put(SKIPHIDDENITEMS, skipHiddenItems);
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
                    //Display one element indicating that no results could be found
                    model.addElement(new DocumentWrapper(null));
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
