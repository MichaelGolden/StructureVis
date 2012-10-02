/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package structurevis.ui.layerpanel;

import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Michael Golden
 */
public class GeneralLayer extends JPanel implements Cloneable {

    int xoffset = 5;
    BufferedImage bufferedImage = null;
    LayerPanel layerPanel;
    String name = "";
    public boolean canPin = false;
    public boolean isPinned = false;

    public GeneralLayer(LayerPanel layerPanel, String name) {
        this.layerPanel = layerPanel;
        this.name = name;
    }

    public void setLayerName(String name)
    {
        this.name = name;
        layerPanel.updateNames();
    }

    public void redraw()
    {

    }

    @Override
    public Object clone () throws CloneNotSupportedException
    {
        GeneralLayer layer = new GeneralLayer(layerPanel, name);
        layer.canPin = canPin;
        layer.isPinned = isPinned;
        return layer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeneralLayer other = (GeneralLayer) obj;
        if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
