/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package structurevis.ui.datacreation.wizard;

import java.util.EventListener;

/**
 *
 * @author Michael Golden
 */
public interface WizardListener extends EventListener {
    
    public void panelChangedEvent(Object fromPanelIdentifier, Object toPanelIdentifier);
}
