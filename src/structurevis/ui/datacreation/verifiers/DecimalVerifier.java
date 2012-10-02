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
public class DecimalVerifier extends InputVerifier {

    Color background = Color.white;
    Color invalidBackground = new Color(255,150,150);

    @Override
    public boolean verify(JComponent input) {
        if (input instanceof JTextField) {
            String text = ((JTextField) input).getText();

            try {
                Double.parseDouble(text);
                input.setBackground(background);
                return true;
            } catch (NumberFormatException ex) {
            }

        }

        input.setBackground(invalidBackground);
        return false;
    }
}
