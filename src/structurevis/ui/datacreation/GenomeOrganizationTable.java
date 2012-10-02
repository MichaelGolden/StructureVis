package structurevis.ui.datacreation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import structurevis.data.Mapping;
import structurevis.structures.io.ReadseqTools;

/**
 *
 * @author Michael Golden
 */
public class GenomeOrganizationTable extends JPanel {

    DataModel dataModel;
    TableDataModel tableDataModel;
    final JTable table;
    public JScrollPane scrollPane;
    static int currentColor = 0;
    static Color[] geneColors = {
        new Color(255, 190, 190),
        new Color(190, 255, 255),
        new Color(190, 190, 255),
        new Color(255, 190, 255),
        new Color(200, 255, 190),
        new Color(255, 255, 190)};

    public GenomeOrganizationTable(DataModel dataModel) {
        super(new BorderLayout());
        this.dataModel = dataModel;

        tableDataModel = new TableDataModel();
        table = new JTable(tableDataModel);
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Boolean.class, new CheckBoxRenderer(true));
        table.setDefaultRenderer(Color.class, new ColorRenderer(true));
        table.setDefaultEditor(Color.class, new ColorEditor());
        table.setRowHeight(20);

        table.getColumnModel().getColumn(8).setCellEditor(new SpinnerEditor(0, Integer.MAX_VALUE));

        scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    public static Object[] defaultRow() {
        currentColor = (currentColor + 1) % geneColors.length;
        Object[] data = {new Boolean(false), "", new Integer(-1), new Integer(-1), new Boolean(true), new Integer(-1), new Integer(-1), geneColors[currentColor], new Integer(0)};
        return data;
    }

    public Mapping createGenbankMapping(File genbankFile) {
        Mapping genbankMapping = null;
        genbankMapping = dataModel.mapCache.getMap(dataModel.getReferenceAlignment(), genbankFile);
        if (genbankMapping == null) {
            ReadseqTools.convertToFastaFormat(dataModel.getReferenceAlignment(), new File("temp1.fas"));
            ReadseqTools.convertToFastaFormat(genbankFile, new File("temp2.fas"));
            genbankMapping = Mapping.createMappingWithRestrictionsAutoDirection(new File("temp1.fas"), new File("temp2.fas"), 1, -1, -1, -1, -1);
            tableDataModel.updateMappings();
            dataModel.mapCache.registerMap(dataModel.getReferenceAlignment(), genbankFile, genbankMapping);
        }
        return genbankMapping;
    }
    
    public int genomeLength = 0;
    public void addAnnotationsFromGenbankFile(File genbankFile, boolean map) {
        WizardMain.wizard.getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Mapping genbankMapping = null;
        if (map) {
            genbankMapping = createGenbankMapping(genbankFile);
            /*setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            genbankMapping = dataModel.mapCache.getMap(dataModel.getReferenceAlignment(), genbankFile);
            if (genbankMapping == null) {
            ReadseqTools.convertToFastaFormat(dataModel.getReferenceAlignment(), new File("temp1.fas"));
            //tableDataModel.genomeOrganizationMappingFile = new File(createMappingField.getText());
            ReadseqTools.convertToFastaFormat(genbankFile, new File("temp2.fas"));
            genbankMapping = Mapping.createMappingWithRestrictionsAutoDirection(new File("temp1.fas"), new File("temp2.fas"), 1, -1, -1, -1, -1);
            tableDataModel.updateMappings();
            dataModel.mapCache.registerMap(dataModel.getReferenceAlignment(), genbankFile, genbankMapping);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }*/
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(genbankFile));
            // a namespace to override that in the file
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            // we are reading DNA sequences
            
            RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(br, ns);
            while (seqs.hasNext()) {
                try {
                    RichSequence rs = seqs.nextRichSequence();
                    Iterator<Feature> it = rs.features();
                    while (it.hasNext()) {
                        Feature ft = it.next();

                        String name = null;
                        int newGenomeLength = 0;
                        if (ft.getType().equalsIgnoreCase("source")) {
                            newGenomeLength = ft.getLocation().getMax();
                        }
                        if (genbankMapping != null) {                            
                           // newGenomeLength = genbankMapping.bToANearest(genbankMapping.getRefLength());
                            newGenomeLength = genbankMapping.getRefLength();
                        }
                        genomeLength = Math.max(genomeLength, newGenomeLength);

                        if (ft.getAnnotation().containsProperty("gene")) {
                            name = ft.getAnnotation().getProperty("gene").toString();
                        } else if (ft.getAnnotation().containsProperty("product")) {
                            name = ft.getAnnotation().getProperty("product").toString();
                        } else {
                            name = ft.getType();
                        }


                        Iterator<Location> blocks = ft.getLocation().blockIterator();
                        Object[] newRow = defaultRow();                        
                        while(blocks.hasNext())
                        {
                            Location lt = blocks.next();
                            //lt.
                            Object [] row = Arrays.copyOf(newRow, newRow.length);                            
                            row[1] = name;
                            row[2] = lt.getMin();
                            row[3] = lt.getMax();
                            tableDataModel.addRow(row, genbankMapping, genbankFile);
                        }                       
                    }
                } catch (NoSuchElementException ex) {
                    Logger.getLogger(GenomeOrganizationTable.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BioException ex) {
                    Logger.getLogger(GenomeOrganizationTable.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenomeOrganizationTable.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(GenomeOrganizationTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        WizardMain.wizard.getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SimpleTableDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        GenomeOrganizationTable newContentPane = new GenomeOrganizationTable(null);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    class TableDataModel extends AbstractTableModel {

        String[] columnNames = {"Use", "Locus name", "Start", "End", "Use mapping", "Mapped start", "Mapped end", "Color", "Level"};
        Class[] columnClasses = {Boolean.class, String.class, Integer.class, Integer.class, Boolean.class, Integer.class, Integer.class, Color.class, Integer.class};
        public ArrayList<Object[]> rows = new ArrayList<Object[]>();
        public ArrayList<Mapping> mappings = new ArrayList<Mapping>();
        public ArrayList<File> mappingFiles = new ArrayList<File>();

        public void recomputeMappings() {
            WizardMain.wizard.getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (int i = 0; i < mappings.size(); i++) {
                Mapping mapping = mappings.get(i);
                if (mapping != null) {
                    mapping = createGenbankMapping(mappingFiles.get(i));
                    mappings.set(i, mapping);
                }
            }

            updateMappings();
            WizardMain.wizard.getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return rows.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return rows.get(row)[col];
        }
        boolean hasMappedData = false;

        public void addRow(Object[] row, Mapping mapping, File mappingFile) {
            rows.add(row);
            mappings.add(mapping);
            mappingFiles.add(mappingFile);
            if (mapping == null) {
                row[4] = null;
            } else {
                hasMappedData = true;
            }
            fireTableRowsInserted(rows.size(), rows.size());
        }

        public void removeRow(int index) {
            rows.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public void removeRows(int[] indices) {
            Arrays.sort(indices);
            int correction = 0;
            for (int i = 0; i < indices.length; i++) {
                removeRow(indices[i] - correction);
                correction++;
            }
        }

        public void move(int index, int value) {

            Object[] array = rows.remove(index);
            rows.add(index + value, array);
            /* if(value < 0)
            {
            rows.add(index+value, array);
            }
            else
            {

            }*/
            fireTableDataChanged();
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return columnClasses[c];
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            if (col == 4 && getValueAt(row, col) == null) {
                return false;
            }
            if (col != 5 && col != 6) {
                return true;
            }
            return false;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            rows.get(row)[col] = value;
            fireTableCellUpdated(row, col);

            if (col == 2 || col == 3 || col == 4) {
                updateMapping(row);
            }
        }

        public void updateMappings() {
            for (int i = 0; i < rows.size(); i++) {
                updateMapping(i);
            }
            // this.fireTableDataChanged();
        }

        public void updateMapping(int row) {
            Mapping mapping = mappings.get(row);
            if (getValueAt(row, 4) != null && (Boolean) getValueAt(row, 4) && mapping != null) {
                //System.out.println("updating row " + row + "\t" + (((Integer) getValueAt(row, 2) - 1) + 1) + "\t" + (((Integer) getValueAt(row, 3) - 1) + 1));
                setValueAt(mapping.bToANearest((Integer) getValueAt(row, 2) - 1) + 1, row, 5);
                setValueAt(mapping.bToANearest((Integer) getValueAt(row, 3) - 1) + 1, row, 6);
            } else {
                setValueAt((Integer) getValueAt(row, 2), row, 5);
                setValueAt((Integer) getValueAt(row, 3), row, 6);
                //System.out.println("updating xxx " + row);
            }
        }
    }

    class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

        //Border unselectedBorder = null;
        //Border selectedBorder = null;
        //boolean isBordered = true;
        public CheckBoxRenderer(boolean isBordered) {
            // this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus, int row, int column) {
            Boolean isChecked = (Boolean) object;
            if (isChecked == null) {
                setEnabled(false);
            } else {
                setEnabled(true);
                this.setSelected(isChecked);
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            /*if (isBordered) {
            if (isSelected) {
            if (selectedBorder == null) {
            selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
            table.getSelectionBackground());
            }
            setBorder(selectedBorder);
            } else {
            if (unselectedBorder == null) {
            unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
            table.getBackground());
            }
            setBorder(unselectedBorder);
            }
            }*/
            return this;
        }
    }

    class ColorRenderer extends JLabel
            implements TableCellRenderer {

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color newColor = (Color) color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());
            return this;
        }
    }

    class ColorEditor extends AbstractCellEditor
            implements TableCellEditor,
            ActionListener {

        Color currentColor;
        JButton button;
        JColorChooser colorChooser;
        JDialog dialog;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            //Set up the editor (from the table's point of view),
            //which is a button.
            //This button brings up the color chooser dialog,
            //which is the editor from the user's point of view.
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            //Set up the dialog that the button brings up.
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button,
                    "Pick a Color",
                    true, //modal
                    colorChooser,
                    this, //OK button handler
                    null); //no CANCEL button handler
        }

        /**
         * Handles events from the editor button and from
         * the dialog's OK button.
         */
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                //The user has clicked the cell, so
                //bring up the dialog.
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);

                //Make the renderer reappear.
                fireEditingStopped();

            } else { //User pressed dialog's "OK" button.
                currentColor = colorChooser.getColor();
            }
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return currentColor;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            currentColor = (Color) value;
            return button;
        }
    }

    class SpinnerEditor extends AbstractCellEditor
            implements TableCellEditor,
            ChangeListener {

        Integer currentValue;
        JSpinner button;

        public SpinnerEditor(int min, int max) {
            button = new JSpinner(new SpinnerNumberModel(0, min, max, 1));
            button.addChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            currentValue = (Integer) button.getValue();
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return currentValue;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            currentValue = (Integer) value;
            button.setValue(currentValue);
            return button;
        }
    }
}
