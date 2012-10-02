/*
 * SequenceDialog2D.java
 *
 * Created on 06 Dec 2011, 11:10:40 AM
 */
package structurevis.ui.datacreation;

import java.awt.Image;
import structurevis.ui.datacreation.verifiers.DecimalVerifier;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import structurevis.data.SparseMatrix;
import structurevis.structures.io.ReadseqTools;
import structurevis.ui.DataLegend;
import structurevis.ui.DataTransform;
import structurevis.ui.DataTransform.TransformType;
import structurevis.ui.datacreation.verifiers.TextVerifier;
import structurevis.ui.datacreation.wizard.Wizard;

/**
 *
 * @author Michael Golden
 */
public class SequenceDialog2D extends javax.swing.JDialog implements KeyListener {

    ImageIcon errorIcon = new ImageIcon(getClass().getResource("/structurevis/resources/error.png"));
    ImageIcon correctIcon = new ImageIcon(getClass().getResource("/structurevis/resources/correct.png"));
    Image appIcon = new ImageIcon(getClass().getResource("/structurevis/resources/sv_icon.png")).getImage();
    DataOverlay dataOverlay = new DataOverlay();
    int currentIndex = 0;
    DataSource2D currentItem;

    /*
     * public enum ExitValue {
     *
     * EXIT_AND_ADD, EXIT_AND_CANCEL }; public ExitValue returnVal = ExitValue.EXIT_AND_CANCEL;
     */
    DefaultComboBoxModel positionBoxModel = new DefaultComboBoxModel();
    DefaultComboBoxModel dataBoxModel = new DefaultComboBoxModel();
    DataModel dataModel;
    int openIndex = -1;
    // store minimum and maximum values of datasets
    HashMap<DataSource2D, Double> minDataTable = new HashMap<DataSource2D, Double>();
    HashMap<DataSource2D, Double> maxDataTable = new HashMap<DataSource2D, Double>();
    DecimalVerifier decimalVerifier = new DecimalVerifier();
    TextVerifier textVerifier = new TextVerifier();
    DataLegend dataLegend1 = new DataLegend();

    /**
     * Creates new form SequenceDialog1D
     */
    public SequenceDialog2D(java.awt.Frame parent, boolean modal, DataModel dataModel, int openOverlay) {
        super(parent, modal);
        initComponents();

        if (openOverlay == -1) {
            addNewDataSource();
        }

        this.dataModel = dataModel;
        this.openIndex = openOverlay;

        if (openOverlay == -1) {
            // dataOverlay.fieldName = );
            this.nameField.setText(dataModel.getNextName("SequenceData2D_"));
            updateTextFields();
        }

        if (openOverlay != -1) {
            dataOverlay = dataModel.overlays.get(openOverlay);
            this.dataLegend1.colorGradient = dataOverlay.colorGradient;
            this.nameField.setText(dataOverlay.fieldName);
            this.transformTypeComboBox.setSelectedIndex(dataOverlay.transformType.ordinal());
            // TODO these fields must be stored
            if (dataOverlay.useMin) {
                this.dataMinComboBox.setSelectedIndex(0);
            } else {
                this.dataMinComboBox.setSelectedIndex(1);
            }
            if (dataOverlay.useMax) {
                this.dataMaxComboBox.setSelectedIndex(0);
            } else {
                this.dataMaxComboBox.setSelectedIndex(1);
            }
            this.minField.setText(dataOverlay.minValue + "");
            this.maxField.setText(dataOverlay.maxValue + "");
            currentItem = dataOverlay.dataSources2D.get(0);
            updateDataLegend();
            displayDataSource(0);
        } else {
            updateDataLegend();
        }

        this.jTextPane1.setCaretPosition(0);

        nameField.addKeyListener(this);
        minField.addKeyListener(this);
        maxField.addKeyListener(this);
        nameField.setInputVerifier(textVerifier);
        minField.setInputVerifier(decimalVerifier);
        maxField.setInputVerifier(decimalVerifier);

        setTitle("Add 2-dimensional data");
        setIconImage(appIcon);
        dataLegend1.showEditMode();
        jScrollPane1.setViewportView(dataLegend1);
        updateToolTips();
    }

    public void updateToolTips() {
        // consensus structure
        errorLabel_dataFile.setIcon(errorIcon);
        errorLabel_dataFile.setToolTipText("");
        if (dataFileField.getText().equals("")) {
            errorLabel_dataFile.setToolTipText("Please select a file.");
        } else if (!new File(dataFileField.getText()).exists()) {
            errorLabel_dataFile.setToolTipText("The specified file does not exist.");
        } else {
            errorLabel_dataFile.setIcon(correctIcon);
        }

        // reference alignment
        errorLabel_mappingAlignment.setIcon(errorIcon);
        errorLabel_mappingAlignment.setIcon(errorIcon);
        errorLabel_mappingAlignment.setToolTipText("");
        if (mappingFileField.getText().equals("")) {
            errorLabel_mappingAlignment.setToolTipText("Please select a file.");
        } else if (!new File(mappingFileField.getText()).exists()) {
            errorLabel_mappingAlignment.setToolTipText("The specified file does not exist.");
        } else if (!ReadseqTools.isKnownFormat(new File(mappingFileField.getText()))) {
            errorLabel_mappingAlignment.setToolTipText("The alignment format is not recognized.");
        } else {
            errorLabel_mappingAlignment.setToolTipText("The alignment format has been detected as: " + ReadseqTools.getFormatName(new File(mappingFileField.getText())) + ".");
            errorLabel_mappingAlignment.setIcon(correctIcon);
        }

        // field name
        errorLabel_fieldName.setToolTipText("");
        errorLabel_fieldName.setIcon(errorIcon);
        if (nameField.getText().equals("")) {
            errorLabel_fieldName.setToolTipText("Please enter a name.");
        } else if (dataModel.isFieldNameUsed(nameField.getText(), dataOverlay)) {
            errorLabel_fieldName.setToolTipText("A data overlay with this name already exists in this project. Please use a different name.");
        } else {
            errorLabel_fieldName.setIcon(null);
        }
    }

    public void updateMinMaxField() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int j = 0; j < dataOverlay.dataSources2D.size(); j++) {
            File dataFile = dataOverlay.dataSources2D.get(j).dataFile;
            if (dataFile.exists()) {
                if (dataOverlay.dataSources2D.get(j).dataFileType == 0) // StructureVis SparseMatrix
                {
                    if (!minDataTable.containsKey(dataOverlay.dataSources2D.get(j))) {
                        double[] minAndMax = SparseMatrix.getMinAndMaxFromFile(dataFile);
                        min = Math.min(minAndMax[0], min);
                        max = Math.max(minAndMax[1], max);
                        minDataTable.put(dataOverlay.dataSources2D.get(j), minAndMax[0]);
                        maxDataTable.put(dataOverlay.dataSources2D.get(j), minAndMax[1]);
                    } else {
                        min = Math.min(minDataTable.get(dataOverlay.dataSources2D.get(j)), min);
                        max = Math.max(maxDataTable.get(dataOverlay.dataSources2D.get(j)), max);
                    }
                } else if (dataOverlay.dataSources2D.get(j).dataFileType == 1) // Pairwise
                {
                    if (!minDataTable.containsKey(dataOverlay.dataSources2D.get(j))) {
                        double[] minAndMax = SparseMatrix.loadSparseMatrixFromPairwiseFile(dataFile, "\t").getMinAndMax();
                        min = Math.min(minAndMax[0], min);
                        max = Math.max(minAndMax[1], max);
                        minDataTable.put(dataOverlay.dataSources2D.get(j), minAndMax[0]);
                        maxDataTable.put(dataOverlay.dataSources2D.get(j), minAndMax[1]);
                    } else {
                        min = Math.min(minDataTable.get(dataOverlay.dataSources2D.get(j)), min);
                        max = Math.max(maxDataTable.get(dataOverlay.dataSources2D.get(j)), max);
                    }
                }
            }
        }

        if (min == Double.MAX_VALUE && max == Double.MIN_VALUE) {
            min = 0;
            max = 1;
        }

        if (dataOverlay.useMin) {
            minField.setText(min + "");
        }
        if (dataOverlay.useMax) {
            maxField.setText(max + "");
        }


        /*
         * double min = Double.MAX_VALUE; double max = Double.MIN_VALUE;
         *
         * for (int j = 0; j < dataOverlay.dataSources1D.size(); j++) { File
         * dataFile = dataOverlay.dataSources1D.get(j).dataFile; if
         * (dataFile.exists()) { if (dataOverlay.dataSources1D.get(j).dataColumn
         * >= 0) { ArrayList<String> data = IO.getColumnFromCSV(dataFile,
         * dataOverlay.dataSources1D.get(j).dataColumn,
         * hasHeaderCheckbox.isSelected()); for (int i = 0; i < data.size();
         * i++) { try { if (data.get(i) != null) { double val =
         * Double.parseDouble(data.get(i)); min = Math.min(min, val); max =
         * Math.max(max, val); } } catch (NumberFormatException ex) { } } } } }
         *
         * if (dataMinComboBox.getSelectedIndex() == 0) { minField.setText(min +
         * ""); } if (dataMaxComboBox.getSelectedIndex() == 0) {
         * maxField.setText(max + "");
        }
         */
    }

    public void updateDataLegend() {
        double min = Double.parseDouble(minField.getText());
        double max = Double.parseDouble(maxField.getText());
        TransformType type = TransformType.IDENTITY;
        switch (this.transformTypeComboBox.getSelectedIndex()) {
            case 0:
                type = TransformType.LINEAR;
                break;
            case 1:
                type = TransformType.EXPLOG;
                break;
        }
        DataTransform dt = new DataTransform(min, max, type);

        if (dataLegend1.colorGradient != null) {
            dataOverlay.colorGradient = dataLegend1.colorGradient;
        }
        dataLegend1.initialise(nameField.getText(), dt, dataOverlay.colorGradient, dataOverlay.colorGradient);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField3 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        dataBrowseButton = new javax.swing.JButton();
        mappingBrowseButton = new javax.swing.JButton();
        mappingFileField = new javax.swing.JTextField();
        dataFileField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        dataTypeComboBox = new javax.swing.JComboBox();
        codonPositionsCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        previousButton = new javax.swing.JButton();
        currentField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        upperField = new javax.swing.JTextField();
        nextButton = new javax.swing.JButton();
        addNewButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        errorLabel_dataFile = new javax.swing.JLabel();
        errorLabel_mappingAlignment = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        transformTypeComboBox = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        minField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        dataMinComboBox = new javax.swing.JComboBox();
        dataMaxComboBox = new javax.swing.JComboBox();
        maxField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        errorLabel_fieldName = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        addButton = new javax.swing.JButton();

        jTextField3.setText("jTextField3");

        jLabel8.setText("jLabel8");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(700, 550));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Data sources"));

        dataBrowseButton.setText("Browse...");
        dataBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataBrowseButtonActionPerformed(evt);
            }
        });

        mappingBrowseButton.setText("Browse...");
        mappingBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mappingBrowseButtonActionPerformed(evt);
            }
        });

        mappingFileField.setEditable(false);
        mappingFileField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mappingFileFieldActionPerformed(evt);
            }
        });

        dataFileField.setEditable(false);
        dataFileField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataFileFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Data file");

        jLabel10.setText("Mapping alignment");

        jLabel3.setText("Data file type");

        dataTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "StructureVis Matrix", "Pairwise Matrix" }));
        dataTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeComboBoxActionPerformed(evt);
            }
        });

        codonPositionsCheckBox.setText("Codon positions?");
        codonPositionsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codonPositionsCheckBoxActionPerformed(evt);
            }
        });

        previousButton.setText("<");
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });
        jPanel3.add(previousButton);

        currentField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        currentField.setText("1");
        currentField.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel3.add(currentField);

        jLabel12.setText(" of ");
        jPanel3.add(jLabel12);

        upperField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        upperField.setText("1");
        upperField.setPreferredSize(new java.awt.Dimension(40, 20));
        upperField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upperFieldActionPerformed(evt);
            }
        });
        jPanel3.add(upperField);

        nextButton.setText(">");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        jPanel3.add(nextButton);

        addNewButton.setText("Add new");
        addNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewButtonActionPerformed(evt);
            }
        });
        jPanel3.add(addNewButton);

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        jPanel3.add(deleteButton);

        errorLabel_dataFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/structurevis/resources/error.png"))); // NOI18N
        errorLabel_dataFile.setText(" ");

        errorLabel_mappingAlignment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/structurevis/resources/error.png"))); // NOI18N
        errorLabel_mappingAlignment.setText(" ");

        jTextPane1.setEditable(false);
        jTextPane1.setText("The \"Add new\" allows you to combine multiple data sources into the same overlay. This is useful when you have related data sources corresponding to non-overlapping parts of the genome, e.g. genes. Non-empty values in the last data source take precedence (i.e. overwrite) over values in the previous data source(s).");
        jScrollPane3.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel10)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(dataTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(codonPositionsCheckBox))
                            .addComponent(mappingFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                            .addComponent(dataFileField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(errorLabel_dataFile)
                            .addComponent(errorLabel_mappingAlignment))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mappingBrowseButton)
                            .addComponent(dataBrowseButton)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(dataFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorLabel_dataFile)
                    .addComponent(dataBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(mappingFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(errorLabel_mappingAlignment))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(dataTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codonPositionsCheckBox)))
                    .addComponent(mappingBrowseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Data display"));

        jLabel1.setText("Field name");

        transformTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Linear", "Exponential logarithm (p-values)", "None" }));
        transformTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transformTypeComboBoxActionPerformed(evt);
            }
        });

        jLabel6.setText("Data transform");

        minField.setText("0.0");
        minField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Data minimum");

        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        dataMinComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Use minimum value", "Custom" }));
        dataMinComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataMinComboBoxActionPerformed(evt);
            }
        });

        dataMaxComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Use maximum value", "Custom" }));
        dataMaxComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataMaxComboBoxActionPerformed(evt);
            }
        });

        maxField.setText("1.0");

        jLabel9.setText("Data maximum");

        errorLabel_fieldName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/structurevis/resources/error.png"))); // NOI18N
        errorLabel_fieldName.setText(" ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(dataMaxComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dataMinComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                    .addComponent(transformTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minField, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                            .addComponent(maxField, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(errorLabel_fieldName)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorLabel_fieldName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(transformTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataMinComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(minField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dataMaxComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel9))))
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Data legend preview"));

        addButton.setText("Ok");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dataTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataTypeComboBoxActionPerformed

        currentItem.dataFileType = dataTypeComboBox.getSelectedIndex();
        if (currentItem.dataFileType == 0) {
        } else {
        }
    }//GEN-LAST:event_dataTypeComboBoxActionPerformed

    private void dataBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataBrowseButtonActionPerformed
        int returnVal = Wizard.fileChooserOpen.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            dataFileField.setText(Wizard.fileChooserOpen.getSelectedFile().getPath());
            currentItem.dataFile = new File(Wizard.fileChooserOpen.getSelectedFile().getPath());
            if (currentItem.dataFile.exists()) {
                displayDataSource(currentIndex);
            }
        }
        updateMinMaxField();
        updateToolTips();
    }//GEN-LAST:event_dataBrowseButtonActionPerformed

    private void mappingBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mappingBrowseButtonActionPerformed
        int returnVal = Wizard.fileChooserOpen.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            mappingFileField.setText(Wizard.fileChooserOpen.getSelectedFile().getPath());
            currentItem.mappingFile = new File(Wizard.fileChooserOpen.getSelectedFile().getPath());
        }
        updateToolTips();
    }//GEN-LAST:event_mappingBrowseButtonActionPerformed

    private void dataMinComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataMinComboBoxActionPerformed
        if (dataMinComboBox.getSelectedIndex() == 0) {
            dataOverlay.useMin = true;
        } else {
            dataOverlay.useMin = false;
        }
        updateMinMaxField();
        updateTextFields();
    }//GEN-LAST:event_dataMinComboBoxActionPerformed

    private void dataMaxComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataMaxComboBoxActionPerformed
        if (dataMaxComboBox.getSelectedIndex() == 0) {
            dataOverlay.useMax = true;
        } else {
            dataOverlay.useMax = false;
        }
        updateMinMaxField();
        updateTextFields();
    }//GEN-LAST:event_dataMaxComboBoxActionPerformed

    private void transformTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transformTypeComboBoxActionPerformed
        switch (transformTypeComboBox.getSelectedIndex()) {
            case 0:
                dataOverlay.transformType = DataTransform.TransformType.LINEAR;
                break;
            case 1:
                dataOverlay.transformType = DataTransform.TransformType.EXPLOG;
                break;
            case 2:
                dataOverlay.transformType = DataTransform.TransformType.IDENTITY;
                break;
        }
        updateDataLegend();
    }//GEN-LAST:event_transformTypeComboBoxActionPerformed

    private void addNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewButtonActionPerformed
        addNewDataSource();
        /*
         * updateComboBoxes(); updateFields(); updateDataLegend();
        updateDisplayToCurrent();
         */
        displayDataSource(currentIndex);
    }//GEN-LAST:event_addNewButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        displayDataSource(Math.min(currentIndex + 1, dataOverlay.dataSources2D.size() - 1));
    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        displayDataSource(Math.max(currentIndex - 1, 0));
    }//GEN-LAST:event_previousButtonActionPerformed

    public void displayDataSource(int index) {
        currentItem = dataOverlay.dataSources2D.get(index);
        currentIndex = index;

        this.codonPositionsCheckBox.setSelected(currentItem.codonPositions);

        // update data fields
        this.dataFileField.setText(currentItem.dataFile.getPath());
        this.mappingFileField.setText(currentItem.mappingFile.getPath());
        this.dataTypeComboBox.setSelectedIndex(currentItem.dataFileType);
        this.currentField.setText((currentIndex + 1) + "");
        this.upperField.setText(dataOverlay.dataSources2D.size() + "");

        updateTextFields();
        updateDataLegend();
    }

    public void updateTextFields() {

        // update fields
        if (dataMinComboBox.getSelectedIndex() == 0) {
            minField.setEditable(false);
        } else {
            minField.setEditable(true);
        }
        if (dataMaxComboBox.getSelectedIndex() == 0) {
            maxField.setEditable(false);
        } else {
            maxField.setEditable(true);
        }

    }

    private void upperFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upperFieldActionPerformed
    }//GEN-LAST:event_upperFieldActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed

        if (dataOverlay.dataSources2D.size() > 1) {
            dataOverlay.dataSources2D.remove(currentIndex);
            displayDataSource(Math.min(Math.max(currentIndex, 0), dataOverlay.dataSources2D.size() - 1));
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void codonPositionsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codonPositionsCheckBoxActionPerformed
        currentItem.codonPositions = codonPositionsCheckBox.isSelected();
    }//GEN-LAST:event_codonPositionsCheckBoxActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
//        this.returnVal = ExitValue.EXIT_AND_ADD;
        if (openIndex == -1) {
            dataModel.overlays.add(dataOverlay);
        }
        dataOverlay.fieldName = this.nameField.getText();
        dataOverlay.type = "SequenceData2D";
        dataOverlay.colorGradient = dataLegend1.colorGradient;
        dataOverlay.minValue = Double.parseDouble(minField.getText());
        dataOverlay.maxValue = Double.parseDouble(maxField.getText());
        this.dispose();
    }//GEN-LAST:event_addButtonActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameFieldActionPerformed

    private void dataFileFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataFileFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dataFileFieldActionPerformed

    private void mappingFileFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mappingFileFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mappingFileFieldActionPerformed

    private void minFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_minFieldActionPerformed

    public void addNewDataSource() {
        currentItem = new DataSource2D();
        currentIndex = dataOverlay.dataSources2D.size();
        dataOverlay.dataSources2D.add(currentItem);
    }

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

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                SequenceDialog2D dialog = new SequenceDialog2D(new javax.swing.JFrame(), true, new DataModel(), -1);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addNewButton;
    private javax.swing.JCheckBox codonPositionsCheckBox;
    private javax.swing.JTextField currentField;
    private javax.swing.JButton dataBrowseButton;
    private javax.swing.JTextField dataFileField;
    private javax.swing.JComboBox dataMaxComboBox;
    private javax.swing.JComboBox dataMinComboBox;
    private javax.swing.JComboBox dataTypeComboBox;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel errorLabel_dataFile;
    private javax.swing.JLabel errorLabel_fieldName;
    private javax.swing.JLabel errorLabel_mappingAlignment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JButton mappingBrowseButton;
    private javax.swing.JTextField mappingFileField;
    private javax.swing.JTextField maxField;
    private javax.swing.JTextField minField;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JComboBox transformTypeComboBox;
    private javax.swing.JTextField upperField;
    // End of variables declaration//GEN-END:variables

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if (e.getSource().equals(nameField)) {
            updateDataLegend();
            if (nameField.getInputVerifier().verify(nameField)) {
            }
        } else if (e.getSource().equals(minField)) {
            if (minField.getInputVerifier().verify(minField)) {
                updateDataLegend();
            }
        } else if (e.getSource().equals(maxField)) {
            if (maxField.getInputVerifier().verify(maxField)) {
                updateDataLegend();
            }
        }
        updateToolTips();
    }
}
