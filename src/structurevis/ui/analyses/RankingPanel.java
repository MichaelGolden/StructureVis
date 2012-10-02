/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.analyses;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import structurevis.ranking.Ranking;
import structurevis.ranking.RankingAnalyses;
import structurevis.ranking.StatUtil;
import structurevis.structures.Structure;
import structurevis.structures.StructureParser;
import structurevis.structures.metadata.MetadataFromFile;
import structurevis.structures.metadata.SequenceData1D;
import structurevis.structures.metadata.SequenceData2D;
import structurevis.ui.MainApp;

/**
 *
 * @author Michael
 */
public class RankingPanel extends javax.swing.JPanel {

    MainApp mainapp = null;
    ArrayList<SequenceData1D> sequenceData1D = null;
    ArrayList<SequenceData2D> sequenceData2D = null;
    ArrayList sequenceData = new ArrayList();
    RankingTable rankingTable = null;
    public RankingThread currentThread = null;

    /**
     * Creates new form AnalysesPanel
     */
    public RankingPanel(MainApp mainapp) {
        initComponents();
        this.mainapp = mainapp;

        rankingTable = new RankingTable(mainapp);
        jPanel1.add(rankingTable);
        //jScrollPane1.setViewportView(rankingTable);

        if (mainapp.structureCollection != null) {
            sequenceData1D = mainapp.structureCollection.sequenceData1D;
            sequenceData2D = mainapp.structureCollection.sequenceData2D;
            sequenceData.addAll(sequenceData1D);
            sequenceData.addAll(sequenceData2D);

            DefaultComboBoxModel sequenceDataModel = new DefaultComboBoxModel();
            dataSourceBox.setModel(sequenceDataModel);

            for (int i = 0; i < sequenceData.size(); i++) {
                sequenceDataModel.addElement(((MetadataFromFile) sequenceData.get(i)).name);
            }
        }
    }
    Hashtable<String, ArrayList> rowCache = new Hashtable<String, ArrayList>();

    public ArrayList getListFromCache(String key) {
        ArrayList ret = rowCache.get(key);
        if (ret == null) {
            return new ArrayList();
        }

        return ret;
    }

    class RankingThread extends Thread {

        Object data;
        boolean running = false;
        boolean hasStopped = true;

        public RankingThread(Object data) {
            this.data = data;
        }

        public void run() {
            hasStopped = false;
            running = true;
            
            saveAsCSVButton.setEnabled(false);
            statusLabel.setForeground(Color.red);
            statusLabel.setText("Ranking...");
            
            rankingTable.tableDataModel.clear();
            boolean paired = true;

            Structure fullStructure = StructureParser.parseDotBracketString(mainapp.structureCollection.dotBracketStructure);

            int PAIR_PARAMETER = 0;
            boolean unpaired = true;
            if (RankingPanel.this.pairedOnlyButton.isSelected()) {
                paired = true;
                unpaired = false;
                PAIR_PARAMETER = 1;
            } else if (RankingPanel.this.unpairedOnlyButton.isSelected()) {
                paired = false;
                unpaired = true;
                PAIR_PARAMETER = 2;
            }

            if (data instanceof SequenceData1D) {
                SequenceData1D sequenceData1D = (SequenceData1D) data;
                String cacheKey = sequenceData1D.name + "_" + PAIR_PARAMETER;
                ArrayList rows = getListFromCache(cacheKey);
                rankingTable.tableDataModel.addRows(rows);
                ArrayList<Structure> structures = mainapp.structureCollection.structures;
                for (int i = rows.size(); running && i < structures.size(); i++) {                    
                    statusLabel.setText("Ranking ("+(i+1)+" of "+structures.size()+")");
                    Structure structure = structures.get(i);
                    Ranking ranking = RankingAnalyses.rankSequenceData1D(sequenceData1D, structure, mainapp.structureCollection.dotBracketStructure.length(), fullStructure, paired, unpaired);

                    //System.out.println(ranking.zScore + "\t" + StatUtil.erf(Math.abs(ranking.zScore/2)) + "\t" + StatUtil.erfc(Math.abs(ranking.zScore))+ "\t" + StatUtil.erfcx(Math.abs(ranking.zScore))+ "\t" + StatUtil.getInvCDF(Math.abs(ranking.zScore), true)+"\t"+RankingAnalyses.NormalZ(Math.abs(ranking.zScore))/2);
                    Object[] row = {new Integer(i + 1), structure.name, new Location(structure.startPosition, structure.startPosition + structure.length), new Integer(structure.length), new Integer(ranking.xN), new Integer(ranking.yN), new Double(ranking.xMean), new Double(ranking.yMean), new Double(ranking.xMedian), new Double(ranking.yMedian), new Double(ranking.mannWhitneyU), new Double(RankingAnalyses.NormalZ(Math.abs(ranking.zScore))/2), new Double(ranking.zScore)};
                    rankingTable.tableDataModel.addRow(row);
                    rankingTable.repaint();
                }

                ArrayList<Object[]> clone = new ArrayList<Object[]>();
                for (int i = 0; i < rankingTable.tableDataModel.rows.size(); i++) {
                    clone.add(rankingTable.tableDataModel.rows.get(i));
                }
                rowCache.put(cacheKey, clone);
            } else if (data instanceof SequenceData2D) {
                SequenceData2D sequenceData2D = (SequenceData2D) data;
                String cacheKey = sequenceData2D.name + "_" + PAIR_PARAMETER;
                ArrayList rows = getListFromCache(cacheKey);
                rankingTable.tableDataModel.addRows(rows);
                ArrayList<Structure> structures = mainapp.structureCollection.structures;
                for (int i = rows.size(); running && i < structures.size(); i++) {
                    statusLabel.setText("Ranking ("+(i+1)+" of "+structures.size()+")");
                    Structure structure = structures.get(i);

                    Ranking ranking = RankingAnalyses.rankSequenceData2D(sequenceData2D, structure, mainapp.structureCollection.dotBracketStructure.length(), fullStructure, paired, unpaired);
             
                    Object[] row = {new Integer(i + 1), structure.name, new Location(structure.startPosition, structure.startPosition + structure.length), new Integer(structure.length), new Integer(ranking.xN), new Integer(ranking.yN), new Double(ranking.xMean), new Double(ranking.yMean), new Double(ranking.xMedian), new Double(ranking.yMedian), new Double(ranking.mannWhitneyU), new Double(RankingAnalyses.NormalZ(Math.abs(ranking.zScore))/2), new Double(ranking.zScore)};
                    rankingTable.tableDataModel.addRow(row);
                    rankingTable.repaint();
                }

                ArrayList<Object[]> clone = new ArrayList<Object[]>();
                for (int i = 0; i < rankingTable.tableDataModel.rows.size(); i++) {
                    clone.add(rankingTable.tableDataModel.rows.get(i));
                }
                rowCache.put(cacheKey, clone);
            }

            hasStopped = true;

            statusLabel.setForeground(Color.green);
            statusLabel.setText("Ranking complete");
            saveAsCSVButton.setEnabled(true);
        }

        public void kill() {
            running = false;
            while (!hasStopped) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RankingPanel.class.getName()).log(Level.SEVERE, null, ex);
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

    public void performRanking() {
        kill();

        if (dataSourceBox.getSelectedIndex() >= 0) {
            Object data = sequenceData.get(dataSourceBox.getSelectedIndex());
            currentThread = new RankingThread(data);
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
        pairedOnlyButton = new javax.swing.JRadioButton();
        unpairedOnlyButton = new javax.swing.JRadioButton();
        allSitesButton = new javax.swing.JRadioButton();
        statusLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        saveAsCSVButton = new javax.swing.JButton();

        dataSourceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataSourceBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Data source");

        jPanel1.setLayout(new java.awt.BorderLayout());

        buttonGroup1.add(pairedOnlyButton);
        pairedOnlyButton.setText("Paired only");
        pairedOnlyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pairedOnlyButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(unpairedOnlyButton);
        unpairedOnlyButton.setText("Unpaired only");
        unpairedOnlyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unpairedOnlyButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(allSitesButton);
        allSitesButton.setSelected(true);
        allSitesButton.setText("All nucleotides");
        allSitesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allSitesButtonActionPerformed(evt);
            }
        });

        statusLabel.setText(" ");

        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setText("Double-click on a row to open the corresponding structure in the viewer.");

        saveAsCSVButton.setText("Save as CSV");
        saveAsCSVButton.setEnabled(false);
        saveAsCSVButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsCSVButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dataSourceBox, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(allSitesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pairedOnlyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unpairedOnlyButton)
                        .addGap(18, 18, 18)
                        .addComponent(statusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(saveAsCSVButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataSourceBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(pairedOnlyButton)
                    .addComponent(unpairedOnlyButton)
                    .addComponent(allSitesButton)
                    .addComponent(statusLabel)
                    .addComponent(saveAsCSVButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addGap(22, 22, 22))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dataSourceBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataSourceBoxActionPerformed
        performRanking();
    }//GEN-LAST:event_dataSourceBoxActionPerformed

    private void allSitesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allSitesButtonActionPerformed
        performRanking();
    }//GEN-LAST:event_allSitesButtonActionPerformed

    private void pairedOnlyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pairedOnlyButtonActionPerformed
        performRanking();
    }//GEN-LAST:event_pairedOnlyButtonActionPerformed

    private void unpairedOnlyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unpairedOnlyButtonActionPerformed
        performRanking();
    }//GEN-LAST:event_unpairedOnlyButtonActionPerformed

    private void saveAsCSVButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsCSVButtonActionPerformed

        String name = ((SequenceData1D) sequenceData.get(dataSourceBox.getSelectedIndex())).name;
        if (RankingPanel.this.pairedOnlyButton.isSelected()) {
            name = name + "-paired_only";
        } else if (RankingPanel.this.unpairedOnlyButton.isSelected()) {
            name = name + "-unpaired_only";
        }

        File outFile = new File(MainApp.fileChooserSave.getCurrentDirectory().getPath() + "/" + name + "-ranking.csv");
        MainApp.fileChooserSave.setDialogTitle("Save CSV");
        MainApp.fileChooserSave.setSelectedFile(outFile);
        int returnVal = MainApp.fileChooserSave.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            saveAsCSV(MainApp.fileChooserSave.getSelectedFile());
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        MainApp.fileChooserSave.setDialogTitle("Open");

        System.out.println(name + "\t" + rankingTable.tableDataModel.rows.size());
    }//GEN-LAST:event_saveAsCSVButtonActionPerformed

    public void saveAsCSV(File outFile) {
        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(outFile));
            TableColumnModel columnModel = rankingTable.table.getTableHeader().getColumnModel();
            for (int i = 0; i < columnModel.getColumnCount() - 1; i++) {
                buffer.write(columnModel.getColumn(i).getHeaderValue().toString() + ",");
            }
            buffer.write(columnModel.getColumn(columnModel.getColumnCount() - 1).getHeaderValue().toString());
            buffer.newLine();
            //ArrayList<Object[]> rows = rankingTable.table.getModel().;

            /*
             * TableModel tableModel = rankingTable.table.getModel(); for (int i
             * = 0; i < rows.size(); i++) { Object[] row = rows.get(i); for (int
             * j = 0; j < row.length - 1 ; j++) { buffer.write(row[j].toString()
             * + ","); } buffer.write(row[row.length - 1].toString());
             * buffer.newLine(); }
             */

            TableModel tableModel = rankingTable.table.getModel();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                //Object[] row = rows.get(i);
                for (int j = 0; j < tableModel.getColumnCount() - 1; j++) {
                    buffer.write(tableModel.getValueAt(i, j).toString() + ",");
                }
                buffer.write(tableModel.getValueAt(i, tableModel.getColumnCount() - 1).toString().toString());
                buffer.newLine();
            }
            buffer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void createAndShowGUI(MainApp mainapp) {
        //Create and set up the window.
        JFrame frame = new JFrame("Structure ranking");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(mainapp.appIcon.getImage());

        //Create and set up the content pane.
        RankingPanel newContentPane = new RankingPanel(mainapp);
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
    private javax.swing.JRadioButton allSitesButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox dataSourceBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton pairedOnlyButton;
    private javax.swing.JButton saveAsCSVButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JRadioButton unpairedOnlyButton;
    // End of variables declaration//GEN-END:variables
}
