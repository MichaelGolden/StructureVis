/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LayerPanel.java
 *
 * Created on 02 Dec 2011, 9:33:58 AM
 */
package structurevis.ui.layerpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;
import structurevis.ui.MainApp;

/**
 *
 * @author Michael Golden
 */
public class LayerPanel extends JPanel implements MouseListener, MouseMotionListener, Scrollable {

    boolean repaint = true;
    Font f1 = new Font("Arial", Font.PLAIN, 15);
    Font f2 = new Font("Arial", Font.PLAIN, 12);
    int selectedStart = -1;
    int selectedEnd = -1;
    int mouseoverGeneStart = -1;
    int mouseoverGeneEnd = -1;
    public int genomeLength = 10000;
    public int height = 0;
    MainApp mainapp;
    //Dimension preferredScrollableSize = null;
    int width = 1000;
    boolean trackWidth = true;
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem autofitItem = new JMenuItem("Autofit width");
    JMenuItem zoomInItem = new JMenuItem("Zoom in");
    JMenuItem zoomOutItem = new JMenuItem("Zoom out");
    ArrayList<NameField> nameFields = new ArrayList<NameField>();
    ArrayList<GeneralLayer> layers = new ArrayList<GeneralLayer>();
    GenomeLayer genomeLayer;
    GraphLayer graphLayer1D;
    ArrayList<GeneralLayer> pinnedLayers = new ArrayList<GeneralLayer>();

    /** Creates new form LayerPanel */
    public LayerPanel(MainApp mainapp, int genomeLength) {
        initComponents();
        this.mainapp = mainapp;
        //this.genomeLayer = genomeLayer;
        // this.graphLayer1D = graphLayer1D;
        this.genomeLength = genomeLength;

        this.jSplitPane1.setBorder(BorderFactory.createEmptyBorder());
        this.jSplitPane1.setDividerLocation(125);

        addMouseListener(this);
//        autofitItem.addActionListener(this);
        //zoomInItem.addActionListener(this);
        //popupMenu.add(zoomOutItem).addActionListener(this);
        popupMenu.add(autofitItem);
        popupMenu.add(zoomInItem);
        popupMenu.add(zoomOutItem);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // addLayer(genomeLayer);
        //addLayer(graphLayer1D);
        updatePanel();
    }

    public void setGenomeLayer(GenomeLayer genomeLayer) {
        this.genomeLayer = genomeLayer;
        updatePanel();
    }

    public void setGraphLayer1D(GraphLayer graphLayer1D) {
        this.graphLayer1D = graphLayer1D;
        updatePanel();
    }

    public void setGenomeLength(int genomeLength)
    {
        this.genomeLength = genomeLength;
        updatePanel();
    }

    public void updatePanel() {
        removeAllLayers();
        if (genomeLayer != null) {
            genomeLayer.canPin = false;
            addLayer(genomeLayer);
        }
        if (graphLayer1D != null) {
            addLayer(graphLayer1D);
        }
        for (int i = 0; i < pinnedLayers.size(); i++) {
            if (!pinnedLayers.get(i).equals(graphLayer1D)) {
                if(pinnedLayers.get(i).isPinned)
                {
                    addLayer(pinnedLayers.get(i));
                }
            }
        }
        revalidate();
        repaint();
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).redraw();
        }

        if(mainapp != null)
        {
            
            // this was formerly revalidate
            mainapp.validate();
        }
    }

    public void redraw ()
    {
        for(int i = 0 ; i < layers.size() ; i++)
        {
            layers.get(i).redraw();
        }
    }

    public void pinLayer(GeneralLayer layer) {
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).equals(layer) && !layer.isPinned) {
                try {
                    layer.isPinned = true;
                    GeneralLayer layerClone = (GeneralLayer) layers.get(i).clone();
                    if(!pinnedLayers.contains(layerClone))
                    {
                        pinnedLayers.add(layerClone);
                    }
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(LayerPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
    }

    public boolean unpinLayer(GeneralLayer layer) {
        layer.isPinned = false;
        boolean success = pinnedLayers.remove(layer);
        updatePanel();
        return success;
    }

    public void addLayer(GeneralLayer layer) {
        NameField nameField = new NameField(this, layer.name, layer);
        nameField.setMinimumSize(new Dimension(0, layer.getPreferredSize().height));
        nameField.setPreferredSize(new Dimension(100, layer.getPreferredSize().height));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, layer.getPreferredSize().height));
        nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        left.add(nameField);
        nameFields.add(nameField);

        layer.setMinimumSize(new Dimension(0, layer.getPreferredSize().height));
        layer.setPreferredSize(new Dimension(800, layer.getPreferredSize().height));
        layer.setMaximumSize(new Dimension(Integer.MAX_VALUE, layer.getPreferredSize().height));
        layer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        right.add(layer);
        layers.add(layer);

        height += layer.getPreferredSize().height;
        setPreferredSize(new Dimension(getPreferredSize().width, height));
        
    }

    public void updateNames() {
        for (int i = 0; i < nameFields.size(); i++) {
            nameFields.get(i).textField.setText(layers.get(i).name);
        }
    }

    public void removeAllLayers() {
        height = 0;
        left.removeAll();
        nameFields.clear();
        right.removeAll();
        layers.clear();
    }

    /* public void setLayers(ArrayList<String> names, ArrayList<GeneralLayer> layers) {
    removeAllLayers();
    for (int i = 0; i < names.size(); i++) {
    addLayer(layers.get(i));
    }
    }*/
    public int getPositionInGenome(double f) {
        return (int) (f * genomeLength);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        left = new javax.swing.JPanel();
        right = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBar(null);
        jScrollPane1.setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setDividerSize(2);

        left.setPreferredSize(new java.awt.Dimension(100, 0));
        left.setLayout(new javax.swing.BoxLayout(left, javax.swing.BoxLayout.PAGE_AXIS));
        jSplitPane1.setLeftComponent(left);

        right.setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));
        right.setPreferredSize(new java.awt.Dimension(0, 20));
        right.setLayout(new javax.swing.BoxLayout(right, javax.swing.BoxLayout.PAGE_AXIS));
        jSplitPane1.setRightComponent(right);

        jScrollPane1.setViewportView(jSplitPane1);

        add(jScrollPane1);
    }// </editor-fold>//GEN-END:initComponents

    public void mouseClicked(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mousePressed(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseDragged(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseMoved(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JPanel left;
    public javax.swing.JPanel right;
    // End of variables declaration//GEN-END:variables

    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
        // return new Dimension(1000,100);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return -1;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return -1;
    }

    public boolean getScrollableTracksViewportWidth() {
        return trackWidth;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public void autofitWidth() {
        trackWidth = true;
        this.revalidate();
        width = getWidth();
    }

    public void resizeWidth(int width) {
        trackWidth = false;
        this.setPreferredSize(new Dimension(width, height));
        this.revalidate();
    }

    public void zoomIn() {
        width = (int) ((double) getWidth() * 1.5);
        resizeWidth(width);
    }

    public void zoomOut() {
        width = (int) ((double) getWidth() / 1.5);
        resizeWidth(width);
    }
}
