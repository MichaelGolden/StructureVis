/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.datacreation;

import java.io.File;
import javax.swing.JOptionPane;
import structurevis.ui.datacreation.wizard.Wizard;
import structurevis.ui.datacreation.wizard.WizardPanelDescriptor;

/**
 *
 * @author Michael Golden
 */
/**
 *
 * @author Michael Golden
 */
public class FinishDescriptor extends WizardPanelDescriptor implements DataModelListener {

    public static final String IDENTIFIER = "FINISH_PANEL";
    public FinishPanel panel;
    DataModel dataModel;

    public FinishDescriptor(DataModel dataModel) {

        this.dataModel = dataModel;
        panel = new FinishPanel(dataModel);
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel);
        dataModel.addDataModelListener(this);
    }

    public Object getNextPanelDescriptor() {
        return FINISH;
    }

    public Object getBackPanelDescriptor() {
        return DataDescriptor.IDENTIFIER;
    }

    public void aboutToDisplayPanel() {
    }
    boolean success = true;

    public void displayingPanel() {
        getWizard().setBackButtonEnabled(false);
        getWizard().setNextFinishButtonEnabled(false);
        Thread t = new Thread() {

            public void run() {
                try {
                    panel.jTextArea1.setText("");
                    panel.jTextArea1.insert("StructureVis is creating your dataset, this may take a few minutes.", panel.jTextArea1.getText().length());
                    //panel.jTextArea1.setText(text);
                    panel.dataModel.saveProject();
                    panel.jProgressBar1.setValue(100);
                    panel.jTextArea1.insert("\nCompleted successfully.", panel.jTextArea1.getText().length());
                    if (WizardMain.mainapp != null) {
                        Object[] options = {"Yes", "No"};
                        int n = JOptionPane.showOptionDialog(panel,
                                "Do you wish to open this dataset now? Note: your current dataset will be closed.",
                                "Open dataset?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[1]);
                        if (n == 0) {
                            getWizard().close(Wizard.FINISH_RETURN_CODE);
                            WizardMain.mainapp.openStructureCollectionFromFolder(panel.dataModel.projectDirectory);

                        }
                    }
                    getWizard().setNextFinishButtonEnabled(true);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    panel.jProgressBar1.setValue(0);
                    Throwable throwable = ex.getCause();
                    if (throwable == null) {
                        String traceString = "";
                        StackTraceElement[] traceElem = ex.getStackTrace();
                        for (int i = 0; i < traceElem.length; i++) {
                            traceString += traceElem[i].toString() + "\n";
                        }
                        panel.jTextArea1.insert("\n\nAn unknown error has occurred, please go back and review the settings you have used.\nError thrown: " + ex.toString() + "\nDetails:\n" + traceString, panel.jTextArea1.getText().length());
                    } else {
                        panel.jTextArea1.insert("\nThe following error has occurred:\n" + ex.getCause().toString(), panel.jTextArea1.getText().length());
                    }
                    success = false;
                    
                    System.out.println("An error occured in FinishDescriptor.java");
                    /*
                    // delete project directory
                    dataModel.projectDirectory.deleteOnExit();
                    File[] files = dataModel.projectDirectory.listFiles();
                    for (File f : files) {
                        f.deleteOnExit();
                    }*/
                    

                    getWizard().setBackButtonEnabled(true);
                }

            }
        };
        t.start();
    }

    public void aboutToHidePanel() {
    }

    public void referenceAlignmentChanged(File oldReferenceAlignment, File newReferenceAlignment) {
    }

    public void dataSourceProcessed(int n, int total, Object dataSource) {
        int perc = (int) ((double) n / (double) Math.max(total, 1) * 100);
        panel.jTextArea1.insert("\nData source " + n + " of " + total + " processed.", panel.jTextArea1.getText().length());
        panel.jProgressBar1.setValue(perc);
    }
}
