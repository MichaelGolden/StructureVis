/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import structurevis.ui.FileSpecifier;
import structurevis.data.SparseMatrix.Index2D;
import structurevis.data.SparseMatrix.MatrixIterator;

/**
 *
 * @author Michael Golden
 */
public class CompareCoevolution {

    public static final DecimalFormat df = new DecimalFormat("0.0");

    /* public static void main(String[] args) {
    //new Compare();
    SparseMatrix m = CompareCoevolution.getCoevolutionMatrix(new File("d:/Nasp/BFDV/DIR/"),0);
    m.truncateRows();
    m.saveSparseMatrixToFile(new File("bfdv-pvals.txt"));
    //CompareCoevolution.compareCoevolutionToNasp2();
    }*/
    public static void printTable(double[][] table, int n, int m) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                System.out.print(df.format(table[i][j]) + "\t");
            }
            System.out.println();
        }
    }

    public static double[][] expected(double[][] observed, int n, int m) {
        double[] t1 = new double[m];
        double[] t2 = new double[n];
        double total = 0;

        double expected[][] = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                t1[j] += observed[i][j];
                t2[i] += observed[i][j];
                total += observed[i][j];
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                expected[i][j] = (t1[j] * t2[i]) / total;
            }
        }


        return expected;
    }

    public static double chi2(double[][] observed, double[][] expected, int n, int m) {
        double chi2 = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (expected[i][j] != 0) {
                    chi2 += Math.pow(observed[i][j] - expected[i][j], 2) / expected[i][j];
                }
            }
        }
        return chi2;
    }
    //static FileSpecifier fs = new FileSpecifier(new File("F:/PCV/pcv.txt"),"pcv");
    //static FileSpecifier fs = new FileSpecifier(new File("C:/project/hepacivirus/hepacivirus_wobble.txt"),"hepacivirus_wobble");
    //static FileSpecifier fs = new FileSpecifier(new File("C:/project/hepacivirus/hepacivirus_wobble.txt"), "hepacivirus_wobble");
    //static FileSpecifier fs_comp = new FileSpecifier(new File("C:/project/hepacivirus/hepacivirus_comp.txt"), "hepacivirus_comp");
    // static FileSpecifier fs_revcomp = new FileSpecifier(new File("C:/project/hepacivirus/hepacivirus_revcomp.txt"), "hepacivirus_revcomp");

    /* public static void compareCoevolutionToNasp2() {
    Mapping.setAlignmentParameters(-12, -1, 1);
    Mapping naspAndCoevolutionMapping = new Mapping(fs.naspAlignmentFile, fs.coevolutionAlignmentFile);
    ArrayList<String> naspSequences = new ArrayList<String>();
    ArrayList<String> naspSequenceNames = new ArrayList<String>();
    IO.loadFastaSequences(fs.naspAlignmentFile, naspSequences, naspSequenceNames);
    int len = naspSequences.get(0).length();
    int c = 0;
    int t = 0;

    // System.out.println("LENGTH="+len);
    SparseMatrix naspMatrixOriginal = CollateData.getNaspMatrix(fs.naspMatrixFile);
    // SparseMatrix naspMatrixComp = CompareCoevolution.reverse(CollateData.getNaspMatrix(fs_revcomp.naspMatrixFile), len);
    //SparseMatrix naspMatrixComp = CollateData.getNaspMatrix(fs_revcomp.naspMatrixFile);
    //SparseMatrix naspMatrixComp = CollateData.getNaspMatrix(fs_comp.naspMatrixFile);
    //SparseMatrix naspMatrix = CollateData.getNaspMatrix(fs.naspMatrixFile);

    // just the complementary
    //SparseMatrix naspMatrix = CompareCoevolution.getMatrixAMinusBSpecial(naspMatrixComp, naspMatrixOriginal);
    // the original minus the complementary
    //SparseMatrix naspMatrix = CompareCoevolution.getMatrixAMinusBSpecial(naspMatrixOriginal, naspMatrixComp);
    SparseMatrix naspMatrix = naspMatrixOriginal;

    SparseMatrix coevolutionMatrixPvals = getCoevolutionMatrix(fs.coevolutionDir, 0);
    SparseMatrix coevolutionMatrixGained = getCoevolutionMatrix(fs.coevolutionDir, 2);


    File outFile = new File("hepaci_results_gained.txt");
    try {
    BufferedWriter buffer = new BufferedWriter(new FileWriter(outFile));
    buffer.write("Nasp\tpval\tRgained\n");
    for (int i = 0; i < len; i++) {
    if (naspAndCoevolutionMapping.aToB(i) == -1) {
    int aToRef = naspAndCoevolutionMapping.aToRef(i);
    //System.out.println(i + "\t" + aToRef + "\t" + naspAndCoevolutionMapping.refToB(aToRef));
    c++;
    }
    t++;
    for (int j = 0; j < len; j++) {
    int x = naspAndCoevolutionMapping.aToB(i);
    int y = naspAndCoevolutionMapping.aToB(j);
    double pval = coevolutionMatrixPvals.get(x, y);
    if (pval != -1000 && Math.abs(y - x) <= 1000) {
    if (naspMatrix.get(i, j) != -1) // for A not B matrices
    {
    buffer.write(naspMatrix.get(i, j) + "\t" + coevolutionMatrixPvals.get(x, y) + "\t" + coevolutionMatrixGained.get(x, y));
    buffer.newLine();
    }
    }
    }
    }
    System.out.println("C " + c + " T " + t);
    buffer.close();
    } catch (IOException ex) {
    Logger.getLogger(CompareCoevolution.class.getName()).log(Level.SEVERE, null, ex);
    }


    double significance = 0.05;
    int COMPLEMENTARY = 0;
    int NEUTRAL = 1;
    int NONCOMPLEMENTARY = 2;
    int UNPAIRED = 0;
    int PAIRED = 1;
    double[][] observedall = new double[3][2];
    double[][] observedComplementary = new double[2][2];
    double[][] observedNoncomplementary = new double[2][2];
    try {
    BufferedReader buffer = new BufferedReader(new FileReader(outFile));
    String textline = null;
    buffer.readLine();
    while ((textline = buffer.readLine()) != null) {
    String[] split = textline.split("\t");
    double nasp = Double.parseDouble(split[0]);
    double pval = Double.parseDouble(split[1]);
    double rgained = Double.parseDouble(split[2]);

    int paired = UNPAIRED;
    if (nasp >= 0) {
    paired = PAIRED;
    }

    int coevolving = NEUTRAL;
    if (pval < significance) {
    if (rgained < 1) {
    coevolving = NONCOMPLEMENTARY;
    } else {
    coevolving = COMPLEMENTARY;
    }
    }

    int a = 0;
    if (coevolving == COMPLEMENTARY) {
    a = 1;
    }
    if (rgained > 1) {
    observedComplementary[a][paired]++;
    }


    int b = 0;
    if (coevolving == NONCOMPLEMENTARY) {
    b = 1;
    }
    if (rgained < 1) {
    observedNoncomplementary[b][paired]++;
    }


    observedall[coevolving][paired]++;
    }
    buffer.close();
    } catch (IOException ex) {
    ex.printStackTrace();
    }

    ChiSquaredDistributionImpl chiSquaredDist1Degree = new ChiSquaredDistributionImpl(1);
    ChiSquaredDistributionImpl chiSquaredDist2Degree = new ChiSquaredDistributionImpl(2);

    System.out.println("Unpaired&Complementary\tPaired&Complementary");
    System.out.println("Unpaired&Neutral\tPaired&Neutral");
    System.out.println("Unpaired&Noncomplementary\tPaired&Noncomplementary");
    printTable(observedall, 3, 2);
    double[][] expected = expected(observedall, 3, 2);
    printTable(expected, 3, 2);
    double chi2all = chi2(observedall, expected, 3, 2);
    double pall = 1;
    try {
    pall = 1 - chiSquaredDist2Degree.cumulativeProbability(chi2all);
    } catch (MathException ex) {
    Logger.getLogger(CompareCoevolution.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.println(df.format(chi2all) + "\t" + pall);
    System.out.println();

    System.out.println("Unpaired&NotCoevolving\tPaired&NotCoevolving");
    System.out.println("Unpaired&Coevolving\tPaired&Coevolving");
    printTable(observedComplementary, 2, 2);
    double[][] expectedComplementary = expected(observedComplementary, 2, 2);
    printTable(expectedComplementary, 2, 2);
    double chi2complementary = chi2(observedComplementary, expectedComplementary, 2, 2);
    double pcomplementary = 1;
    try {
    pcomplementary = 1 - chiSquaredDist1Degree.cumulativeProbability(chi2complementary);
    } catch (MathException ex) {
    Logger.getLogger(CompareCoevolution.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.println(df.format(chi2complementary) + "\t" + pcomplementary);
    System.out.println();

    System.out.println("Unpaired&NotCoevolving\tPaired&NotCoevolving");
    System.out.println("Unpaired&NoncompCoevolving\tPaired&NoncompCoevolving");
    printTable(observedNoncomplementary, 2, 2);
    double[][] expectedNoncomplementary = expected(observedNoncomplementary, 2, 2);
    printTable(expectedNoncomplementary, 2, 2);
    double chi2noncomplementary = chi2(observedNoncomplementary, expectedNoncomplementary, 2, 2);
    double pnoncomplementary = 1;
    try {
    pnoncomplementary = 1 - chiSquaredDist1Degree.cumulativeProbability(chi2noncomplementary);
    } catch (MathException ex) {
    Logger.getLogger(CompareCoevolution.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.println(df.format(chi2noncomplementary) + "\t" + pnoncomplementary);
    System.out.println();

    }*/
    static double emptyVal = -1000;

    public static SparseMatrix getCoevolutionMatrix(File coevolutionDir, int parameter) {
        int n = 12000;
        SparseMatrix coevolutionMatrix = new SparseMatrix(n, emptyVal);
        ArrayList<File> files = CollateData.recursivelyListFiles(coevolutionDir);
        for (int i = 0; i < files.size(); i++) {
            readCoevolutionIntoMatrix(files.get(i), coevolutionMatrix, parameter);
        }
        return coevolutionMatrix;
    }

    /*public static void readCoevolutionIntoMatrixWithGained(File coevolutionFile, SparseMatrix coevolutionMatrix, boolean pvals) {
    try {
    BufferedReader buffer = new BufferedReader(new FileReader(coevolutionFile));
    buffer.readLine();
    buffer.readLine();
    String textline = null;
    while ((textline = buffer.readLine()) != null) {
    String[] split = textline.split("(\t)+");
    int i = Integer.parseInt(split[0]);
    int j = Integer.parseInt(split[1]);
    if (split.length >= 11 && !split[4].equals("I") && !split[5].equals("I") && !split[4].equals("NA") && !split[5].equals("NA")) {
    double dlogl = Double.parseDouble(split[4]);
    double pval = Double.parseDouble(split[5]);
    double rgained = Double.parseDouble(split[7]);

    if (rgained <= 1) {
    if (pvals) {
    coevolutionMatrix.set(i, j, pval);
    } else {
    coevolutionMatrix.set(i, j, dlogl);
    }
    } else {
    if (pvals) {
    coevolutionMatrix.set(i, j, pval);
    } else {
    coevolutionMatrix.set(i, j, dlogl);
    }
    }
    }

    }
    } catch (IOException ex) {
    ex.printStackTrace();
    }
    }*/
    public static void readCoevolutionIntoMatrix(File coevolutionFile, SparseMatrix coevolutionMatrix, int parameter) {
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(coevolutionFile));
            if (!coevolutionFile.getName().endsWith(".txt")) {
                return;
            }
            buffer.readLine();
            buffer.readLine();
            String textline = null;
            while ((textline = buffer.readLine()) != null) {
                String[] split = textline.split("(\t)+");
                int i = Integer.parseInt(split[0]);
                int j = Integer.parseInt(split[1]);
                if (split.length >= 11 && !split[4].equals("I") && !split[5].equals("I")) {
                    if (!split[4].equals("NA") && !split[5].equals("NA")) {
                        double dlogl = Double.parseDouble(split[4]);
                        double pval = Double.parseDouble(split[5]);
                        double rgained = Double.parseDouble(split[7]);

                        switch (parameter) {
                            case 0:
                                coevolutionMatrix.set(i, j, pval);
                                break;
                            case 1:
                                coevolutionMatrix.set(i, j, dlogl);
                                break;
                            case 2:
                                coevolutionMatrix.set(i, j, rgained);
                                break;
                            case 3:
                                if (rgained > 1) {
                                    coevolutionMatrix.set(i, j, pval);
                                }
                                break;
                            case 4:
                                if (rgained < 1) {
                                    coevolutionMatrix.set(i, j, pval);
                                }
                                break;

                        }
                    } else {
                        double dlogl = Double.parseDouble(split[4]);
                        double rgained = Double.parseDouble(split[7]);
                        switch (parameter) {
                            case 0:
                                coevolutionMatrix.set(i, j, 1.0);
                                break;
                            case 1:
                                coevolutionMatrix.set(i, j, dlogl);
                                break;
                            case 2:
                                coevolutionMatrix.set(i, j, rgained);
                                break;
                        }
                    }
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /* public static SparseMatrix getMatrixAMinusB(SparseMatrix matrixA, SparseMatrix matrixB) {
    SparseMatrix ret = new SparseMatrix(matrixA.n, matrixA.emptyValue);
    MatrixIterator it = matrixA.getMatrixIterator();
    while (it.hasNext()) {
    Index2D index = it.next();
    int i = index.i;
    int j = index.j;
    if (matrixA.get(i, j) != matrixA.emptyValue && matrixB.get(i, j) == matrixB.emptyValue) {
    ret.set(i, j, matrixA.get(i, j));
    }
    }

    return ret;
    }

    public static SparseMatrix reverse(SparseMatrix matrix, int length) {
    SparseMatrix ret = new SparseMatrix(length, matrix.emptyValue);
    MatrixIterator it = matrix.getMatrixIterator();
    while (it.hasNext()) {
    Index2D index = it.next();
    int i = index.i;
    int j = index.j;
    if (matrix.get(i, j) != matrix.emptyValue) {
    ret.set(length-i-1, length-j-1, matrix.get(i, j));
    }
    }
    return ret;
    }

    public static SparseMatrix getMatrixAMinusBSpecial(SparseMatrix matrixA, SparseMatrix matrixB) {
    SparseMatrix ret = new SparseMatrix(matrixA.n, matrixA.emptyValue);
    MatrixIterator it = matrixA.getMatrixIterator();
    while (it.hasNext()) {
    Index2D index = it.next();
    int i = index.i;
    int j = index.j;
    if (matrixA.get(i, j) != matrixA.emptyValue) {
    if (matrixB.get(i, j) == matrixB.emptyValue) {
    ret.set(i, j, matrixA.get(i, j));
    } else {
    ret.set(i, j, -1);
    }
    }
    }

    return ret;
    }*/
}
