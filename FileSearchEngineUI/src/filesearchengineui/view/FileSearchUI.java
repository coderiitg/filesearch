package filesearchengineui.view;

import filesearchengine.common.CommonUtils;
import filesearchengine.common.CorpusType;
import filesearchengine.common.DocInfo;

import filesearchengine.process.IndexBuilder;
import filesearchengine.process.MainQueryProcess;

import filesearchengineui.model.DocumentWrapper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean hasFocus) {
                if (value instanceof DocumentWrapper) {
                    return super.getListCellRendererComponent(
                            list, ((DocumentWrapper) value).getFilePath(), index,
                            isSelected, hasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, 
                        isSelected, hasFocus);
            }
    }
    
    
    public class TestPane extends JPanel {

        private JTextField findText;
        private JTextField dirPathText;
        private JButton searchBtn;
        private JButton browseBtn;
        private DefaultListModel model;
        private final JTextArea fileContent = new JTextArea(5, 40);
        private IndexBuilder indexBuilder = new IndexBuilder();
        
        public TestPane() {
            setLayout(new BorderLayout());
            JPanel searchPane = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 2, 2, 2);
            
            //Search Label
            JLabel findLabel = new JLabel("Find: ");
            //findLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            searchPane.add(findLabel, gbc);
            
            //Directory Label
            //Should appear below search label
            gbc.gridy++;
            JLabel dirLabel = new JLabel("Directory: ");
            //dirLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            searchPane.add(dirLabel, gbc);
            
            //reset the vertical position
            gbc.gridy--;
            
            //Search Field
            gbc.gridx++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            findText = new JTextField(20);
            searchPane.add(findText, gbc);

            //Directory Field
            //should appear below search field
            gbc.gridy++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;            
            dirPathText = new JTextField(20);
            dirPathText.setEditable(false);
            dirPathText.setText("C:\\Users\\gunsrini.ORADEV\\Desktop\\TexFilesDir");
            searchPane.add(dirPathText, gbc);
            
            //reset the vertical position
            gbc.gridy--;
            
            //Search Button
            gbc.gridx++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            searchBtn = new JButton("Search");
            searchPane.add(searchBtn, gbc);

            //Browse Button
            //should appear below Search Button
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            browseBtn = new JButton("Browse");
            browseBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();

                    // For Directory
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    // For File
                    //fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                    fileChooser.setAcceptAllFileFilterUsed(false);

                    int rVal = fileChooser.showOpenDialog(null);
                    if (rVal == JFileChooser.APPROVE_OPTION) {
                        dirPathText.setText(fileChooser.getSelectedFile().toString());
                    }
                }
            });
            searchPane.add(browseBtn, gbc);
            
            add(searchPane, BorderLayout.NORTH);

            model = new DefaultListModel<>();
            
            final JList list = new JList(model);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new DocumentWrapperRenderer());

            list.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            if (!e.getValueIsAdjusting()) {
                                DocumentWrapper docWrapper = (DocumentWrapper) list
                                        .getSelectedValue();
                                if (docWrapper != null) {
                                    fileContent.setText(docWrapper.getData());
                                } else {
                                    fileContent.setText("");
                                }
                            }
                        }
                    });
            
            add(new JScrollPane(list), BorderLayout.WEST);
            add(new JScrollPane(fileContent), BorderLayout.CENTER);
            QueryBtnHandler queryHandler = new QueryBtnHandler();

            searchBtn.addActionListener(queryHandler);
            findText.addActionListener(queryHandler);
        }

        private void displayError(String errorMsg){
            JOptionPane.showMessageDialog(TestPane.this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        private void addListToModel(Set<Integer> docIdSet, Map<Integer, DocInfo> docIdFileMap ){
            for(Integer docId : docIdSet){
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
                CorpusType corpusInfo = null;
                
                //Get the corpus info
                try {
                    corpusInfo = indexBuilder.getCorpusInfo(dirPath);
                } catch (FileNotFoundException ex) {
                    displayError(ex.getMessage());
                } catch (IOException ex) {
                    displayError(ex.getMessage());
                }
                //Initialize the query process
                MainQueryProcess mainProc = new MainQueryProcess(corpusInfo);
                
                //Query with search text and get the score for each document
                Map<Integer, Float> docScoreMap = mainProc.searchQuery(searchText);
                
                if(docScoreMap == null || docScoreMap.isEmpty()){
                    //Display one element indicating that no results could be found
                    model.addElement(new DocumentWrapper(null));
                }
                else{
                    
                    //Sort and fetch top few results
                    Map<Integer, Float> sortedDocScoreMap = CommonUtils.sortByValue(docScoreMap, 10/*fetch top results*/);
                    //add the search results iteratively
                    addListToModel(sortedDocScoreMap.keySet(), corpusInfo.getDocIdInfoMap());
                }
            }
        }
    }
}