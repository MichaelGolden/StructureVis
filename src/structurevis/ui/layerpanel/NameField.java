/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.layerpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 *
 * @author Michael Golden
 */
public class NameField extends JPanel implements ActionListener, MouseListener {

    public Border borderPadding = BorderFactory.createEmptyBorder(0, 3, 0, 0);
    public JTextField textField = new JTextField();
    ImageIcon pinnedIcon = new ImageIcon(getClass().getResource("/structurevis/resources/pinned.png"));
    ImageIcon unpinnedIcon = new ImageIcon(getClass().getResource("/structurevis/resources/unpinned.png"));
    public JLabel labelIcon = new JLabel();
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem pinTab = new JMenuItem("Pin tab");
    LayerPanel layerPanel;
    GeneralLayer layer;

    public NameField(LayerPanel layerPanel, String text, GeneralLayer layer) {
        this.layerPanel = layerPanel;
        this.layer = layer;

        addMouseListener(this);
        textField.addMouseListener(this);
        setBackground(Color.white);
        setLayout(new BorderLayout());

        textField.setText(text);
        textField.setEditable(false);
        textField.setBorder(borderPadding);
        this.add(textField, BorderLayout.CENTER);

        labelIcon.addMouseListener(this);
        labelIcon.setBorder(BorderFactory.createEmptyBorder(2,8,0,0));
        if (layer.canPin) {
            if (layer.isPinned) {
                labelIcon.setIcon(pinnedIcon);
                labelIcon.setToolTipText("Unpin layer");
            } else {
                labelIcon.setIcon(unpinnedIcon);
                labelIcon.setToolTipText("Pin layer");
            }
            this.add(labelIcon, BorderLayout.EAST);
        }

        pinTab.addActionListener(this);
        popupMenu.add(pinTab);
    }

    public void setBackground(Color backgroundColor) {
        super.setBackground(backgroundColor);
        if (textField != null) {
            textField.setBackground(backgroundColor);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(pinTab)) {
            if (!layer.isPinned) {
                labelIcon.setIcon(pinnedIcon);
                layerPanel.pinLayer(layer);
            } else {
                labelIcon.setIcon(unpinnedIcon);
                layerPanel.unpinLayer(layer);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (layer.canPin) {
            if (e.getClickCount() >= 1 && SwingUtilities.isLeftMouseButton(e)) {
                if (!layer.isPinned) {
                    labelIcon.setIcon(pinnedIcon);
                    layerPanel.pinLayer(layer);
                    labelIcon.setToolTipText("Unpin layer");
                } else {
                    labelIcon.setIcon(unpinnedIcon);
                    layerPanel.unpinLayer(layer);
                    labelIcon.setToolTipText("Pin layer");
                }
            }
            /* if (SwingUtilities.isRightMouseButton(e)) {
            if (layer.isPinned) {
            this.pinTab.setText("Unpin tab");
            } else {
            this.pinTab.setText("Pin tab");
            }
            this.popupMenu.show(this, e.getX(), e.getY());
            }*/
        }
    }

    public void mousePressed(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseReleased(MouseEvent e) {
        if (layer.canPin) {
            /* if (SwingUtilities.isRightMouseButton(e)) {
            if (layer.isPinned) {
            this.pinTab.setText("Unpin tab");
            } else {
            this.pinTab.setText("Pin tab");
            }
            this.popupMenu.show(this, e.getX(), e.getY());
            }*/
        }
    }

    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
