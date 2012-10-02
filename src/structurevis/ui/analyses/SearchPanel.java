/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.analyses;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import structurevis.ranking.Ranking;
import structurevis.ranking.RankingAnalyses;
import structurevis.structures.Structure;
import structurevis.structures.StructureParser;
import structurevis.structures.metadata.MetadataFromFile;
import structurevis.structures.metadata.NucleotideComposition;
import structurevis.structures.metadata.SequenceData1D;
import structurevis.structures.metadata.SequenceData2D;
import structurevis.ui.MainApp;
import structurevis.ui.analyses.Search.SearchResult;

/**
 *
 * @author Michael
 */
public class SearchPanel extends javax.swing.JPanel {

    MainApp mainapp = null;
    ArrayList<NucleotideComposition> nucleotideDataList = null;
    ArrayList data = new ArrayList();
    SearchTable searchTable = null;
    public SearchThread currentThread = null;

    /**
     * Creates new form AnalysesPanel
     */
    public SearchPanel(MainApp mainapp) {
        initComponents();
        this.mainapp = mainapp;

        searchTable = new SearchTable(mainapp);
        jPanel1.add(searchTable, BorderLayout.CENTER);

        if (mainapp.structureCollection != null) {
            nucleotideDataList = mainapp.structureCollection.nucleotideComposition;
            data.addAll(nucleotideDataList);

            //nucleotideDataList.get(0).
            DefaultComboBoxModel dataModel = new DefaultComboBoxModel();
            dataSourceBox.setModel(dataModel);

            for (int i = 0; i < data.size(); i++) {
                dataModel.addElement(nucleotideDataList.get(i).name);
            }
        }
    }

    public void clearHighlighting() {
        mainapp.drawPanel1.setHighlightPosition(-1, -1);
    }

    class SearchThread extends Thread {

        Object data;
        boolean running = false;
        boolean hasStopped = true;

        public SearchThread(Object data) {
            this.data = data;
        }

        public void run() {
            hasStopped = false;
            running = true;
            
            statusLabel.setForeground(Color.red);
            statusLabel.setText("Searching...");

            searchTable.tableDataModel.clear();


            Search search = new Search();
            ArrayList<SearchResult> results = search.search(searchField.getText(), (NucleotideComposition) data, 0.75, true);


            ArrayList<Structure> structures = mainapp.structureCollection.structures;
            
            /*for(int i = 0 ; i < structures.size() ; i++)
            {
                System.out.println(structures.get(i));
            }*/
            
            for (int j = 0; running && j < results.size(); j++) {
                SearchResult result = results.get(j);
                ArrayList<Structure> s1 = new ArrayList<Structure>();
                for (int i = 0; i < structures.size(); i++) {
                    Structure structure = structures.get(i);
                    structure.index = i;
                    if (result.matchPosition >= structure.getStartPosition() - 1 && result.matchPosition + result.searchTerm.length() < structure.getStartPosition() - 1 + structure.getLength()) {
                        s1.add(structure);
                    }
                }

                ArrayList<Structure> s2 = s1;
                if(!SearchPanel.this.showParentCheckBox1.isSelected())
                {
                    s2 = getStructuresWithoutChildren(s1);
                }
                
                for (int i = 0; i < s2.size(); i++) {
                    Structure structure = s2.get(i);
                    Object[] row = {result.searchTerm, result.match, new Location(result.matchPosition + 1, result.matchPosition + result.searchTerm.length()), structure.index, new Location(structure.startPosition, structure.startPosition + structure.length), structure.getLength(), result.score};
                    searchTable.tableDataModel.addRow(row);
                    searchTable.repaint();
                }

            }

            statusLabel.setForeground(Color.green);
            statusLabel.setText("Search complete.");
            hasStopped = true;
        }

        public void kill() {
            running = false;
            while (!hasStopped) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SearchPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void kill() {
        if (currentThread != null) {
            currentThread.kill();
            currentThread = null;
        }
    }

    public static ArrayList<Structure> getStructuresWithoutChildren(ArrayList<Structure> structures) {
        ArrayList<Structure> childlessStructures = new ArrayList<Structure>();
        for (int i = 0; i < structures.size(); i++) {
            boolean hasChild = false;
            for (int j = 0 ; j < structures.size(); j++) {
                if(i != j)
                {
                    if (isParentStructureOf(structures.get(i), structures.get(j))) {
                        hasChild = true;
                        break;
                    }
                }
            }
            if (!hasChild) {
                childlessStructures.add(structures.get(i));
            }
        }

        return childlessStructures;
    }

    public static boolean isParentStructureOf(Structure s1, Structure s2) {
        if (s1.getStartPosition() <= s2.getStartPosition() && s1.getEndPosition() >= s2.getEndPosition()) {
            return true;
        }

        return false;
    }

    public void performSearch() {
        kill();

        if (dataSourceBox.getSelectedIndex() >= 0) {
            Object data = this.data.get(dataSourceBox.getSelectedIndex());
            currentThread = new SearchThread(data);
            currentThread.setPriority(Thread.MIN_PRIORITY);
            currentThread.start();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        dataSourceBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        clearButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        showParentCheckBox1 = new javax.swing.JCheckBox();
        statusLabel = new javax.swing.JLabel();

        dataSourceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataSourceBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Data source");

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setText("Double-click on a row to open the corresponding structure in the viewer.");

        jLabel3.setText("Search");

        clearButton.setText("Clear Highlighting");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        jButton2.setText("Search");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jToggleButton1.setText("Display DNA Ambiguity Codes");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        showParentCheckBox1.setText("Show parent structures");
        showParentCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showParentCheckBox1ActionPerformed(evt);
            }
        });

        statusLabel.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dataSourceBox, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearButton))
                    .addComponent(showParentCheckBox1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataSourceBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(showParentCheckBox1)
                        .addComponent(statusLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dataSourceBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataSourceBoxActionPerformed
        performSearch();
    }//GEN-LAST:event_dataSourceBoxActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearHighlighting();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        performSearch();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        AmbiguityCodesFrame frame = new AmbiguityCodesFrame();
        frame.setSize(350, 550);
        frame.setIconImage(mainapp.appIcon.getImage());
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - frame.getWidth()) / 2;
        final int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void showParentCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showParentCheckBox1ActionPerformed
        performSearch();
    }//GEN-LAST:event_showParentCheckBox1ActionPerformed

    public void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Structure ranking");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(mainapp.appIcon.getImage());

        //Create and set up the content pane.
        SearchPanel newContentPane = new SearchPanel(mainapp);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void createAndShowGUI(MainApp mainapp) {
        //Create and set up the window.
        JFrame frame = new JFrame("Structure ranking");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(mainapp.appIcon.getImage());

        //Create and set up the content pane.
        SearchPanel newContentPane = new SearchPanel(mainapp);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(null);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox dataSourceBox;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField searchField;
    private javax.swing.JCheckBox showParentCheckBox1;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
