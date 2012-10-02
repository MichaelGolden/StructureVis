/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.analyses;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import structurevis.data.Mapping;
import structurevis.ui.MainApp;
import structurevis.ui.TableSorter;
import structurevis.ui.datacreation.DataModel;

/**
 *
 * @author Michael
 */
public class RankingTable extends JPanel {

    TableDataModel tableDataModel;
    final JTable table;
    public JScrollPane scrollPane;
    final MainApp mainapp;

    public RankingTable(MainApp mainapp) {
        super(new BorderLayout());
        this.mainapp = mainapp;

        tableDataModel = new TableDataModel();
        TableSorter sorter = new TableSorter(tableDataModel);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());
        sorter.sortOnColumn(table.getTableHeader(),table.getColumnCount()-1,1);
        table.setFillsViewportHeight(true);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();

                    int s = ((Integer) table.getModel().getValueAt(row, 0)).intValue() - 1;
                    RankingTable.this.mainapp.openStructure(s);
                    //System.out.println(row+"\tID"+);
                    // do some action
                }
            }
        });


        /*
         * table.setDefaultRenderer(Boolean.class, new
         * structurevis.ui.datacreation.GenomeOrganizationTable.CheckBoxRenderer(true));
         * table.setDefaultRenderer(Color.class, new
         * structurevis.ui.datacreation.GenomeOrganizationTable.ColorRenderer(true));
         * table.setDefaultEditor(Color.class, new
         * structurevis.ui.datacreation.GenomeOrganizationTable.ColorEditor());
         * table.setRowHeight(20);
         *
         * table.getColumnModel().getColumn(8).setCellEditor(new
         * structurevis.ui.datacreation.GenomeOrganizationTable.SpinnerEditor(0,
         * Integer.MAX_VALUE));
         */
        scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    class TableDataModel extends AbstractTableModel {

        String[] columnNames = {"Structure #", "Structure ID", "Location", "Structure Length", "Structure N", "Genome N", "Structure Mean", "Genome Mean", "Structure Median", "Genome Median", "Mann-Whitney U stat.", "p-value", "z-score"};
        Class[] columnClasses = {Integer.class, String.class, Location.class, Integer.class, Integer.class, Integer.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class};
        public ArrayList<Object[]> rows = new ArrayList<Object[]>();
        public ArrayList<Mapping> mappings = new ArrayList<Mapping>();
        public ArrayList<File> mappingFiles = new ArrayList<File>();

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

        public void clear() {
            rows.clear();
            fireTableRowsDeleted(0, rows.size());
        }

        public void addRows(ArrayList rowsArray) {
            int start = rows.size() + 1;
            int end = rows.size() + rowsArray.size();
            rows.addAll(rowsArray);
            fireTableRowsInserted(start, end);
        }

        public void addRow(Object[] row) {
            rows.add(row);
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
            /*
             * if(value < 0) { rows.add(index+value, array); } else {
             *
             * }
             */
            fireTableDataChanged();
        }

        /*
         * JTable uses this method to determine the default renderer/ editor for
         * each cell. If we didn't implement this method, then the last column
         * would contain text ("true"/"false"), rather than a check box.
         */
        public Class getColumnClass(int c) {
            return columnClasses[c];
        }

        /*
         * Don't need to implement this method unless your table's editable.
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        /*
         * Don't need to implement this method unless your table's data can
         * change.
         */
        public void setValueAt(Object value, int row, int col) {
            rows.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SimpleTableDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        RankingTable newContentPane = new RankingTable(null);
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
}
