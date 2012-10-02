/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Michael Golden
 */
public class CollateData {

    public static final double emptyVal = -1000;

    public static void main(String[] args) {
        //new CollateData();
        //double [] fa = {0.25,0.25,0.25,0.25};
        double[] fa = {0.6, 0.1, 0, 0.3};
        double[] ha = getSequenceLogo(fa, 10);
        System.out.println(ha[0] + "\t" + ha[1] + "\t" + ha[2] + "\t" + ha[3] + "\t");

    }

    public static void impute(ArrayList<String> sequences, int end) {
        double[][] distanceMatrix = getDistanceMatrix(sequences);

        for (int i = 0; (i < end || end == -1) && i < sequences.size(); i++) {
            for (int pos = 0; pos < sequences.get(i).length(); pos++) {
                double minDistance = -1;
                int closestSeq = -1;
                if (sequences.get(i).charAt(pos) == '-') {
                    for (int j = 0; j < sequences.size(); j++) {
                        if (pos < sequences.get(j).length() && sequences.get(j).charAt(pos) != '-') {
                            if (closestSeq == -1 || distanceMatrix[i][j] < minDistance) {
                                minDistance = distanceMatrix[i][j];
                                closestSeq = j;
                            }
                        }
                    }
                    StringBuffer buf = new StringBuffer(sequences.get(i));
                    if (closestSeq != -1) {
                        buf.setCharAt(pos, sequences.get(closestSeq).charAt(pos));
                        sequences.set(i, buf.toString());
                    }
                }
            }
        }
    }

    public static double[] getWeights(ArrayList<String> sequences) {
        double[][] distanceMatrix = getDistanceMatrix(sequences);
        double[] weights = new double[sequences.size()];
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights.length; j++) {
                weights[i] += distanceMatrix[i][j];
            }
            weights[i] /= weights.length;

        }
        return weights;
    }

    public static double[][] getNucleotideComposition(ArrayList<String> sequences, double[] weights) {
        int seqLength = sequences.get(0).length();
        double[][] nucleotideComposition = new double[seqLength][5];
        for (int i = 0; i < seqLength; i++) {
            for (int j = 0; j < sequences.size(); j++) {
                char c = sequences.get(j).charAt(i);
                switch (c) {
                    case 'A':
                        nucleotideComposition[i][0] += weights[j];
                        break;
                    case 'C':
                        nucleotideComposition[i][1] += weights[j];
                        break;
                    case 'G':
                        nucleotideComposition[i][2] += weights[j];
                        break;
                    case 'T':
                        nucleotideComposition[i][3] += weights[j];
                        break;
                    default:
                        nucleotideComposition[i][4] += weights[j];
                        break;
                }
            }
        }

        for (int i = 0; i < seqLength; i++) {
            double gapsum = 0;
            double sum = 0;
            for (int j = 0; j < 5; j++) {
                gapsum += nucleotideComposition[i][j];
                if (j != 4) {
                    sum += nucleotideComposition[i][j];
                }
            }

            String s = i + "\t";
            for (int j = 0; j < 5; j++) {
                s += (nucleotideComposition[i][j] / gapsum) + "\t";
            }
            for (int j = 0; j < 5; j++) {
                s += (nucleotideComposition[i][j] / sum) + "\t";
            }
        }

        return nucleotideComposition;
    }

    public static double[] getSequenceLogoAtI(ArrayList<String> sequences, int i, double[] weights) {
        int n = 0;
        for (int j = 0; j < sequences.size(); j++) {
            if (sequences.get(j).charAt(i) != '-') {
                n++;
            }
        }
        double fa[] = getFrequenciesAtI(sequences, i, weights);

        return getSequenceLogo(fa, n);
    }

    public static double[] getFrequenciesAtI(ArrayList<String> sequences, int i, double[] weights) {
        double[] fa = new double[4];

        for (int j = 0; j < sequences.size(); j++) {
            char c = sequences.get(j).charAt(i);
            switch (c) {
                case 'A':
                    fa[0] += weights[j];
                    break;
                case 'C':
                    fa[1] += weights[j];
                    break;
                case 'G':
                    fa[2] += weights[j];
                    break;
                case 'T':
                    fa[3] += weights[j];
                    break;
            }
        }

        double t = fa[0] + fa[1] + fa[2] + fa[3];
        for (int k = 0; k < 4; k++) {
            fa[k] = fa[k] / t;
        }

        return fa;
    }

    public static double[] getSequenceLogo(double[] fa, int n) {
        double[] ha = new double[4];

        double Hi = 0;
        double en = 3.0 / (2 * Math.log(2) * n);
        en = 0;
        for (int a = 0; a < 4; a++) {
            double log2fa = Math.log(fa[a]) / Math.log(2);

            if (fa[a] == 0) {
                Hi += 0;
            } else {
                Hi += -(log2fa * fa[a]);
            }
        }

        double Ri = 2 - (Hi + en);
        for (int a = 0; a < 4; a++) {
            ha[a] = fa[a] * Ri;
        }

        return ha;
    }

    public static double[][] getDistanceMatrix(ArrayList<String> sequences) {
        int len = sequences.size();
        double[][] distanceMatrix = new double[len][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                distanceMatrix[i][j] = distanceIgnoringGaps(sequences.get(i), sequences.get(j));
            }
        }

        return distanceMatrix;
    }

    public static int distanceIgnoringGaps(String seq1, String seq2) {
        int dist = 0;
        int length = Math.min(seq1.length(), seq2.length());
        for (int i = 0; i < length; i++) {
            if (seq1.charAt(i) != '-' && seq2.charAt(i) != '-' && seq1.charAt(i) != seq2.charAt(i)) {
                dist += 1;
            }
        }

        return dist;
    }

    public static void loadFastaSequences(File file, ArrayList<String> sequences, ArrayList<String> sequenceNames) {
        int newMaxSequenceLength = 0;
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(file));
            String textline = null;
            String sequence = "";
            while ((textline = buffer.readLine()) != null) {
                if (textline.startsWith(">")) {
                    sequenceNames.add(textline.substring(1));
                    if (!sequence.equals("")) {
                        sequences.add(sequence);
                        sequence = "";
                    }
                } else {
                    sequence += textline.trim();
                }
            }
            buffer.close();
            if (!sequence.equals("")) {
                newMaxSequenceLength = Math.max(newMaxSequenceLength, sequence.length());
                sequences.add(sequence);
            }
        } catch (IOException ex) {
        }
    }

    public static SparseMatrix getNaspMatrix(File naspMatrixFile) {
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(naspMatrixFile));
            int n = buffer.readLine().split(",").length - 1;
            SparseMatrix naspMatrix = new SparseMatrix(n, emptyVal);
            String textline = null;
            for (int i = 0; (textline = buffer.readLine()) != null; i++) {
                String[] split = textline.split(",");
                for (int j = 1; j < n && j < split.length; j++) {
                    double nasp = Double.parseDouble(split[j]);
                    if (nasp != 0) {
                        naspMatrix.set(i, j - 1, nasp);
                    }
                }
            }
            buffer.close();
            return naspMatrix;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ArrayList<File> recursivelyListFiles(File dir) {
        ArrayList<File> files = new ArrayList<File>();
        recursivelyListFiles(dir, files);
        return files;
    }

    public static void recursivelyListFiles(File dir, ArrayList<File> files) {
        File[] list = dir.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                files.add(list[i]);
            } else if (list[i].isDirectory()) {
                recursivelyListFiles(list[i], files);
            }
        }
    }

    public static SparseMatrix getCoevolutionMatrix(File coevolutionDir, boolean pvals) {
        int n = 12000;
        SparseMatrix coevolutionMatrix = new SparseMatrix(n, emptyVal);
        ArrayList<File> files = recursivelyListFiles(coevolutionDir);
        for (int i = 0; i < files.size(); i++) {
            readCoevolutionIntoMatrix(files.get(i), coevolutionMatrix, pvals);
        }
        return coevolutionMatrix;
    }

    public static SparseMatrix readCoevolutionIntoMatrix2(File coevolutionFile) {
        SparseMatrix coevolutionMatrix = new SparseMatrix(12000, emptyVal);

        try {
            BufferedReader buffer = new BufferedReader(new FileReader(coevolutionFile));
            buffer.readLine();
            buffer.readLine();
            String textline = null;
            while ((textline = buffer.readLine()) != null) {
                String[] split = textline.split("(\t)+");
                int i = Integer.parseInt(split[0]);
                int j = Integer.parseInt(split[1]);
                if (!split[4].equals("I") && !split[5].equals("I") && !split[4].equals("NA") && !split[5].equals("NA")) {
                    // double dlogl = Double.parseDouble(split[4]);
                    double pval = Double.parseDouble(split[4]);
                    coevolutionMatrix.set(i, j, pval);
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return coevolutionMatrix;
    }

    public static void readCoevolutionIntoMatrix(File coevolutionFile, SparseMatrix coevolutionMatrix, boolean pvals) {
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(coevolutionFile));
            buffer.readLine();
            buffer.readLine();
            String textline = null;
            while ((textline = buffer.readLine()) != null) {
                String[] split = textline.split("(\t)+");
                int i = Integer.parseInt(split[0]);
                int j = Integer.parseInt(split[1]);
                if (split.length >= 6 && !split[4].equals("I") && !split[5].equals("I") && !split[4].equals("NA") && !split[5].equals("NA")) {
                    double dlogl = Double.parseDouble(split[4]);
                    double pval = Double.parseDouble(split[5]);

                    if (pvals) {
                        coevolutionMatrix.set(i, j, pval);
                    } else {
                        coevolutionMatrix.set(i, j, dlogl);
                    }
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static int[][] readNaspCtFile(File naspCtFile) {
        int[][] pairs = {};
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(naspCtFile));
            String textline = buffer.readLine();
            String[] split = textline.split("\t");
            int len = Integer.parseInt(split[0]);
            pairs = new int[2][len];
            for (int i = 0; (textline = buffer.readLine()) != null; i++) {
                split = textline.split("\t");
                pairs[0][i] = Integer.parseInt(split[5]);
                pairs[1][i] = Integer.parseInt(split[4]);
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return pairs;
    }

    /*public static ArrayList<NaspStructure> loadNaspStructures(File naspFile) {
        ArrayList<NaspStructure> naspStructures = new ArrayList<NaspStructure>();

        try {
            BufferedReader buffer = new BufferedReader(new FileReader(naspFile));
            String textline = null;
            boolean read = false;
            while ((textline = buffer.readLine()) != null) {
                if (textline.startsWith("1:")) {
                    read = true;
                }
                if (read) {
                    if (!textline.startsWith("---")) {
                        naspStructures.add(new NaspStructure(textline));
                    } else {
                        break;
                    }
                }
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return naspStructures;
    }

    public ArrayList<AnnotatedStructure> loadStructures() {
        ArrayList<AnnotatedStructure> annotatedStructures = new ArrayList<AnnotatedStructure>();

        //FileSpecifier fs = new FileSpecifier(new File("C:/project/dengue/dengue.txt"), "dengue");
         FileSpecifier fs = new FileSpecifier(new File("C:/project/hepacivirus/hepacivirus_wobble.txt"), "hepacivirus_wobble");
         // FileSpecifier fs = new FileSpecifier(new File("F:/Nasp/PCV/pcv.txt"), "pcv");
       // FileSpecifier fs = new FileSpecifier(new File("C:/project/hepacivirus/hepacivirus.txt"), "hepacivirus");
        // FileSpecifier fs = new FileSpecifier(new File("C:/project/norovirus/norovirus.txt"), "norovirus");
        File covariation2File = new File("C:/Users/Michael/workspace/Coevolution/src/hepaci_custom_results2.txt");


        ArrayList<String> coevolutionSequences = new ArrayList<String>();
        ArrayList<String> coevolutionSequenceNames = new ArrayList<String>();
        Mapping naspAndCoevolutionMapping = null;
        if (fs.coevolutionAlignmentFile != null) {
            IO.loadFastaSequences(fs.coevolutionAlignmentFile, coevolutionSequences, coevolutionSequenceNames);
            Mapping.setAlignmentParameters(-12, -1, 1);
            naspAndCoevolutionMapping = new Mapping(fs.naspAlignmentFile, fs.coevolutionAlignmentFile);
        }

        ArrayList<NaspStructure> naspStructures = loadNaspStructures(fs.naspStructuresFile);

        SparseMatrix naspMatrix = getNaspMatrix(fs.naspMatrixFile);
        SparseMatrix coevolutionMatrixPvals = new SparseMatrix(0, -1000);
        SparseMatrix coevolutionMatrixRgained = new SparseMatrix(0, -1000);
        SparseMatrix coevolutionMatrix2 = new SparseMatrix(0, -1000);
        if (fs.coevolutionDir != null) {
            coevolutionMatrixPvals = CompareCoevolution.getCoevolutionMatrix(fs.coevolutionDir, 0);
            coevolutionMatrixRgained = CompareCoevolution.getCoevolutionMatrix(fs.coevolutionDir, 2);
            coevolutionMatrix2 = readCoevolutionIntoMatrix2(covariation2File);
        }

        //ParrisData parrisData = new ParrisData(fs.parrisMarginalsFiles.get(0));
        //Mapping naspAndGeneMapping = new Mapping(fs.naspAlignmentFile, fs.geneAlignmentFiles.get(0));
        Mapping.setAlignmentParameters(-500, -1, 2);
        ArrayList<Mapping> naspAndGeneMapping = new ArrayList<Mapping>();
        ArrayList<ParrisData> parrisData = new ArrayList<ParrisData>();
        for (int i = 0; i < fs.geneAlignmentFiles.size(); i++) {
            Mapping mapping = new Mapping(fs.naspAlignmentFile, fs.geneAlignmentFiles.get(i));
            naspAndGeneMapping.add(mapping);
            parrisData.add(new ParrisData(fs.parrisMarginalsFiles.get(i)));
            parrisData.get(i).normaliseSynonymous(2.5); // normalise maximum to 2.5 for readibility
        }

        if(fs.genbankReferenceFile != null)
        {
            GenomeStructureParser.saveGenomeStructure(GenomeStructureParser.getGenomeMappedToNASPAlignment(fs), new File(fs.collateDir.getPath() + "/genomestructure.gs"));
        }
        

        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(new File(fs.collateDir.getPath() + "/synonymous.ds")));

            for (int i = 0; i <= naspAndGeneMapping.get(0).getALength(); i++) {
                double dS = -1;
                for (int j = 0; j < naspAndGeneMapping.size(); j++) {
                    int x = naspAndGeneMapping.get(j).aToB(i - 1) / 3;
                    if (naspAndGeneMapping.get(j).aToB(i - 1) != -1) {
                        dS = parrisData.get(j).weightedSynonymous[x];
                    }
                }
                buffer.write(i + "\t" + dS);
                buffer.newLine();
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (int s = 0 ; s < naspStructures.size(); s++) {
            NaspStructure naspStructure = naspStructures.get(s);
            AnnotatedStructure structure = new AnnotatedStructure(naspStructure);
            // coevolution
            for (int i = structure.gapStartA; i <= structure.gapEndB; i++) {
                for (int j = structure.gapStartA; j <= structure.gapEndB; j++) {
                    if (naspAndCoevolutionMapping != null) {
                        int x = naspAndCoevolutionMapping.aToB(i - 1);
                        int y = naspAndCoevolutionMapping.aToB(j - 1);
                        double pval = coevolutionMatrixPvals.get(x, y);
                        double rgained = coevolutionMatrixRgained.get(x, y);
                        if (pval != -1000) {
                            structure.setCoevolutionPval(i, j, pval);
                            structure.setCoevolutionRgained(i, j, rgained);
                        }
                        double pval2 = coevolutionMatrix2.get(x, y);
                        if (pval != -1000) {
                            structure.setCovariation2(i, j, pval2);
                        }
                        double nasp = naspMatrix.get(i - 1, j - 1);
                        if (nasp != 0) {
                            structure.setNasp(i, j, nasp);
                        }
                    }
                }
            }

            // nasp
            for (int j = 0; j < naspAndGeneMapping.size(); j++) {
                for (int i = structure.gapStartA; i <= structure.gapEndB; i++) {
                    int x = naspAndGeneMapping.get(j).aToB(i - 1) / 3;
                    if (naspAndGeneMapping.get(j).aToB(i - 1) != -1) {
                        double dS = parrisData.get(j).weightedSynonymous[x];
                        structure.parrisScores.put(i + "", dS);
                    }
                }
            }

            if (naspAndCoevolutionMapping != null) {
                double[] weights = getWeights(coevolutionSequences);
                for (int i = structure.gapStartA; i <= structure.gapEndB; i++) {
                    int x = naspAndCoevolutionMapping.aToB(i - 1);
                    if (x >= 0) {
                        double[] freq = getFrequenciesAtI(coevolutionSequences, x, weights);
                        double[] his = getSequenceLogoAtI(coevolutionSequences, x, weights);
                        structure.setFrequencies(i, freq);
                        structure.setHis(i, his);
                    }
                }
            }

            DecimalFormat df = new DecimalFormat("000");
            structure.saveStructure(new File(fs.collateDir.getPath() + "/s" + df.format(structure.id) + ".str"));
        }

        return annotatedStructures;
    }*/
}
