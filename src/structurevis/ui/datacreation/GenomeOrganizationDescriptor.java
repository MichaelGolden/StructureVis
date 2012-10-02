/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package structurevis.ui.datacreation;

import structurevis.ui.datacreation.wizard.WizardPanelDescriptor;

/**
 *
 * @author Michael Golden
 */
public class GenomeOrganizationDescriptor  extends WizardPanelDescriptor {
    public static final String IDENTIFIER = "GENOME_ORGANIZATION_PANEL";

    GenomeOrganizationPanel panel;

    public GenomeOrganizationDescriptor(DataModel dataModel) {

        panel = new GenomeOrganizationPanel(dataModel);
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel);
    }

    public Object getNextPanelDescriptor() {
        return DataDescriptor.IDENTIFIER;
    }

    public Object getBackPanelDescriptor() {
        return StructureDescriptor.IDENTIFIER;
    }


    public void aboutToDisplayPanel() {

       /* panel3.setProgressValue(0);
        panel3.setProgressText("Connecting to Server...");
*/
        //getWizard().setNextFinishButtonEnabled(false);
        //getWizard().setBackButtonEnabled(false);

    }

    public void displayingPanel() {
/*
            Thread t = new Thread() {

            public void run() {

                try {
                    Thread.sleep(2000);
                    panel3.setProgressValue(25);
                    panel3.setProgressText("Server Connection Established");
                    Thread.sleep(500);
                    panel3.setProgressValue(50);
                    panel3.setProgressText("Transmitting Data...");
                    Thread.sleep(3000);
                    panel3.setProgressValue(75);
                    panel3.setProgressText("Receiving Acknowledgement...");
                    Thread.sleep(1000);
                    panel3.setProgressValue(100);
                    panel3.setProgressText("Data Successfully Transmitted");

                    getWizard().setNextFinishButtonEnabled(true);
                    getWizard().setBackButtonEnabled(true);

                } catch (InterruptedException e) {

                    panel3.setProgressValue(0);
                    panel3.setProgressText("An Error Has Occurred");

                    getWizard().setBackButtonEnabled(true);
                }

            }
        };

        t.start();*/
    }

    public void aboutToHidePanel() {
        //  Can do something here, but we've chosen not not.
    }
}
