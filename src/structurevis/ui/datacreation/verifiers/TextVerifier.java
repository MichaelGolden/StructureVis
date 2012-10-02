/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.datacreation.verifiers;

import java.awt.Color;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author Michael Golden
 */
public class TextVerifier extends InputVerifier {

    Color background = Color.white;
    Color invalidBackground = new Color(255, 150, 150);

    @Override
    public boolean verify(JComponent input) {
        if (input instanceof JTextField) {
            String text = ((JTextField) input).getText();
            if (text.length() > 0) {
                input.setBackground(background);
                return true;
            }
        }
        
        input.setBackground(invalidBackground);
        return false;
    }
}
