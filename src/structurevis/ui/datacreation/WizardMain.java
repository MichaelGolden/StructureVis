package structurevis.ui.datacreation;

import java.awt.Cursor;
import java.awt.Image;
import javax.swing.*;
import structurevis.ui.MainApp;
import structurevis.ui.datacreation.wizard.Wizard;

public class WizardMain {

    Image appIcon = new ImageIcon(getClass().getResource("/structurevis/resources/sv_icon.png")).getImage();
    DataModel dataModel = new DataModel();
    static Wizard wizard = new Wizard();
    static MainApp mainapp = null;

    public void show(MainApp mainapp) {
        mainapp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        WizardMain.mainapp = mainapp;
        
        //SwingUtilities.updateComponentTreeUI(wizard.getDialog());
        wizard.getDialog().setTitle("Dataset creation wizard");
        wizard.getDialog().setIconImage(appIcon);

        wizard.registerWizardPanel(WelcomeDescriptor.IDENTIFIER, new WelcomeDescriptor(dataModel));
        wizard.registerWizardPanel(StructureDescriptor.IDENTIFIER, new StructureDescriptor(dataModel));
        wizard.registerWizardPanel(GenomeOrganizationDescriptor.IDENTIFIER, new GenomeOrganizationDescriptor(dataModel));
        wizard.registerWizardPanel(DataDescriptor.IDENTIFIER, new DataDescriptor(dataModel));
        wizard.registerWizardPanel(FinishDescriptor.IDENTIFIER, new FinishDescriptor(dataModel));

        wizard.setCurrentPanel(WelcomeDescriptor.IDENTIFIER);
        
        mainapp.setCursor(Cursor.getDefaultCursor());
        int ret = wizard.showModalDialog();
        //JDialog d = wizard.getDialog().ce;
        
        wizard.showErrorsBeforeContinue(false);
    }

    public WizardMain() {
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }

        new WizardMain().show(null);
    }
}
