/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StructurePanel.java
 *
 * Created on 03 Dec 2011, 6:06:17 PM
 */
package structurevis.ui.datacreation;

import java.io.File;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import structurevis.structures.StructureParser;
import structurevis.structures.io.ReadseqTools;
import structurevis.ui.datacreation.DataModel.GenomeStructureFileType;
import structurevis.ui.datacreation.DataModel.SubstructuresStructureFileType;
import structurevis.ui.datacreation.wizard.Wizard;
import structurevis.ui.datacreation.wizard.WizardListener;

/**
 *
 * @author Michael Golden
 */
public class StructurePanel extends javax.swing.JPanel implements WizardListener {

    ImageIcon errorIcon = new ImageIcon(getClass().getResource("/structurevis/resources/error.png"));
    ImageIcon correctIcon = new ImageIcon(getClass().getResource("/structurevis/resources/correct.png"));
    DataModel dataModel;
    boolean errors = true;

    /**
     * Creates new form StructurePanel
     */
    public StructurePanel(DataModel dataModel) {
        this.dataModel = dataModel;
        initComponents();

        try {

            PropertyResourceBundle resources = (PropertyResourceBundle) ResourceBundle.getBundle("structurevis.ui.datacreation.text");
            genomeStructureDescriptionPane.setText(resources.getString("GENOME_STRUCTURE_DESCRIPTION"));
            substructureDescriptionPane.setText(resources.getString("SUBSTRUCTURE_DESCRIPTION"));
            genomeStructureDescriptionPane.setCaretPosition(0);
            substructureDescriptionPane.setCaretPosition(0);

        } catch (MissingResourceException mre) {
            // resource missing
        }

        minSpinner.setModel(new SpinnerNumberModel(10, 0, Integer.MAX_VALUE, 1));
        maxSpinner.setModel(new SpinnerNumberModel(250, 2, Integer.MAX_VALUE, 1));

        WizardMain.wizard.addWizardListener(this);
        updateErrors();
    }

    public int getBestConsensusStructureFileType(File file) {
        try {
            StructureParser.parseCtFile(file);
            return 0;
        } catch (Exception ex) {
        }

        try {
            StructureParser.parseNaspCtFile(file);
            return 1;
        } catch (Exception ex) {
        }

        try {
            if (StructureParser.isDotBracketString(StructureParser.getDotBracketStringFromFile(file))) {
                return 2;
            }
        } catch (Exception ex) {
        }

        try {
            StructureParser.parseTabDelimittedHelixFile(file, 0);
            return 3;
        } catch (Exception ex) {
        }

        return -1;
    }

    public String getConsensusStructureFileTypeError(File file) {

        String error = null;
        if (consensusFileType.getSelectedIndex() == 0) // Standard CT file
        {
            try {
                StructureParser.parseCtFile(file);
            } catch (Exception ex) {
                error = "File does not appear to be a standard connect file.";
            }
        } else if (consensusFileType.getSelectedIndex() == 1) // NASP CT file
        {
            try {
                StructureParser.parseCtFile(file);
            } catch (Exception ex) {
                error = "File does not appear to be a NASP connect file.";
            }
        } else if (consensusFileType.getSelectedIndex() == 2) // Dot bracket
        {
            try {
                String dotBracketString = StructureParser.getDotBracketStringFromFile(file);
                if (!StructureParser.isDotBracketString(dotBracketString)) {
                    error = "File does not appear to be a dot bracket string file.";
                }
            } catch (Exception ex) {
                error = "File does not appear to be a dot bracket string file.";
            }
        } else if (consensusFileType.getSelectedIndex() == 3) // Tab delimitted helix
        {
            try {
                StructureParser.parseTabDelimittedHelixFile(file, 0);
            } catch (Exception ex) {
                error = "File does not appear to be a tab-delimitted helix file.";
            }
        }
        if (error != null) {
            int bestFileType = getBestConsensusStructureFileType(file);
            switch (bestFileType) {
                case -1:
                    return "File cannot be read using any of the supported formats. Please select a different file.";
                case 0:
                    return error + " Try using 'Standard connect file'.";
                case 1:
                    return error + " Try using 'NASP connect file'.";
                case 2:
                    return error + " Try using 'Dot bracket file'.";
                case 3:
                    return error + " Try using 'Tab delimmited helix file'.";
            }
        }
        return error; // no error
    }
    
    /*public String getSubstructuresFromFileError (File file)
    {
        
        String error = null;
        if (structureTypeBox1.getSelectedIndex() == 0) // Standard CT file
        {
            try {
                StructureParser.parseNaspFiles(file);
            } catch (Exception ex) {
                error = "File does not appear to be a standard connect file.";
            }
        } else if (structureTypeBox1.getSelectedIndex() == 1) // NASP CT file
        {
            try {
                StructureParser.parseCtFile(file);
            } catch (Exception ex) {
                error = "File does not appear to be a NASP connect file.";
            }
        }
        return error; // no error
    }*/

    public void updateErrors() {
        WizardMain.wizard.showErrorsBeforeContinue(false);
        errors = false;

        // consensus structure
        errorLabel_consensusStructure.setIcon(errorIcon);
        errorLabel_consensusStructure.setToolTipText("");
        //File consensusFile = new File(consensusStructureField.getText());        
        String consensusErrorMessage = null;
        if (consensusStructureField.getText().equals("")) {
            errorLabel_consensusStructure.setToolTipText("Please select a file.");
            WizardMain.wizard.showErrorsBeforeContinue(true);
            errors = true;
        } else if (!dataModel.genomeStructureFile.exists()) {
            errorLabel_consensusStructure.setToolTipText("The specified file does not exist.");
            WizardMain.wizard.showErrorsBeforeContinue(true);
            errors = true;
        } else if ((consensusErrorMessage = getConsensusStructureFileTypeError(dataModel.genomeStructureFile)) != null) // Standard CT file
        {
            errorLabel_consensusStructure.setToolTipText(consensusErrorMessage);
            WizardMain.wizard.showErrorsBeforeContinue(true);
            errors = true;
        } else {
            errorLabel_consensusStructure.setIcon(correctIcon);
        }

        // reference alignment
        this.errorLabelReferenceAlignment.setIcon(errorIcon);
        errorLabelReferenceAlignment.setIcon(errorIcon);
        errorLabelReferenceAlignment.setToolTipText("");
        if (referenceTextField.getText().equals("")) {
            errorLabelReferenceAlignment.setToolTipText("Please select a file.");
            WizardMain.wizard.showErrorsBeforeContinue(true);
            errors = true;
        } else if (!dataModel.getReferenceAlignment().exists()) {
            errorLabelReferenceAlignment.setToolTipText("The specified file does not exist.");
            WizardMain.wizard.showErrorsBeforeContinue(true);
            errors = true;
        } else if (!ReadseqTools.isKnownFormat(dataModel.getReferenceAlignment())) {
            errorLabelReferenceAlignment.setToolTipText("The alignment format is not recognized.");
            WizardMain.wizard.showErrorsBeforeContinue(true);
            errors = true;
        } else {
            errorLabelReferenceAlignment.setToolTipText("The alignment format has been detected as: " + ReadseqTools.getFormatName(dataModel.getReferenceAlignment()) + ".");
            errorLabelReferenceAlignment.setIcon(correctIcon);
        }

        // from file
        errorLabelFromFile.setToolTipText("");
        if (fromFileRadioButton.isSelected()) {
            errorLabelFromFile.setIcon(errorIcon);
            if (fromFileField.getText().equals("")) {
                errorLabelFromFile.setToolTipText("Please select a file.");
                WizardMain.wizard.showErrorsBeforeContinue(true);
                errors = true;
            } else if (!new File(referenceTextField.getText()).exists()) {
                errorLabelFromFile.setToolTipText("The specified file does not exist.");
                WizardMain.wizard.showErrorsBeforeContinue(true);
                errors = true;
            }
            else {
                errorLabelFromFile.setIcon(correctIcon);
            }
        } else {
            this.errorLabelFromFile.setIcon(null);
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

        nucleicAcidButtonGroup = new javax.swing.ButtonGroup();
        conformationButtonGroup = new javax.swing.ButtonGroup();
        subtructuresButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        consensusStructureField = new javax.swing.JTextField();
        consensusBrowseButton = new javax.swing.JButton();
        consensusFileType = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        genomeStructureDescriptionPane = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        referenceBrowseButton = new javax.swing.JButton();
        referenceTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        linearRadioButton = new javax.swing.JRadioButton();
        circularRadioButton = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        rnaRadioButton = new javax.swing.JRadioButton();
        dnaRadioButton = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        errorLabel_consensusStructure = new javax.swing.JLabel();
        errorLabelReferenceAlignment = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        fromFileField = new javax.swing.JTextField();
        fromFileBrowseButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        substructureDescriptionPane = new javax.swing.JTextPane();
        fromFileRadioButton = new javax.swing.JRadioButton();
        autoRadioButton = new javax.swing.JRadioButton();
        minSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        maxSpinner = new javax.swing.JSpinner();
        structureTypeBox1 = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        errorLabelFromFile = new javax.swing.JLabel();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Secondary structure"));
        jPanel1.setPreferredSize(new java.awt.Dimension(912, 255));

        consensusStructureField.setEditable(false);
        consensusStructureField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consensusStructureFieldActionPerformed(evt);
            }
        });

        consensusBrowseButton.setText("Browse...");
        consensusBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consensusBrowseButtonActionPerformed(evt);
            }
        });

        consensusFileType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Standard connect file", "NASP connect file", "Dot bracket file", "Tab delimitted helix file" }));
        consensusFileType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consensusFileTypeActionPerformed(evt);
            }
        });

        jLabel1.setText("Description");

        genomeStructureDescriptionPane.setContentType("text/html");
        genomeStructureDescriptionPane.setEditable(false);
        jScrollPane2.setViewportView(genomeStructureDescriptionPane);

        jLabel2.setText("Type:");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("Structure");

        jLabel9.setText("Reference alignment");

        referenceBrowseButton.setText("Browse...");
        referenceBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                referenceBrowseButtonActionPerformed(evt);
            }
        });

        referenceTextField.setEditable(false);
        referenceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                referenceTextFieldActionPerformed(evt);
            }
        });

        conformationButtonGroup.add(linearRadioButton);
        linearRadioButton.setSelected(true);
        linearRadioButton.setText("Linear");
        linearRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                linearRadioButtonStateChanged(evt);
            }
        });

        conformationButtonGroup.add(circularRadioButton);
        circularRadioButton.setText("Circular");

        jLabel7.setText("Conformation");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(linearRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(circularRadioButton)
                .addContainerGap(102, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(linearRadioButton)
                .addComponent(circularRadioButton))
        );

        nucleicAcidButtonGroup.add(rnaRadioButton);
        rnaRadioButton.setSelected(true);
        rnaRadioButton.setText("RNA");
        rnaRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rnaRadioButtonStateChanged(evt);
            }
        });

        nucleicAcidButtonGroup.add(dnaRadioButton);
        dnaRadioButton.setText("DNA");

        jLabel10.setText("Nucleic acid");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(rnaRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dnaRadioButton)
                .addContainerGap(86, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(rnaRadioButton)
                .addComponent(dnaRadioButton))
        );

        errorLabel_consensusStructure.setIcon(new javax.swing.ImageIcon(getClass().getResource("/structurevis/resources/error.png"))); // NOI18N
        errorLabel_consensusStructure.setText(" ");

        errorLabelReferenceAlignment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/structurevis/resources/error.png"))); // NOI18N
        errorLabelReferenceAlignment.setText(" ");

        jButton1.setText("Auto-detect");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(consensusStructureField, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(errorLabel_consensusStructure)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(consensusBrowseButton)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(consensusFileType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(10, 10, 10)
                                .addComponent(referenceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(errorLabelReferenceAlignment)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(referenceBrowseButton))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(consensusStructureField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consensusFileType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jButton1)
                    .addComponent(consensusBrowseButton)
                    .addComponent(errorLabel_consensusStructure))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(referenceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(referenceBrowseButton)
                    .addComponent(errorLabelReferenceAlignment))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel1))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Substructures"));

        fromFileField.setEditable(false);

        fromFileBrowseButton.setText("Browse...");
        fromFileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromFileBrowseButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Description");

        substructureDescriptionPane.setContentType("text/html");
        substructureDescriptionPane.setEditable(false);
        jScrollPane3.setViewportView(substructureDescriptionPane);

        subtructuresButtonGroup.add(fromFileRadioButton);
        fromFileRadioButton.setText("From file");
        fromFileRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fromFileRadioButtonStateChanged(evt);
            }
        });

        subtructuresButtonGroup.add(autoRadioButton);
        autoRadioButton.setSelected(true);
        autoRadioButton.setText("Auto-generate from consensus structure");

        minSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minSpinnerStateChanged(evt);
            }
        });

        jLabel4.setText("Min. substructure size (nucleotides)");

        jLabel5.setText("Max. substructure size (nucleotides)");

        maxSpinner.setVerifyInputWhenFocusTarget(false);
        maxSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxSpinnerStateChanged(evt);
            }
        });

        structureTypeBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NASP structures file", "Existing structure collection" }));
        structureTypeBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                structureTypeBox1ActionPerformed(evt);
            }
        });

        jLabel6.setText("Type:");

        errorLabelFromFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/structurevis/resources/error.png"))); // NOI18N
        errorLabelFromFile.setText(" ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(fromFileRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(fromFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(errorLabelFromFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fromFileBrowseButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(autoRadioButton)
                                .addGap(32, 32, 32)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(maxSpinner)
                                    .addComponent(minSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addGap(10, 10, 10)
                        .addComponent(structureTypeBox1, 0, 124, Short.MAX_VALUE))
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(autoRadioButton)
                            .addComponent(jLabel4)
                            .addComponent(minSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(maxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(62, 62, 62))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fromFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fromFileBrowseButton)
                            .addComponent(structureTypeBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(fromFileRadioButton)
                            .addComponent(errorLabelFromFile))
                        .addGap(18, 18, 18)))
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 743, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void consensusFileTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consensusFileTypeActionPerformed
        switch (consensusFileType.getSelectedIndex()) {
            case 0:
                dataModel.genomeStructureFileType = GenomeStructureFileType.STANDARD_CT_FILE;
                break;
            case 1:
                dataModel.genomeStructureFileType = GenomeStructureFileType.NASP_CT_FILE;
                break;
            case 2:
                dataModel.genomeStructureFileType = GenomeStructureFileType.DOT_BRACKET_FILE;
                break;
            case 3:
                dataModel.genomeStructureFileType = GenomeStructureFileType.TAB_DELIMITTED_HELIX;
                break;
        }
        updateErrors();
    }//GEN-LAST:event_consensusFileTypeActionPerformed

    private void structureTypeBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_structureTypeBox1ActionPerformed
        switch (structureTypeBox1.getSelectedIndex()) {
            case 0:
                dataModel.substructuresStructureFileType = SubstructuresStructureFileType.NASP_FILE_TYPE;
                break;
            case 1:
                dataModel.substructuresStructureFileType = SubstructuresStructureFileType.EXISTING_COLLECTION;
                break;
        }
        updateErrors();
    }//GEN-LAST:event_structureTypeBox1ActionPerformed

    private void consensusBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consensusBrowseButtonActionPerformed
        int returnVal = Wizard.fileChooserOpen.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            consensusStructureField.setText(Wizard.fileChooserOpen.getSelectedFile().getPath());
            dataModel.genomeStructureFile = Wizard.fileChooserOpen.getSelectedFile();
        }
        updateErrors();
    }//GEN-LAST:event_consensusBrowseButtonActionPerformed

    private void referenceBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_referenceBrowseButtonActionPerformed
        int returnVal = Wizard.fileChooserOpen.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.referenceTextField.setText(Wizard.fileChooserOpen.getSelectedFile().getPath());
            dataModel.setReferenceAlignment(Wizard.fileChooserOpen.getSelectedFile());
        }
        updateErrors();
    }//GEN-LAST:event_referenceBrowseButtonActionPerformed

    private void referenceTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_referenceTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_referenceTextFieldActionPerformed

    private void rnaRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rnaRadioButtonStateChanged
        dataModel.nucleicAcidRNA = rnaRadioButton.isSelected();
    }//GEN-LAST:event_rnaRadioButtonStateChanged

    private void linearRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_linearRadioButtonStateChanged
        dataModel.conformationCircular = !linearRadioButton.isSelected();
    }//GEN-LAST:event_linearRadioButtonStateChanged

    private void fromFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromFileBrowseButtonActionPerformed
        int returnVal = Wizard.fileChooserOpen.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fromFileField.setText(Wizard.fileChooserOpen.getSelectedFile().getPath());
            dataModel.substructureFile = Wizard.fileChooserOpen.getSelectedFile();
        }
        updateErrors();
    }//GEN-LAST:event_fromFileBrowseButtonActionPerformed

    private void consensusStructureFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consensusStructureFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_consensusStructureFieldActionPerformed

    private void fromFileRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fromFileRadioButtonStateChanged
        if (!fromFileRadioButton.isSelected()) {
            dataModel.substructureFile = null;
        } else {
            dataModel.substructureFile = new File(fromFileField.getText());
        }
        updateErrors();
    }//GEN-LAST:event_fromFileRadioButtonStateChanged

    private void minSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minSpinnerStateChanged

        dataModel.minSubstructureSize = (Integer) minSpinner.getValue();
        if ((Integer) maxSpinner.getValue() < (Integer) minSpinner.getValue()) {
            minSpinner.setValue(maxSpinner.getValue());
        }
    }//GEN-LAST:event_minSpinnerStateChanged

    private void maxSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxSpinnerStateChanged
        dataModel.maxSubstructureSize = (Integer) maxSpinner.getValue();
        if ((Integer) maxSpinner.getValue() < (Integer) minSpinner.getValue()) {
            maxSpinner.setValue(minSpinner.getValue());
        }
    }//GEN-LAST:event_maxSpinnerStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        File consensusFile = new File(consensusStructureField.getText());
        if (consensusFile.exists()) {
            int index = getBestConsensusStructureFileType(consensusFile);
            if (index >= 0) {
                consensusFileType.setSelectedIndex(index);
            } else {
                JOptionPane.showMessageDialog(this, "No suitable file type could be detected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoRadioButton;
    private javax.swing.JRadioButton circularRadioButton;
    private javax.swing.ButtonGroup conformationButtonGroup;
    private javax.swing.JButton consensusBrowseButton;
    private javax.swing.JComboBox consensusFileType;
    private javax.swing.JTextField consensusStructureField;
    private javax.swing.JRadioButton dnaRadioButton;
    private javax.swing.JLabel errorLabelFromFile;
    private javax.swing.JLabel errorLabelReferenceAlignment;
    private javax.swing.JLabel errorLabel_consensusStructure;
    private javax.swing.JButton fromFileBrowseButton;
    private javax.swing.JTextField fromFileField;
    private javax.swing.JRadioButton fromFileRadioButton;
    private javax.swing.JTextPane genomeStructureDescriptionPane;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JRadioButton linearRadioButton;
    private javax.swing.JSpinner maxSpinner;
    private javax.swing.JSpinner minSpinner;
    private javax.swing.ButtonGroup nucleicAcidButtonGroup;
    private javax.swing.JButton referenceBrowseButton;
    private javax.swing.JTextField referenceTextField;
    private javax.swing.JRadioButton rnaRadioButton;
    private javax.swing.JComboBox structureTypeBox1;
    private javax.swing.JTextPane substructureDescriptionPane;
    private javax.swing.ButtonGroup subtructuresButtonGroup;
    // End of variables declaration//GEN-END:variables

    DataOverlay dataOverlay = null;
    public void panelChangedEvent(Object fromPanelIdentifier, Object toPanelIdentifier) {
        if (fromPanelIdentifier.equals(StructureDescriptor.IDENTIFIER) && toPanelIdentifier.equals(GenomeOrganizationDescriptor.IDENTIFIER)) {
            if (errors) {
                Object[] options = {"Yes (recommended)", "No"};
                int n = JOptionPane.showOptionDialog(this,
                        "There are error(s) on the previous panel that will prevent the dataset creation wizard from completing successfully. Review settings now?",
                        "Critical error(s)",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);
                if (n == 0) {
                    WizardMain.wizard.setCurrentPanel(StructureDescriptor.IDENTIFIER);
                }
            }          
        }
    }
}
