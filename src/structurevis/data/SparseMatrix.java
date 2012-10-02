/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Golden
 */
public class SparseMatrix {

    public int n;
    public int m;
    public SparseVector[] rows;
    public double emptyValue;

    public MatrixIterator getMatrixIterator() {
        return new MatrixIterator();
    }

    public class MatrixIterator implements Iterator<Index2D> {

        int curi = 0;
        Enumeration<Integer> curEn = rows[curi].table.keys();

        public boolean hasNext() {
            if (curEn.hasMoreElements()) {
                return true;
            } else {
                for (int i = curi + 1; i < rows.length; i++) {
                    curi = i;
                    curEn = rows[curi].table.keys();
                    if (curEn.hasMoreElements()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Index2D next() {
            return new Index2D(curi, curEn.nextElement());
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public SparseMatrix(int n, int m, double emptyValue) {
        initialise(n, m, emptyValue);
    }

    public SparseMatrix(int n, double emptyValue) {
        initialise(n, n, emptyValue);
    }

    private void initialise(int n, int m, double emptyValue) {
        this.n = n;
        this.m = m;
        this.emptyValue = emptyValue;

        rows = new SparseVector[n];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new SparseVector(emptyValue);
        }
    }

    public void set(int i, int j, double val) {
        rows[i].set(j, val);
    }

    public double get(int i, int j) {
        if (i >= 0 && i < rows.length) {
            return rows[i].get(j);
        }
        return emptyValue;
    }

    public class Index2D {

        public int i;
        public int j;

        public Index2D(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    public void saveSparseMatrixToFile(File outFile) {
        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(outFile));
            buffer.write(n + "," + m + "," + emptyValue + "\n");
            MatrixIterator it = getMatrixIterator();
            while (it.hasNext()) {
                Index2D index = it.next();
                int i = index.i;
                int j = index.j;
                buffer.write(i + "\t" + j + "\t" + get(i, j) + "\n");
            }
            buffer.close();
        } catch (IOException ex) {
            Logger.getLogger(SparseMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void truncateRows() {
        int lastNonemptyRow = 0;
        for (int i = rows.length - 1; i >= 0; i--) {
            if (!rows[i].table.isEmpty()) {
                lastNonemptyRow = i;
                break;
            }
        }
        this.n = lastNonemptyRow + 1;
    }

    public static SparseMatrix loadSparseMatrixFromFile(File inFile, String splitOn, boolean zeroOffset) throws Exception {
        int n;
        int m;
        double emptyVal = Double.MIN_VALUE;
        BufferedReader buffer2 = new BufferedReader(new FileReader(inFile));
        String textline2 = buffer2.readLine();

        boolean isStructureVisMatrix = true;
        if (textline2.contains(",")) {
            String[] split = textline2.split(",");
            n = Integer.parseInt(split[0]);
            m = Integer.parseInt(split[1]);
            emptyVal = Double.parseDouble(split[2]);
        } else {
            String[] split = textline2.split("(\\s)+");
            n = Integer.parseInt(split[0]);
            m = Integer.parseInt(split[1]);
            while ((textline2 = buffer2.readLine()) != null) {
                split = textline2.split("(\\s)+");
                n = Math.max(Integer.parseInt(split[0]), n);
                m = Math.max(Integer.parseInt(split[1]), m);
            }
            n++;
            m++;
            isStructureVisMatrix = false;
        }
        buffer2.close();


        SparseMatrix matrix = null;
        BufferedReader buffer = new BufferedReader(new FileReader(inFile));

        // initialise sparse matrix
        matrix = new SparseMatrix(n, m, emptyVal);
        String textline = null;
        if (isStructureVisMatrix) {
            buffer.readLine();
        }
        while ((textline = buffer.readLine()) != null) {
            String[] split = textline.split(splitOn);
            try {
                int i = Integer.parseInt(split[0]);
                int j = Integer.parseInt(split[1]);
                if (!zeroOffset) {
                    i -= 1;
                    j -= 1;
                }
                double val = Double.parseDouble(split[2]);
                matrix.set(i, j, val);
            } catch (Exception ex) {
            }
        }
        buffer.close();
        return matrix;
    }

    public SparseMatrix transpose() {
        SparseMatrix transpose = new SparseMatrix(m, n);
        MatrixIterator matrixIterator = getMatrixIterator();
        while (matrixIterator.hasNext()) {
            Index2D index = matrixIterator.next();
            transpose.set(index.j, index.i, get(index.i, index.j));
        }
        return transpose;
    }

    public double[] getMinAndMax() {
        double[] minAndMax = {Float.MAX_VALUE, Float.MIN_VALUE};
        MatrixIterator it = getMatrixIterator();
        while (it.hasNext()) {
            Index2D index = it.next();
            int i = index.i;
            int j = index.j;

            double val = get(i, j);
            if (val != emptyValue) {
                minAndMax[0] = Math.min(minAndMax[0], val);
                minAndMax[1] = Math.max(minAndMax[1], val);
            }
        }
        return minAndMax;
    }

    public static double[] getMinAndMaxFromFile(File inFile) {
        double[] minAndMax = {Float.MAX_VALUE, Float.MIN_VALUE};
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(inFile));
            String[] split = buffer.readLine().split(",");
            String textline = null;
            while ((textline = buffer.readLine()) != null) {
                split = textline.split("\t");
                int i = Integer.parseInt(split[0]);
                int j = Integer.parseInt(split[1]);
                double val = Double.parseDouble(split[2]);
                minAndMax[0] = Math.min(minAndMax[0], val);
                minAndMax[1] = Math.max(minAndMax[1], val);
            }
            buffer.close();
        } catch (IOException ex) {
            Logger.getLogger(SparseMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
        return minAndMax;
    }

    public static SparseMatrix loadSparseMatrixFromPairwiseFile(File inFile, String seperator) {
        SparseMatrix matrix = null;
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(inFile));
            String[] split = {};
            String textline = null;
            int maxi = 0;
            int maxj = 0;
            while ((textline = buffer.readLine()) != null) {
                try {
                    split = textline.split(seperator);
                    int i = Integer.parseInt(split[0]);
                    int j = Integer.parseInt(split[1]);
                    maxi = Math.max(maxi, i);
                    maxj = Math.max(maxj, j);
                } catch (Exception ex) {
                }
            }
            buffer.close();
            buffer = new BufferedReader(new FileReader(inFile));

            matrix = new SparseMatrix(maxi + 1, maxj + 1, Double.MIN_VALUE);
            while ((textline = buffer.readLine()) != null) {
                try {
                    split = textline.split(seperator);
                    int i = Integer.parseInt(split[0]);
                    int j = Integer.parseInt(split[1]);
                    maxi = Math.max(maxi, i);
                    maxj = Math.max(maxj, j);
                    double val = Double.parseDouble(split[2]);
                    matrix.set(i, j, val);
                } catch (Exception ex) {
                }
            }
            buffer.close();

        } catch (IOException ex) {
            Logger.getLogger(SparseMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matrix;
    }

    public static void main(String[] args) {
        SparseMatrix m = new SparseMatrix(100, 200, -1000);
        m.set(39, 40, 2929.2);
        m.set(41, 4, 3.2);
        m.saveSparseMatrixToFile(new File("mat.m"));
//        SparseMatrix p = SparseMatrix.loadSparseMatrixFromFile(new File("mat.m"), ",");
        //p.saveSparseMatrixToFile(new File("mat2.m"));

    }
}
