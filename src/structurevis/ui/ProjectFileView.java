/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

import java.awt.Image;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

/**
 *
 * @author Michael Golden
 */
public class ProjectFileView extends FileView {

    Icon appIcon = new ImageIcon(getClass().getResource("/structurevis/resources/sv_icon.png"));
    //Icon appIcon2 = new ImageIcon(new ImageIcon(getClass().getResource("../resources/icon.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));

    public Icon getIcon(File file) {

        if (ProjectFileFilter.isProjectFolder(file)) {
            return appIcon;
        }
        return super.getIcon(file);
    }
}
