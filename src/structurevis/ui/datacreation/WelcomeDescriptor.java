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
public class WelcomeDescriptor  extends WizardPanelDescriptor {
    public static final String IDENTIFIER = "WELCOME_PANEL";

    WelcomePanel panel;

    public WelcomeDescriptor(DataModel dataModel) {

        panel = new WelcomePanel(dataModel);
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel);
    }

    public Object getNextPanelDescriptor() {
        return StructureDescriptor.IDENTIFIER;
    }

    public Object getBackPanelDescriptor() {
        return null;
    }


    public void aboutToDisplayPanel() {
    }

    public void aboutToHidePanel() {
    }
}
