package structurevis.structures.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import structurevis.data.IO;
import structurevis.data.Mapping;

/**
 *
 * @author Michael Golden
 */
public class NucleotideComposition extends MetadataFromFile {

    public String name;
    File fastaAlignment;
    Mapping mapping;
    ArrayList<String> sequences = new ArrayList<String>();
    ArrayList<String> sequenceNames = new ArrayList<String>();
    double[] weights;
    public int[] mappedNonGapCount;
    public double[][] mappedFrequencyComposition;
    public double[][] mappedShannonComposition;
    public String consensus;

    @Override
    public boolean canFree() {
        return false;
    }

    @Override
    public void load(File file, Mapping mapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void free() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public enum CompositionType {

        FREQUENCY, SHANNON_ENTROPY, CONSENSUS
    };
    //public CompositionType compositionType;

    public NucleotideComposition() {
    }

    public NucleotideComposition(File alignmentB, Mapping mapping) {
        this.fastaAlignment = alignmentB;
        this.mapping = mapping;

        IO.loadFastaSequences(alignmentB, sequences, sequenceNames);

        // getWeights() is very slow for large number of sequences!!!! problem is distance matrix calculation
        if (sequences.size() > 200) {
            System.out.println("::" + alignmentB);
            weights = new double[sequences.size()];
            Arrays.fill(weights, 1.0);
            System.out.println(weights.length);
        } else {
            weights = getWeights(sequences);
        }

        double[][] unbiasedFrequencyB = getFrequencyComposition(sequences, weights, false);
        mappedFrequencyComposition = new double[mapping.getALength()][4];
        for (int i = 0; i < mappedFrequencyComposition.length; i++) {
            int x = mapping.aToB(i);
            for (int n = 0; n < mappedFrequencyComposition[0].length; n++) {
                if (x != -1) {
                    mappedFrequencyComposition[i][n] = unbiasedFrequencyB[x][n];
                }
            }
        }

        double[][] shannnonEntropyB = getShannonEntropyComposition(sequences, weights);
        mappedShannonComposition = new double[mapping.getALength()][4];
        for (int i = 0; i < mappedShannonComposition.length; i++) {
            int x = mapping.aToB(i);
            for (int n = 0; n < mappedShannonComposition[0].length; n++) {
                if (x != -1) {
                    mappedShannonComposition[i][n] = shannnonEntropyB[x][n];
                }
            }
        }

        int[] unmappedNongapCharCount = getNumNonGappedCharacters(sequences);
        mappedNonGapCount = new int[mapping.getALength()];
        for (int i = 0; i < mappedNonGapCount.length; i++) {
            int x = mapping.aToB(i);
            if (x != -1) {
                mappedNonGapCount[i] = unmappedNongapCharCount[x];
            }
        }

        consensus = "";
        // determine consensus sequence
        for (int i = 0; i < mappedFrequencyComposition.length; i++) {
            int maxFrequencyIndex = 0;
            for (int n = 1; n < 4; n++) {
                if (mappedFrequencyComposition[i][n] > mappedFrequencyComposition[i][maxFrequencyIndex]) {
                    maxFrequencyIndex = n;
                }
            }

            if (mappedFrequencyComposition[i][maxFrequencyIndex] == 0) {
                consensus += "-";
            } else {
                switch (maxFrequencyIndex) {
                    case 0:
                        consensus += "A";
                        break;
                    case 1:
                        consensus += "C";
                        break;
                    case 2:
                        consensus += "G";
                        break;
                    case 3:
                        consensus += "T";
                        break;
                }
            }
        }
    }

    public static double[][] getFrequencyComposition(ArrayList<String> sequences, double[] weights, boolean includeGaps) {
        int seqLength = sequences.get(0).length();
        double[][] nucleotideComposition = new double[seqLength][5];
        for (int i = 0; i < seqLength; i++) {
            double sum = 0;
            double sumUngapped = 0;
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
                        sumUngapped -= weights[j];
                        break;
                }
                sum += weights[j];
                sumUngapped += weights[j];
            }

            if (includeGaps) {
                if(sum == 0)
                {
                    sum = 1;
                }
                for (int j = 0; j < 5; j++) {
                    nucleotideComposition[i][j] /= sum;
                }
            } else {
                if(sumUngapped == 0)
                {
                    sumUngapped = 1;
                }
                for (int j = 0; j < 5; j++) {
                    nucleotideComposition[i][j] /= sumUngapped;
                }
            }
        }

        return nucleotideComposition;
    }

    public static double[][] getShannonEntropyComposition(ArrayList<String> sequences, double[] weights) {
        int seqLength = sequences.get(0).length();
        double[][] nucleotideComposition = new double[seqLength][4];
        for (int i = 0; i < seqLength; i++) {
            nucleotideComposition[i] = getShannonEntropyAtI(sequences, i, weights);
        }
        return nucleotideComposition;
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

    public static int[] getNumNonGappedCharacters(ArrayList<String> sequences) {
        int[] nonGappedCharCount = new int[sequences.get(0).length()];
        for (int i = 0; i < nonGappedCharCount.length; i++) {
            for (int j = 0; j < sequences.size(); j++) {
                if (sequences.get(j).charAt(i) != '-') {
                    nonGappedCharCount[i]++;
                }
            }
        }
        return nonGappedCharCount;
    }

    public static double[] getShannonEntropyAtI(ArrayList<String> sequences, int i, double[] weights) {
        int n = 0;
        for (int j = 0; j < sequences.size(); j++) {
            if (sequences.get(j).charAt(i) != '-') {
                n++;
            }
        }
        double fa[] = getFrequenciesAtI(sequences, i, weights);

        return getShannonEntropy(fa, n);
    }

    public static double[] getShannonEntropy(double[] fa, int n) {
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

    public static double[] getWeights(ArrayList<String> sequences) {
        if (sequences.size() == 1) {
            double[] weights = new double[sequences.size()];
            Arrays.fill(weights, 1);
            return weights;
        } else {
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
    }

    public static void main(String[] args) {
        //Mapping m = Mapping.createMapping(new File("d:/Nasp/PCV/PCV_10seq.fasta"), new File("d:/Nasp/PCV/PCV29sequencedatasetCP.fasta"), 2, true);
        //NucleotideComposition c = new NucleotideComposition(new File("d:/Nasp/PCV/PCV29sequencedatasetCP.fasta"), m);
    }

    public String getType() {
        return "NucleotideComposition";
    }

    public void writeToXMLStream(XMLStreamWriter writer) throws XMLStreamException {
        try {
            writer.writeStartElement("file");
            writer.writeAttribute("fasta-file", fastaAlignment.getPath());
            if (mapping.mappingFile != null) {
                writer.writeAttribute("mapping-file", mapping.mappingFile.getPath());
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
        } catch (XMLStreamException ex) {
            Logger.getLogger(NucleotideComposition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
