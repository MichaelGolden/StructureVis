/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainApp.java
 *
 * Created on 15 Aug 2011, 7:57:34 PM
 */
package structurevis.ui;

import structurevis.ui.analyses.RankingPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import structurevis.data.IO;
import structurevis.data.Mapping;
import structurevis.structures.Structure;
import structurevis.structures.StructureCollection;
import structurevis.structures.io.StructureCollectionStAXReader;
import structurevis.structures.metadata.*;
import structurevis.ui.analyses.SearchPanel;
import structurevis.ui.datacreation.WizardMain;
import structurevis.ui.layerpanel.GenomeLayer;
import structurevis.ui.layerpanel.GraphLayer;
import structurevis.ui.layerpanel.LayerPanel;

/**
 *
 * @author Michael Golden
 */
public class MainApp extends javax.swing.JFrame implements AdjustmentListener, ChangeListener, ListSelectionListener, WindowListener {

    public static File persistenceFile = new File(System.getProperty("user.dir") + File.separatorChar + "app.persistence");
    public static File defaultWorkspace = new File(System.getProperty("user.dir") + File.separatorChar + "workspace" + File.separatorChar);
    public ImageIcon appIcon = new ImageIcon(getClass().getResource("/structurevis/resources/sv_icon.png"));
    public static JFileChooser fileChooserOpen = new JFileChooser();
    public static JFileChooser projectChooserOpen = new JFileChooser();
    public static JFileChooser fileChooserSave = new JFileChooser();
    public StructureDrawPanel drawPanel1;
    FullGenomeDrawPanel genomePanel1;
    JList directoryStructureList = new JList();
    JList parentStructureList = new JList();
    JList childStructureList = new JList();
    ArrayList<StructureListObject> directoryStructureFiles = new ArrayList<StructureListObject>();
    File[] files = {};
    LayerPanel layerPanel1;
    GenomeLayer genomeLayer;
    GraphLayer graphLayer1D;
    //GraphLayer graphLayer2D;
    public File collectionFolder = null;
    public StructureCollection structureCollection = null;
    ApplicationPersistence appPersistence = new ApplicationPersistence(persistenceFile);

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (structureCollection != null) {
            appPersistence.setLastDatasetUsed(structureCollection.file);
        }
        appPersistence.save(persistenceFile);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
    
    public enum NucleotideCompositionType {

        FREQUENCY, SHANNON, HIDE
    };
    DataLegend dataLegend1D = null;
    DataLegend dataLegend2D = null;
    NucleotideComposition nucleotideComposition = null;
    NucleotideCompositionType nucleotideCompositionType = NucleotideCompositionType.FREQUENCY;
    boolean showDNA = true;
    boolean showBonds = true;
    int numbering = 5;
    Color filteredDataColor = Color.darkGray;
    Color missingDataColor = Color.lightGray;
    SequenceData1D data1D = null;
    SequenceData2D data2D = null;
    double thresholdMin1D = Double.MIN_VALUE;
    double thresholdMax1D = Double.MAX_VALUE;
    double thresholdMin2D = Double.MIN_VALUE;
    double thresholdMax2D = Double.MAX_VALUE;
    boolean useUpperThreshold1D = false;
    boolean useLowerThreshold1D = false;
    boolean useUpperThreshold2D = false;
    boolean useLowerThreshold2D = false;
    int maxDistance = -1;
    int selected = 0;
    DistanceMatrix distanceMatrix;

    public MainApp() {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null) {
            return;
        }

        initComponents();

        drawPanel1 = new StructureDrawPanel();
        genomePanel1 = new FullGenomeDrawPanel();

        if (!defaultWorkspace.exists()) {
            defaultWorkspace.mkdir();
        }

        SwingUtilities.updateComponentTreeUI(projectChooserOpen);
        projectChooserOpen.setFileView(new ProjectFileView());
        projectChooserOpen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        projectChooserOpen.addChoosableFileFilter(new ProjectFileFilter());
        projectChooserOpen.setAcceptAllFileFilterUsed(false);
        projectChooserOpen.setApproveButtonText("Open project");
        SwingUtilities.updateComponentTreeUI(fileChooserOpen);
        SwingUtilities.updateComponentTreeUI(fileChooserSave);

        this.substructureScrollPane.setViewportView(drawPanel1);
        this.genomeScrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
        this.genomeScrollPane.getVerticalScrollBar().addAdjustmentListener(this);
        this.genomeScrollPane.setViewportView(genomePanel1);

        mainTabbedPane.addChangeListener(this);
        directoryListScrollPane.setViewportView(directoryStructureList);
        directoryStructureList.addListSelectionListener(this);
        parentListScrollPane.setViewportView(parentStructureList);
        parentStructureList.addListSelectionListener(this);
        childListScrollPane.setViewportView(childStructureList);
        childStructureList.addListSelectionListener(this);

        this.unbiasedFrequencyRadioButton.addChangeListener(this);
        this.unbiasedShannonRadioButton.addChangeListener(this);

        addWindowListener(this);
        setIconImage(appIcon.getImage());

        dataLegend1D = new DataLegend();
        dataLegend1D.showFilteredAndMissingData(filteredDataColor, missingDataColor);
        dataLegend1D.addChangeListener(this);
        legendPanel.add(dataLegend1D);

        dataLegend2D = new DataLegend();
        dataLegend2D.addChangeListener(this);
        legendPanel.add(dataLegend2D);

        splash.close();
    }

    public void openStructureCollectionFromFolder(File folder) {
        File collectionFile = new File(folder.getAbsolutePath() + File.separatorChar + "collection.xml");
        if (collectionFile.exists()) {
            openStructureCollectionFromFile(collectionFile);
        } else {
            JOptionPane.showMessageDialog(this, "Cannot open. This is not a StructureVis project.", "Error", JOptionPane.ERROR_MESSAGE);
            openDatasetAction();
        }
    }
    //ProgressDialogOpen progressDialog = new ProgressDialogOpen(this, false);

    public void openStructureCollectionFromFile(File file) {
        /*
         * final Runnable progressDialogThread = new Runnable() { public void
         * run() { final Toolkit toolkit = Toolkit.getDefaultToolkit(); final
         * Dimension screenSize = toolkit.getScreenSize(); final int x =
         * (screenSize.width - progressDialog.getWidth()) / 2; final int y =
         * (screenSize.height - progressDialog.getHeight()) / 2;
         * progressDialog.setLocation(x, y); progressDialog.setVisible(true); }
         * };
        SwingUtilities.invokeLater(progressDialogThread);
         */
        //   progressDialogThread.start();


        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dataLegend1D.setVisible(false);
        dataLegend2D.setVisible(false);
        //dataLegend2D = new DataLegend();
        data1D = null;
        data2D = null;
        nucleotideComposition = null;
        thresholdMin1D = Double.MIN_VALUE;
        thresholdMax1D = Double.MAX_VALUE;
        thresholdMin2D = Double.MIN_VALUE;
        thresholdMax2D = Double.MAX_VALUE;
        useUpperThreshold1D = false;
        dataLegend1D.upSliderOpen = true;
        useLowerThreshold1D = false;
        dataLegend1D.downSliderOpen = true;
        useUpperThreshold2D = false;
        dataLegend2D.upSliderOpen = true;
        useLowerThreshold2D = false;
        dataLegend2D.downSliderOpen = true;
        selected = 0;

        this.structureCollection = StructureCollectionStAXReader.loadStructureCollectionFromFile(file);
        collectionFolder = file.getParentFile();
        if (collectionFolder != null) {
            this.setTitle("StructureVis - " + collectionFolder.getAbsolutePath());
        }

        for (int i = 0; i < structureCollection.metadata.size(); i++) {
            Metadata metadata = structureCollection.metadata.get(i);
            if (metadata.getType().equals("MetadataFromFile")) {
                MetadataFromFile metadataFromFile = (MetadataFromFile) structureCollection.metadata.get(i);
                if (metadataFromFile.type.equals("NucleotideComposition")) {
                    NucleotideComposition nucleotideComposition = new NucleotideComposition(IO.getFileInCwd(collectionFolder, metadataFromFile.files.get(0)), Mapping.loadMapping(IO.getFileInCwd(collectionFolder, metadataFromFile.mappingFiles.get(0))));
                    nucleotideComposition.name = metadataFromFile.name;
                    structureCollection.nucleotideComposition.add(nucleotideComposition);
                } else if (metadataFromFile.type.equals("SequenceData1D")) {
                    ArrayList<SequenceData1D> data = new ArrayList<SequenceData1D>();
                    //int posColumn = 0;
                    int dataColumnIndex = 0;
                    for (int j = 0; j < metadataFromFile.files.size(); j++) {
                        /*
                         * if (j < metadataFromFile..size()) // pick last if
                         * fewer column positions specified than files {
                         * dataColumnIndex = j; }
                         */
                        if (j < metadataFromFile.dataColumns.size()) // pick last if fewer column positions specified than files
                        {
                            dataColumnIndex = j;
                        }
                        data.add(SequenceData1D.loadFromCsv(IO.getFileInCwd(collectionFolder, metadataFromFile.files.get(j)), Mapping.loadMapping(IO.getFileInCwd(collectionFolder, metadataFromFile.mappingFiles.get(j))), metadataFromFile.codonData.get(j), 0, metadataFromFile.dataColumns.get(dataColumnIndex)));
                    }
                    SequenceData1D data1D = SequenceData1D.combine(metadataFromFile.name, data);
                    data1D.colorGradient = metadataFromFile.colorGradient;
                    data1D.colorGradientSecondary = metadataFromFile.colorGradientSecondary;
                    data1D.useMin = metadataFromFile.useMin;
                    data1D.useMax = metadataFromFile.useMax;
                    data1D.min = metadataFromFile.min;
                    data1D.max = metadataFromFile.max;
                    data1D.dataTransform = metadataFromFile.dataTransform;
                    structureCollection.sequenceData1D.add(data1D);
                } else if (metadataFromFile.type.equals("SequenceData2D")) {
                    try {
                        SequenceData2D data2D = SequenceData2D.loadFromSparseMatrixFile(IO.getFileInCwd(collectionFolder, metadataFromFile.files.get(0)), Mapping.loadMapping(IO.getFileInCwd(collectionFolder, metadataFromFile.mappingFiles.get(0))));
                        data2D.name = metadataFromFile.name;
                        data2D.colorGradient = metadataFromFile.colorGradient;
                        data2D.colorGradientSecondary = metadataFromFile.colorGradientSecondary;
                        data2D.useMin = metadataFromFile.useMin;
                        data2D.useMax = metadataFromFile.useMax;
                        data2D.min = metadataFromFile.min;
                        data2D.max = metadataFromFile.max;
                        data2D.dataTransform = metadataFromFile.dataTransform;
                        structureCollection.sequenceData2D.add(data2D);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                "Could not load '" + metadataFromFile.name + "'.\n Error: " + ex.getCause().getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        // load list information
        Vector<String> nucleotideCompositionListModel = new Vector<String>();
        nucleotideCompositionListModel.add("None");
        for (int i = 0; i < structureCollection.nucleotideComposition.size(); i++) {
            nucleotideCompositionListModel.add(structureCollection.nucleotideComposition.get(i).name);
        }
        this.nucleotideCompositionBox.setModel(new DefaultComboBoxModel(nucleotideCompositionListModel));

        Vector<String> oneDimensionalDataListModel = new Vector<String>();
        oneDimensionalDataListModel.add("None");
        for (int i = 0; i < structureCollection.sequenceData1D.size(); i++) {
            oneDimensionalDataListModel.add(structureCollection.sequenceData1D.get(i).name);
        }
        this.oneDimensionalDataBox.setModel(new DefaultComboBoxModel(oneDimensionalDataListModel));

        Vector<String> twoDimensionalDataListModel = new Vector<String>();
        twoDimensionalDataListModel.add("None");
        for (int i = 0; i < structureCollection.sequenceData2D.size(); i++) {
            twoDimensionalDataListModel.add(structureCollection.sequenceData2D.get(i).name);
        }
        this.twoDimensionalDataBox.setModel(new DefaultComboBoxModel(twoDimensionalDataListModel));

        // load directory information
        directoryStructureFiles = new ArrayList<StructureListObject>();
        for (int i = 0; i < structureCollection.structures.size(); i++) {
            StructureListObject s = new StructureListObject(i, structureCollection.structures.get(i));
            directoryStructureFiles.add(s);
        }
        directoryStructureList.setListData(directoryStructureFiles.toArray());

        this.showDNA = structureCollection.dnaSequence;
        nucleotideComposition = structureCollection.getNucleotideComposition();

        layerPanel1 = new LayerPanel(this, structureCollection.genomeLength);
        genomeLayer = new GenomeLayer(layerPanel1, structureCollection.getGenomeOrganization(), "Sequence annotations");
        graphLayer1D = new GraphLayer(layerPanel1, data1D, "1D data (none)");
        graphLayer1D.canPin = false;
        layerPanel1.setGenomeLayer(genomeLayer);
        layerPanel1.setGraphLayer1D(graphLayer1D);

        jScrollPane1.setViewportView(layerPanel1);
        jScrollPane1.setBackground(Color.white);
        jScrollPane1.getViewport().setBackground(Color.white);

        layerPanel1.autofitWidth();
        drawPanel1.initialise(this);
        setGenomePanelVerticalPos = true;

        //genomeScrollPane.setV

        // load distance matrix and compute structures
        ProgressDialog p = new ProgressDialog(this);

        setCursor(Cursor.getDefaultCursor());
        openStructure(selected);

        try {
            this.nucleotideCompositionBox.setSelectedIndex(Math.min(1, nucleotideCompositionBox.getItemCount()));
            this.oneDimensionalDataBox.setSelectedIndex(Math.min(1, oneDimensionalDataBox.getItemCount()));
        } catch (java.lang.IllegalArgumentException ex) {
        }

        // reset ranking frame
        if (rankingFrame != null) {
            this.rankingFrame.dispose();
            this.rankingFrame = null;
        }

        if (searchFrame != null) {
            this.searchFrame.dispose();
            this.searchFrame = null;
        }
//        progressDialog.setVisible(false);
        //ContigencyPanel.createAndShowGUI(this);
    }
    double[] oneDimensionalSlidingData;
    //double[] paircount;
    int windowSize = 11;

    public void valueChanged(ListSelectionEvent e) {
        if (directoryStructureList.isEnabled()) {
            if (!e.getValueIsAdjusting()) {
                StructureListObject s = null;
                if (e.getSource().equals(parentStructureList)) {
                    s = (StructureListObject) parentStructureList.getSelectedValue();
                } else if (e.getSource().equals(directoryStructureList)) {
                    s = (StructureListObject) directoryStructureList.getSelectedValue();
                } else if (e.getSource().equals(childStructureList)) {
                    s = (StructureListObject) childStructureList.getSelectedValue();
                }
                if (s != null) {
                    openStructure(s.structureIndex);
                }

                /*
                 * if (s != null && selected !=
                 * parentStructureList.getSelectedIndex()) { //
                 * openStructure(directoryStructureList.getSelectedValue()); //
                 * TODO this should be something } else {
                 * //this.openStructure(0); // System.out.println("NULL"); }
                 */
            }
        }
    }

    public void update1DThreshold() {
        double min = dataLegend1D.transform.transform(dataLegend1D.transform.min);
        double max = dataLegend1D.transform.transform(dataLegend1D.transform.max);
        this.thresholdMax1D = dataLegend1D.transform.inverseTransform(min + dataLegend1D.upSliderPercentY * (max - min));
        this.thresholdMin1D = dataLegend1D.transform.inverseTransform(min + dataLegend1D.downSliderPercentY * (max - min));
        redrawVisibleTab();
    }

    public void update2DThreshold() {
        double min = dataLegend2D.transform.transform(dataLegend2D.transform.min);
        double max = dataLegend2D.transform.transform(dataLegend2D.transform.max);
        this.thresholdMax2D = dataLegend2D.transform.inverseTransform(min + dataLegend2D.upSliderPercentY * (max - min));
        this.thresholdMin2D = dataLegend2D.transform.inverseTransform(min + dataLegend2D.downSliderPercentY * (max - min));
        redrawVisibleTab();
    }

    public void stateChanged(ChangeEvent e) {
        // if(e.getSource().equals(dataLegend2D))
        //{
        if (e.getSource().equals(dataLegend1D)) {
            update1DThreshold();
            this.data1D.colorGradientSecondary = dataLegend1D.colorGradient;
            this.useLowerThreshold1D = !dataLegend1D.downSliderOpen;
            this.useUpperThreshold1D = !dataLegend1D.upSliderOpen;
            graphLayer1D.redraw();
            redrawVisibleTab();
        } else if (e.getSource().equals(dataLegend2D)) {
            update2DThreshold();
            this.data2D.colorGradientSecondary = dataLegend2D.colorGradient;
            this.useLowerThreshold2D = !dataLegend2D.downSliderOpen;
            this.useUpperThreshold2D = !dataLegend2D.upSliderOpen;
            redrawVisibleTab();
        } else if (e.getSource().equals(unbiasedFrequencyRadioButton) || e.getSource().equals(unbiasedShannonRadioButton)) {
            if (unbiasedFrequencyRadioButton.isSelected()) {
                this.nucleotideCompositionType = NucleotideCompositionType.FREQUENCY;
            } else {
                this.nucleotideCompositionType = NucleotideCompositionType.SHANNON;
            }
            redrawVisibleTab();
        }

        if (e.getSource().equals(mainTabbedPane)) {
            redrawVisibleTab();
        }
    }
    boolean setGenomePanelVerticalPos = true;

    public void redrawVisibleTab() {
        switch (mainTabbedPane.getSelectedIndex()) {
            case 0:
                this.drawPanel1.redraw();
                break;
            case 1:
                this.genomePanel1.redraw();
                if (setGenomePanelVerticalPos) {
                    int verticalPos = ((int) genomePanel1.basePosY + 30) - genomeScrollPane.getViewport().getHeight();
                    genomeScrollPane.getViewport().setViewPosition(new Point(0, verticalPos));
                    setGenomePanelVerticalPos = false;
                }
                break;
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (!e.getValueIsAdjusting()) {
            redrawVisibleTab();
        }
    }

    class StructureListObject {

        int structureIndex;
        Structure structure;
        String descriptiveName;

        public StructureListObject(int structureIndex, Structure s) {
            this.structureIndex = structureIndex;
            this.structure = s;
            this.descriptiveName = "" + s.name + " [" + s.getStartPosition() + "-" + s.getEndPosition() + "] L=" + s.length;
        }

        @Override
        public String toString() {
            return descriptiveName;
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

        nucleotideCompositionGroup = new javax.swing.ButtonGroup();
        verticalSplitPane = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        leftPanelCenterPanel = new javax.swing.JPanel();
        directoryListScrollPane = new javax.swing.JScrollPane();
        childListScrollPane = new javax.swing.JScrollPane();
        parentListScrollPane = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        oneDimensionalDataBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        twoDimensionalDataBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        unbiasedFrequencyRadioButton = new javax.swing.JRadioButton();
        unbiasedShannonRadioButton = new javax.swing.JRadioButton();
        nucleotideCompositionBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel();
        structureTextPane = new javax.swing.JTextPane();
        legendPanel = new javax.swing.JPanel();
        mainTabbedPane = new javax.swing.JTabbedPane();
        substructureScrollPane = new javax.swing.JScrollPane();
        genomeScrollPane = new javax.swing.JScrollPane();
        bottomPanel = new javax.swing.JPanel();
        distanceSlider = new javax.swing.JSlider();
        limitLabel = new javax.swing.JLabel();
        distanceLabel2D = new javax.swing.JLabel();
        data2DLabel = new javax.swing.JLabel();
        nextButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        openDatasetItem = new javax.swing.JMenuItem();
        createDatasetItem = new javax.swing.JMenuItem();
        exportDatasetItem = new javax.swing.JMenuItem();
        exitItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        rankingMenuItem = new javax.swing.JMenuItem();
        sequenceSearchItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StructureVis");

        verticalSplitPane.setDividerLocation(80);
        verticalSplitPane.setDividerSize(2);
        verticalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setMinimumSize(new java.awt.Dimension(100, 50));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        verticalSplitPane.setTopComponent(jPanel2);

        mainPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setPreferredSize(new java.awt.Dimension(175, 472));
        leftPanel.setLayout(new java.awt.BorderLayout(2, 0));

        leftPanelCenterPanel.setLayout(new javax.swing.BoxLayout(leftPanelCenterPanel, javax.swing.BoxLayout.PAGE_AXIS));

        directoryListScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Directory"));
        directoryListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        directoryListScrollPane.setMinimumSize(new java.awt.Dimension(33, 15));
        leftPanelCenterPanel.add(directoryListScrollPane);

        childListScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Child structures")));
        childListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        childListScrollPane.setMinimumSize(new java.awt.Dimension(41, 15));
        leftPanelCenterPanel.add(childListScrollPane);

        parentListScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Parent structures"));
        parentListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        parentListScrollPane.setMinimumSize(new java.awt.Dimension(33, 15));
        leftPanelCenterPanel.add(parentListScrollPane);

        leftPanel.add(leftPanelCenterPanel, java.awt.BorderLayout.CENTER);

        jPanel4.setPreferredSize(new java.awt.Dimension(175, 250));

        jLabel3.setText("1-dimensional data");

        oneDimensionalDataBox.setMinimumSize(new java.awt.Dimension(100, 18));
        oneDimensionalDataBox.setPreferredSize(new java.awt.Dimension(140, 20));
        oneDimensionalDataBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oneDimensionalDataBoxActionPerformed(evt);
            }
        });

        jLabel4.setText("2-dimensional data");

        twoDimensionalDataBox.setMinimumSize(new java.awt.Dimension(100, 18));
        twoDimensionalDataBox.setPreferredSize(new java.awt.Dimension(140, 20));
        twoDimensionalDataBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoDimensionalDataBoxActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Nucleotide composition data"));

        jLabel5.setText("Composition type");

        nucleotideCompositionGroup.add(unbiasedFrequencyRadioButton);
        unbiasedFrequencyRadioButton.setSelected(true);
        unbiasedFrequencyRadioButton.setText("Unbiased frequency");

        nucleotideCompositionGroup.add(unbiasedShannonRadioButton);
        unbiasedShannonRadioButton.setText("Unbiased shannon entropy");

        nucleotideCompositionBox.setMinimumSize(new java.awt.Dimension(100, 18));
        nucleotideCompositionBox.setPreferredSize(new java.awt.Dimension(140, 20));
        nucleotideCompositionBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nucleotideCompositionBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Sequence alignment");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nucleotideCompositionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(unbiasedFrequencyRadioButton)
                    .addComponent(jLabel2)
                    .addComponent(unbiasedShannonRadioButton))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nucleotideCompositionBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unbiasedFrequencyRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(unbiasedShannonRadioButton))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(oneDimensionalDataBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(twoDimensionalDataBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(oneDimensionalDataBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(twoDimensionalDataBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        leftPanel.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        mainPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        rightPanel.setPreferredSize(new java.awt.Dimension(180, 520));
        rightPanel.setLayout(new javax.swing.BoxLayout(rightPanel, javax.swing.BoxLayout.PAGE_AXIS));

        structureTextPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        structureTextPane.setContentType("text/html");
        structureTextPane.setEditable(false);
        structureTextPane.setMargin(new java.awt.Insets(10, 10, 10, 10));
        structureTextPane.setOpaque(false);
        rightPanel.add(structureTextPane);

        legendPanel.setPreferredSize(new java.awt.Dimension(150, 500));
        legendPanel.setLayout(new java.awt.GridLayout(0, 1));
        rightPanel.add(legendPanel);

        mainPanel.add(rightPanel, java.awt.BorderLayout.EAST);

        mainTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mainTabbedPaneStateChanged(evt);
            }
        });
        mainTabbedPane.addTab("Substructure view", substructureScrollPane);
        mainTabbedPane.addTab("Full-scale view", genomeScrollPane);

        mainPanel.add(mainTabbedPane, java.awt.BorderLayout.CENTER);
        mainTabbedPane.getAccessibleContext().setAccessibleName("Substructure view");

        verticalSplitPane.setRightComponent(mainPanel);

        getContentPane().add(verticalSplitPane, java.awt.BorderLayout.CENTER);

        bottomPanel.setPreferredSize(new java.awt.Dimension(607, 40));

        distanceSlider.setMajorTickSpacing(10);
        distanceSlider.setMinorTickSpacing(1);
        distanceSlider.setSnapToTicks(true);
        distanceSlider.setValue(100);
        distanceSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                distanceSliderStateChanged(evt);
            }
        });

        limitLabel.setText("Limit nearby 2D interactions");

        distanceLabel2D.setText("Display all");

        data2DLabel.setText(" ");

        nextButton.setText("Next");
        nextButton.setPreferredSize(new java.awt.Dimension(75, 23));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        previousButton.setText("Previous");
        previousButton.setPreferredSize(new java.awt.Dimension(75, 23));
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previousButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addComponent(limitLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(distanceSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(distanceLabel2D)
                .addGap(18, 18, 18)
                .addComponent(data2DLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 3135, Short.MAX_VALUE)
                .addContainerGap())
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(distanceLabel2D, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(data2DLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addComponent(distanceSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(previousButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(limitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        jMenu1.setText("File");

        openDatasetItem.setText("Open dataset");
        openDatasetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDatasetItemActionPerformed(evt);
            }
        });
        jMenu1.add(openDatasetItem);

        createDatasetItem.setText("Create dataset");
        createDatasetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createDatasetItemActionPerformed(evt);
            }
        });
        jMenu1.add(createDatasetItem);

        exportDatasetItem.setText("Export dataset to zip");
        exportDatasetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDatasetItemActionPerformed(evt);
            }
        });
        jMenu1.add(exportDatasetItem);

        exitItem.setText("Exit ");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitItem);

        jMenuBar1.add(jMenu1);

        jMenu3.setText("Analysis");

        rankingMenuItem.setText("Ranking");
        rankingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rankingMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(rankingMenuItem);

        sequenceSearchItem.setText("Sequence Search");
        sequenceSearchItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequenceSearchItemActionPerformed(evt);
            }
        });
        jMenu3.add(sequenceSearchItem);

        jMenuBar1.add(jMenu3);

        jMenu2.setText("Help");

        jMenuItem4.setText("Help contents");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(aboutMenuItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public ArrayList<StructureListObject> getParentStructures(int start, int end) {
        ArrayList<StructureListObject> parentStructures = new ArrayList<StructureListObject>();
        for (int i = 0; i < structureCollection.structures.size(); i++) {
            Structure s = structureCollection.structures.get(i);
            if (s.getStartPosition() <= start && end <= s.getEndPosition()) {
                parentStructures.add(new StructureListObject(i, structureCollection.structures.get(i)));
            }
        }

        ArrayList<StructureListObject> ret = new ArrayList<StructureListObject>();
        while (parentStructures.size() > 0) {
            int min = 0;
            for (int j = 0; j < parentStructures.size(); j++) {
                int a = parentStructures.get(j).structure.length;
                int b = parentStructures.get(min).structure.length;
                if (a <= b) {
                    min = j;
                }
            }
            ret.add(parentStructures.remove(min));
        }
        return ret;
    }

    public ArrayList<StructureListObject> getChildStructures(int start, int end) {
        ArrayList<StructureListObject> childStructures = new ArrayList<StructureListObject>();
        for (int i = 0; i < structureCollection.structures.size(); i++) {
            Structure s = structureCollection.structures.get(i);
            if (s.getStartPosition() >= start && end >= s.getEndPosition()) {
                childStructures.add(new StructureListObject(i, structureCollection.structures.get(i)));
            }
        }

        ArrayList<StructureListObject> ret = new ArrayList<StructureListObject>();
        while (childStructures.size() > 0) {
            int min = 0;
            for (int j = 0; j < childStructures.size(); j++) {
                int a = childStructures.get(j).structure.length;
                int b = childStructures.get(min).structure.length;
                if (a <= b) {
                    min = j;
                }
            }
            ret.add(childStructures.remove(min));
        }
        return ret;
    }

    /**
     * Get the smallest structure at a specified nucleotide position.
     *
     * @param position
     * @return
     */
    public int getStructureIndexAtPosition(int position) {
        int s = -1;
        if (structureCollection != null) {
            for (int i = 0; i < structureCollection.structures.size(); i++) {
                if (structureCollection.structures.get(i).getStartPosition() <= position && structureCollection.structures.get(i).getEndPosition() >= position) {
                    if (s != -1) {
                        int ilen = structureCollection.structures.get(i).length;
                        int slen = structureCollection.structures.get(s).length;
                        if (ilen < slen) {
                            s = i;
                        }
                    } else {
                        s = i;
                    }
                }
            }
        }
        return s;
    }
    
    public Structure getLargestStructureAtPosition(int position, int lessThanLength) {
        Structure s = null;
        if (structureCollection != null) {
            for (int i = 0; i < structureCollection.structures.size(); i++) {
                if (structureCollection.structures.get(i).length < lessThanLength && structureCollection.structures.get(i).getStartPosition() <= position && structureCollection.structures.get(i).getEndPosition() >= position) {
                    if (s != null) {
                        int ilen = structureCollection.structures.get(i).length;
                        int slen = s.length;
                        if (ilen > slen) {
                            s = structureCollection.structures.get(i);
                        }
                    } else {
                       s = structureCollection.structures.get(i);
                    }
                }
            }
        }
        return s;
    }

    public ArrayList<Structure> getStructuresAtPosition(int position) {
        ArrayList<Structure> structures = new ArrayList<Structure>();
        if (structureCollection != null) 
        {
            for (int i = 0; i < structureCollection.structures.size(); i++) {
                if (structureCollection.structures.get(i).getStartPosition() <= position && structureCollection.structures.get(i).getEndPosition() >= position) {
                    structures.add(structureCollection.structures.get(i));
                }
            }
            
        }
        return structures;
    }
    
    public ArrayList<Structure> getStructuresInRegion(int start, int end) {
        ArrayList<Structure> structures = new ArrayList<Structure>();
        if (structureCollection != null) 
        {
            for (int i = 0; i < structureCollection.structures.size(); i++) {
                if (structureCollection.structures.get(i).getStartPosition() >= start && structureCollection.structures.get(i).getEndPosition() <= end) {
                    structures.add(structureCollection.structures.get(i));
                }
            }
            
        }
        return structures;
    }

    

    public Structure getStructureAtPosition(int position) {
        int s = getStructureIndexAtPosition(position);
        if (s >= 0 && s < directoryStructureFiles.size()) {
            return structureCollection.structures.get(s);
        }
        return null;
    }

    public void gotoPosition(int position) {
        int s = getStructureIndexAtPosition(position);
        if (s >= 0 && s < directoryStructureFiles.size()) {
            openStructure(s);
        }

    }

    public void loadParentStructures(int start, int end) {
        parentStructureList.setListData(getParentStructures(start, end).toArray());
    }

    public void loadChildStructures(int start, int end) {
        childStructureList.setListData(getChildStructures(start, end).toArray());
    }

    public void openStructure(Structure structure)
    {
        openStructure(structureCollection.structures.indexOf(structure));
    }
    
    public void openStructure(int index) {
        final Structure structure = structureCollection.structures.get(index);
        boolean openStructure = true;
        if (structure.length > 300) {
            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(this,
                    "This structure is longer than 300 nucleotides, opening this structure may cause StructureVis to hang.\nAre you sure you want to continue?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (n == 0) {
            } else {
                openStructure = false;
            }
        }

        if (openStructure) {
            mainTabbedPane.setSelectedIndex(0);
            if (structureCollection != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                selected = index;
                if (selected < 0) {
                    selected = 0;
                }
                setEnabled(false);

                directoryStructureList.setSelectedIndex(selected);
                String text = "<font face=\"Arial\">";
                text += "<b><u>Structure info</u></b><br>";
                text += "<font size=\"0\"><br></font><font size=\"3\">";
                text += "<b>ID:</b> " + structure.name + "<br>";
                text += "<b>Position:</b> " + structure.getStartPosition() + " - " + structure.getEndPosition() + "<br>";
                text += "<b>Length:</b> " + structure.length + "<br>";
                //text += "<b>NASP score:</b> " + structure.naspScore + "<br>";
                if (structureTextPane != null && text != null) {
                    structureTextPane.setText(text);
                }
                drawPanel1.openStructure(structure);
                genomeLayer.selectStructure(structure);
                setEnabled(true);

                loadParentStructures(structure.getStartPosition(), structure.getEndPosition());
                loadChildStructures(structure.getStartPosition(), structure.getEndPosition());
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                /*
                 * final Runnable distanceComputationThread = new Runnable() {
                 *
                 * public void run() { //
                 * stopComputationButton.setVisible(true);
                 *
                 * //AnnotatedStructure structure = new
                 * AnnotatedStructure(directoryStructureFiles.get(selected).file);
                 * String text = "<font face=\"Arial\">"; text +=
                 * "<b><u>Structure info</u></b><br>"; text += "<font
                 * size=\"0\"><br></font><font size=\"3\">"; text += "<b>ID:</b>
                 * " + structure.name + "<br>"; text += "<b>Position:</b> " +
                 * structure.getStartPosition() + " - " +
                 * structure.getEndPosition() + "<br>"; text += "<b>Length:</b>
                 * " + structure.length + "<br>"; //text += "<b>NASP score:</b>
                 * " + structure.naspScore + "<br>"; if (structureTextPane !=
                 * null && text != null) { structureTextPane.setText(text); }
                 * drawPanel1.openStructure(structure);
                 * genomeLayer.selectStructure(structure); setEnabled(true);
                 *
                 * loadParentStructures(structure.getStartPosition(),
                 * structure.getEndPosition());
                 * loadChildStructures(structure.getStartPosition(),
                 * structure.getEndPosition());
                 * setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 * } };
                 *
                 * new Thread(distanceComputationThread).start();
                 */
            }

            redrawVisibleTab();
        }
    }

    /*
     * @Override public void setEnabled(boolean enabled) { //
     * this.openFileButton.setEnabled(enabled);
     * this.directoryStructureList.setEnabled(enabled);
     * this.parentStructureList.setEnabled(enabled);
     * this.childStructureList.setEnabled(enabled); //
     * this.genomeLegend1.setEnabled(enabled);
     * this.nucleotideCompositionBox.setEnabled(enabled);
     * this.oneDimensionalDataBox.setEnabled(enabled);
     * this.twoDimensionalDataBox.setEnabled(enabled);
     * this.previousButton.setEnabled(enabled);
     * this.nextButton.setEnabled(enabled);
     * //this.showSubstructuresButton.setEnabled(enabled);
     * this.distanceSlider.setEnabled(enabled); }
     */
    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed

        selected--;
        if (selected < 0) {
            selected = directoryStructureFiles.size() - 1;
        }
        openStructure(selected);
    }//GEN-LAST:event_previousButtonActionPerformed
    boolean next = false;

    public void callNext() {
        if (next) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            nextButtonActionPerformed(null);
        }
    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if (drawPanel1.saveStructures) {
            next = true;
//            stopComputationButtonActionPerformed(null);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            drawPanel1.saveAsPNG(new File("c:/project/hepacivirus/images/hcv-" + (selected + 1) + ".png"));
        }
        selected = (selected + 1) % directoryStructureFiles.size();
        openStructure(selected);

        //stopComputationButtonActionPerformed(null);
    }//GEN-LAST:event_nextButtonActionPerformed

    private void oneDimensionalDataBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oneDimensionalDataBoxActionPerformed
        if (oneDimensionalDataBox.getSelectedIndex() == 0) {
            //drawPanel1.oneDimensionalData = StructureDrawPanel.HIDE;
            this.data1D = null;
            graphLayer1D.isPinned = false;
            graphLayer1D.canPin = false;
            graphLayer1D.setData(data1D);
            graphLayer1D.setLayerName("1D data (none)");
            layerPanel1.updatePanel();
            dataLegend1D.setVisible(false);
            graphLayer1D.redraw();
        } else {
            this.data1D = structureCollection.sequenceData1D.get(oneDimensionalDataBox.getSelectedIndex() - 1);
            graphLayer1D.isPinned = false;
            graphLayer1D.canPin = true;
            graphLayer1D.setData(data1D);
            graphLayer1D.redraw();
            layerPanel1.revalidate();
            graphLayer1D.setLayerName("1D data (" + this.data1D.name + ")");
            layerPanel1.updatePanel();
            dataLegend1D.initialise(data1D);
            update1DThreshold();
        }
        //  genomeLegend1.redraw();
        redrawVisibleTab();
    }//GEN-LAST:event_oneDimensionalDataBoxActionPerformed

    private void distanceSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_distanceSliderStateChanged

        if (distanceSlider.getValue() == distanceSlider.getMaximum()) {
            distanceLabel2D.setText("Display all");
            maxDistance = -1;
        } else {
            distanceLabel2D.setText(distanceSlider.getValue() + "");
            maxDistance = distanceSlider.getValue();
        }

        if (!distanceSlider.getValueIsAdjusting()) {
            redrawVisibleTab();
        }
    }//GEN-LAST:event_distanceSliderStateChanged

    private void nucleotideCompositionBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nucleotideCompositionBoxActionPerformed
        if (nucleotideCompositionBox.getSelectedIndex() == 0) {
            this.nucleotideComposition = null;
        } else {
            this.nucleotideComposition = structureCollection.nucleotideComposition.get(nucleotideCompositionBox.getSelectedIndex() - 1);
        }
        redrawVisibleTab();
    }//GEN-LAST:event_nucleotideCompositionBoxActionPerformed

    public int openDatasetAction() {
        projectChooserOpen.setCurrentDirectory(defaultWorkspace);
        int returnVal = projectChooserOpen.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            openStructureCollectionFromFolder(projectChooserOpen.getSelectedFile());
        }
        return returnVal;
    }

    private void mainTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainTabbedPaneStateChanged
        //redrawVisibleTab();
    }//GEN-LAST:event_mainTabbedPaneStateChanged

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        AboutDialog d = new AboutDialog(this, true);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - d.getWidth()) / 2;
        final int y = (screenSize.height - d.getHeight()) / 2;
        d.setLocation(x, y);
        d.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        System.exit(0);
}//GEN-LAST:event_exitItemActionPerformed

    private void exportDatasetItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDatasetItemActionPerformed
        try {
            fileChooserSave.setDialogTitle("Export project to zip");
            fileChooserSave.setSelectedFile(new File(fileChooserSave.getCurrentDirectory().getPath() + "/" + collectionFolder.getName() + ".project.zip"));
            int returnVal = fileChooserSave.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                IO.zipFolder(collectionFolder, fileChooserSave.getSelectedFile(), 2);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            fileChooserSave.setDialogTitle("Open");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
}//GEN-LAST:event_exportDatasetItemActionPerformed

    private void createDatasetItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createDatasetItemActionPerformed
        new WizardMain().show(this);
}//GEN-LAST:event_createDatasetItemActionPerformed

    private void openDatasetItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDatasetItemActionPerformed
        openDatasetAction();
}//GEN-LAST:event_openDatasetItemActionPerformed

    private void twoDimensionalDataBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twoDimensionalDataBoxActionPerformed
        if (twoDimensionalDataBox.getSelectedIndex() == 0) {
            // drawPanel1.show2DData = false;
            dataLegend2D.setVisible(false);
            this.data2D = null;
        } else {
            // drawPanel1.show2DData = true;
            this.data2D = structureCollection.sequenceData2D.get(twoDimensionalDataBox.getSelectedIndex() - 1);
            this.thresholdMin2D = data2D.dataTransform.min;
            this.thresholdMax2D = data2D.dataTransform.max;
            dataLegend2D.initialise(data2D);
            update2DThreshold();
        }
        redrawVisibleTab();
    }//GEN-LAST:event_twoDimensionalDataBoxActionPerformed
    JFrame rankingFrame = null;
    RankingPanel rankingPanel = null;
    private void rankingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rankingMenuItemActionPerformed
        if (rankingFrame == null) {
            rankingFrame = new JFrame("Structure ranking");
            rankingFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            rankingFrame.setIconImage(this.appIcon.getImage());

            rankingPanel = new RankingPanel(this);
            rankingPanel.setOpaque(true); //content panes must be opaque
            rankingFrame.setContentPane(rankingPanel);
            rankingFrame.addWindowListener(new WindowListener() {

                public void windowClosed(WindowEvent arg0) {
                }

                public void windowActivated(WindowEvent arg0) {
                }

                public void windowClosing(WindowEvent arg0) {
                    rankingPanel.kill();
                }

                public void windowDeactivated(WindowEvent arg0) {
                }

                public void windowDeiconified(WindowEvent arg0) {
                }

                public void windowIconified(WindowEvent arg0) {
                }

                public void windowOpened(WindowEvent arg0) {
                }
            });
        }

        if (rankingPanel != null) {
            if (rankingPanel.currentThread == null) {
                rankingPanel.performRanking();
            }
        }

        //Display the window.
        rankingFrame.pack();
        rankingFrame.setVisible(true);

        //RankingPanel.createAndShowGUI(this);
    }//GEN-LAST:event_rankingMenuItemActionPerformed
    Desktop desktop = null;
    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }

        try {
            if (desktop != null) {
                desktop.open(new File("Manual/index.html"));
                /*
                 * try { * //desktop.browse(new URI(new
                 * File("Manual/index.html").getAbsolutePath())); } catch
                 * (URISyntaxException ex) {
                 * Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE,
                 * null, ex); }
                 */
            } else {
                HelpDialog.showDialog(appIcon, new File("Manual/toc.html"), new File("Manual/gettingstarted.html"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed
    JFrame searchFrame = null;
    SearchPanel searchPanel = null;
    private void sequenceSearchItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequenceSearchItemActionPerformed
        if (searchFrame == null) {
            searchFrame = new JFrame("Sequence Search");
            searchFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            searchFrame.setIconImage(this.appIcon.getImage());

            searchPanel = new SearchPanel(this);
            searchPanel.setOpaque(true); //content panes must be opaque
            searchFrame.setContentPane(searchPanel);
            searchFrame.addWindowListener(new WindowListener() {

                public void windowClosed(WindowEvent arg0) {
                }

                public void windowActivated(WindowEvent arg0) {
                }

                public void windowClosing(WindowEvent arg0) {
                    searchPanel.kill();
                }

                public void windowDeactivated(WindowEvent arg0) {
                }

                public void windowDeiconified(WindowEvent arg0) {
                }

                public void windowIconified(WindowEvent arg0) {
                }

                public void windowOpened(WindowEvent arg0) {
                }
            });
        }

        if (searchPanel != null) {
            if (searchPanel.currentThread == null) {
                searchPanel.performSearch();
            }
        }

        //Display the window.
        searchFrame.pack();
        searchFrame.setVisible(true);
    }//GEN-LAST:event_sequenceSearchItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }

        /*
         * java.awt.EventQueue.invokeLater(new Runnable() {
         *
         * public void run() {
         *
         *
         * }
         * });
         */
        MainApp mainapp = new MainApp();
        mainapp.setSize(800, 600);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - mainapp.getWidth()) / 2;
        final int y = (screenSize.height - mainapp.getHeight()) / 2;
        mainapp.setLocation(x, y);
        mainapp.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainapp.setVisible(true);

        StartupDialog s = new StartupDialog(mainapp);
        s.setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JScrollPane childListScrollPane;
    private javax.swing.JMenuItem createDatasetItem;
    public javax.swing.JLabel data2DLabel;
    private javax.swing.JScrollPane directoryListScrollPane;
    private javax.swing.JLabel distanceLabel2D;
    private javax.swing.JSlider distanceSlider;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JMenuItem exportDatasetItem;
    private javax.swing.JScrollPane genomeScrollPane;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    public javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel leftPanelCenterPanel;
    private javax.swing.JPanel legendPanel;
    private javax.swing.JLabel limitLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JButton nextButton;
    private javax.swing.JComboBox nucleotideCompositionBox;
    private javax.swing.ButtonGroup nucleotideCompositionGroup;
    private javax.swing.JComboBox oneDimensionalDataBox;
    private javax.swing.JMenuItem openDatasetItem;
    private javax.swing.JScrollPane parentListScrollPane;
    private javax.swing.JButton previousButton;
    private javax.swing.JMenuItem rankingMenuItem;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JMenuItem sequenceSearchItem;
    private javax.swing.JTextPane structureTextPane;
    public javax.swing.JScrollPane substructureScrollPane;
    private javax.swing.JComboBox twoDimensionalDataBox;
    private javax.swing.JRadioButton unbiasedFrequencyRadioButton;
    private javax.swing.JRadioButton unbiasedShannonRadioButton;
    private javax.swing.JSplitPane verticalSplitPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Makes upper-left corner position (0,0)
     *
     * @param coordinates
     * @return
     */
    public static Point2D.Double[] normaliseStructureCoordinates(ArrayList<Point2D.Double> coordinates) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double maxy = Double.MIN_VALUE;

        for (int i = 0; i < coordinates.size(); i++) {
            Point2D.Double pos = coordinates.get(i);
            minx = Math.min(minx, pos.x);
            miny = Math.min(miny, pos.y);
            maxx = Math.max(maxx, pos.x);
            maxy = Math.max(maxy, pos.y);
        }
        Point2D.Double normalisedPositions[] = new Point2D.Double[coordinates.size()];
        for (int i = 0; i < normalisedPositions.length; i++) {
            normalisedPositions[i] = new Point2D.Double();
            normalisedPositions[i].x = 0 + (coordinates.get(i).x - minx);
            normalisedPositions[i].y = 0 + (coordinates.get(i).y - miny);
        }
        return normalisedPositions;
    }

    public ArrayList<Point2D.Double> getStructureCoordinates(String dotBracketString) {
        ArrayList<Point2D.Double> coordinates = new ArrayList<Point2D.Double>();

        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(new File("temp.dbn")));
            buffer.write(">temp");
            buffer.newLine();
            for (int i = 0; i < dotBracketString.length(); i++) {
                buffer.write("A");
            }
            buffer.newLine();
            buffer.write(dotBracketString);
            buffer.newLine();
            buffer.close();

            //Process p = Runtime.getRuntime().exec(appPersistence.getRNAPlotCommandForOS("temp.dbn")); 
            Process p = appPersistence.execRNAPlot("temp.dbn");

            /*
             * BufferedReader errorStream = new BufferedReader(new
             * InputStreamReader(p.getErrorStream())); String errorLine = null;
             * if((errorLine = errorStream.readLine()) != null) {
             * System.err.println(errorLine); } errorStream.close();
             * System.out.println("HERE1");
             *
             * BufferedReader inputStream = new BufferedReader(new
             * InputStreamReader(p.getInputStream())); String inputLine = null;
             * if((inputLine = inputStream.readLine()) != null) {
             * System.out.println(inputLine); } inputStream.close();
             * System.out.println("HERE2");
             */

            int code = p.waitFor();
            if (code == 0) {
                BufferedReader bufferIn = new BufferedReader(new FileReader(new File("temp_ss.ps")));
                String textline = null;
                boolean readCoord = false;
                while ((textline = bufferIn.readLine()) != null) {
                    if (textline.startsWith("/coor")) {
                        readCoord = true;
                    } else if (readCoord) {
                        String[] split = textline.substring(1).replaceAll("]", "").split("\\s");
                        if (textline.startsWith("[")) {
                            Point2D.Double point = new Point2D.Double(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                            coordinates.add(point);
                        } else {
                            readCoord = false;
                        }
                    }
                }
                bufferIn.close();
            } else {
                JOptionPane.showMessageDialog(this,
                        "RNAplot could not be executed to calculate the structure coordinates.",
                        "Fatal error",
                        JOptionPane.ERROR_MESSAGE);
                //System.err.println();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "RNAplot could not be executed to calculate the structure coordinates.",
                    "Fatal error",
                    JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(this,
                    "RNAplot could not be executed to calculate the structure coordinates.",
                    "Fatal error",
                    JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        //ArrayList<Point2D.Double> origCoordinates = (ArrayList<Point2D.Double>) coordinates.clone();
        //coordinates = adjustCoordinates(origCoordinates, coordinates, dotBracketString);
        //coordinates = adjustCoordinates(origCoordinates, coordinates, dotBracketString);
        //coordinates = adjustCoordinates(origCoordinates, coordinates, dotBracketString);
        // coordinates = adjustCoordinates(origCoordinates, coordinates, dotBracketString);
        // coordinates = adjustCoordinates(coordinates, dotBracketString);
        //coordinates = adjustCoordinates(coordinates, dotBracketString);
        //coordinates = adjustCoordinates(coordinates, dotBracketString);
        //coordinates = adjustCoordinates(coordinates, dotBracketString);
        //coordinates = adjustCoordinates(coordinates, dotBracketString);
        //coordinates = adjustCoordinates(coordinates, dotBracketString);


        //boolean left = true;
        int[] pairedSites = RNAFoldingTools.getPairedSitesFromDotBracketString(dotBracketString);
        for (int i = 1; i < pairedSites.length - 1; i++) {
            Point2D.Double a = coordinates.get(i - 1);
            Point2D.Double b = coordinates.get(i);
            Point2D.Double c = coordinates.get(i + 1);

            System.out.println(dotBracketString.charAt(i));
            double d1 = Point2D.distance(a.x, a.y, b.x, b.y);
            System.out.println((i - 1) + "\t" + i + "\t" + d1);
            double d2 = Point2D.distance(b.x, b.y, c.x, c.y);
            System.out.println((i) + "\t" + (i + 1) + "\t" + d2);
        }

        return coordinates;
    }

    public ArrayList<Point2D.Double> adjustCoordinates(ArrayList<Point2D.Double> origCoordinates, ArrayList<Point2D.Double> coordinates, String dotBracketString) {
        ArrayList<Point2D.Double> adjusted = new ArrayList<Point2D.Double>(coordinates.size());
        adjusted.add(coordinates.get(0));
        char lastBracket = '(';
        for (int i = 1; i < dotBracketString.length() - 1; i++) {
            if (dotBracketString.charAt(i) != '.') {
                lastBracket = dotBracketString.charAt(i);
            }

            Point2D.Double a1 = coordinates.get(i - 1);
            Point2D.Double a2 = coordinates.get(i);
            Point2D.Double a3 = coordinates.get(i + 1);

            Point2D.Double o1 = origCoordinates.get(i - 1);
            Point2D.Double o2 = origCoordinates.get(i);
            Point2D.Double o3 = origCoordinates.get(i + 1);

            Point2D.Double newPoint = (Point2D.Double) a2.clone();

            if (dotBracketString.charAt(i) == '.') {
                double d1 = Point2D.distance(a1.x, a1.y, a2.x, a2.y);
                if (d1 < 14) {
                    if (lastBracket == '(') {
                        newPoint.x -= (15 - d1);
                    } else {
                        newPoint.x += (15 - d1);
                    }
                }
            }

            adjusted.add(newPoint);
        }
        adjusted.add(coordinates.get(coordinates.size() - 1));
        return adjusted;
    }
}
