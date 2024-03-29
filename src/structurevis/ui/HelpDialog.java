/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author Michael
 */
public class HelpDialog extends javax.swing.JDialog implements HyperlinkListener {

    /**
     * Creates new form HelpDialog
     */
    public HelpDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        leftPane.addHyperlinkListener(this);
        mainPane.addHyperlinkListener(this);
    }
    
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                
                mainPane.setPage(event.getURL());
                //jTextPane1.setP
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public static void showDialog(ImageIcon icon, File toc, File main) {
        try {
            HelpDialog dialog = new HelpDialog(new javax.swing.JFrame(), true);
            dialog.setIconImage(icon.getImage());
            dialog.setSize(800, 600);
            dialog.setTitle("StructureVis User Documentation");
            dialog.leftPane.setPage("file:///" + toc.getAbsolutePath().replaceAll("\\\\", "/"));
            dialog.mainPane.setPage("file:///" + main.getAbsolutePath().replaceAll("\\\\", "/"));
            dialog.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(HelpDialog.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        mainPane = new javax.swing.JEditorPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        leftPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);

        mainPane.setEditable(false);
        jScrollPane2.setViewportView(mainPane);

        getContentPane().add(jScrollPane2, java.awt.BorderLayout.CENTER);

        leftPane.setEditable(false);
        leftPane.setMinimumSize(new java.awt.Dimension(150, 20));
        jScrollPane1.setViewportView(leftPane);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.LINE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HelpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HelpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HelpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HelpDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            public void run() {
                HelpDialog dialog = new HelpDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JEditorPane leftPane;
    private javax.swing.JEditorPane mainPane;
    // End of variables declaration//GEN-END:variables
}
