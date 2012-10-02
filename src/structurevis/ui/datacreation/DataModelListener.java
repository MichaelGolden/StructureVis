/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package structurevis.ui.datacreation;

import java.io.File;
import java.util.EventListener;

/**
 *
 * @author Michael Golden
 */
public interface DataModelListener extends EventListener {
    public void referenceAlignmentChanged(File oldReferenceAlignment, File newReferenceAlignment);
    public void dataSourceProcessed(int n, int total, Object dataSource);
}
