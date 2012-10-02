package structurevis.structures.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import structurevis.data.Mapping;

/**
 *
 * @author Michael Golden
 */
public class SequenceData1D extends MetadataFromFile {

    public float[] data;
    public boolean[] used;
    public double min = Float.MAX_VALUE;
    public double max = Float.MIN_VALUE;
    /**
     * Codon data if true, otherwise nucleotide data.
     */
    public boolean codonData = false;

    private SequenceData1D() {
        //this.type = "SequenceData1D";
    }

    public SequenceData1D(String name) {
        this.name = name;
        //this.type = "SequenceData1D";
    }

    /**
     *
     * @param unmappedDataB
     * @param mapping
     * @param codonData if true, data is numbered according to the codon position (one-offset), otherwise it is numbered according to the nucleotide position (one-offset).
     * @return
     */
    public static SequenceData1D getMappedNucleotideData1D(float[] unmappedDataB, Mapping mapping, boolean codonData) {
        if (codonData) {
            SequenceData1D nd = new SequenceData1D();
            nd.data = new float[mapping.getALength()];
            for (int i = 0; i < unmappedDataB.length; i++) {
                for (int k = 0; k < 3; k++) {
                    int x = mapping.aToB(i * 3 + k);
                    if (x != -1) {
                        nd.data[i * 3 + k] = unmappedDataB[x];
                        nd.min = Math.min(nd.min, unmappedDataB[x]);
                        nd.max = Math.max(nd.max, unmappedDataB[x]);
                    }
                }
            }
            return nd;
        } else {
            SequenceData1D nd = new SequenceData1D();
            nd.data = new float[mapping.getALength()];
            for (int i = 0; i < unmappedDataB.length; i++) {
                int x = mapping.aToB(i);
                if (x != -1) {
                    nd.data[i] = unmappedDataB[x];
                    nd.min = Math.min(nd.min, unmappedDataB[x]);
                    nd.max = Math.max(nd.max, unmappedDataB[x]);
                }
            }
            return nd;
        }
    }

    /**
     *
     * @param csvFile
     * @param mapping
     * @param codonData if true, data is numbered according to the codon position (one-offset), otherwise it is numbered according to the nucleotide position (one-offset).
     * @parama dataColumn column in which the data is present (zero-offset).
     * @return
     */
    public static SequenceData1D loadFromCsv(File csvFile, Mapping mapping, boolean codonData, int posColumn, int dataColumn) {
        SequenceData1D nd = null;
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(csvFile));
            String textline = buffer.readLine();
            nd = new SequenceData1D(textline.split(",")[dataColumn]);
            nd.data = new float[mapping.getALength()];
            nd.used = new boolean[mapping.getALength()];
            while ((textline = buffer.readLine()) != null) {
                String[] split = textline.split(",");
                try {

                    if (posColumn < split.length && dataColumn < split.length) {
                        int i = Integer.parseInt(split[posColumn]) - 1;
                        float val = Float.parseFloat(split[dataColumn]);
                        if (codonData) {
                            for (int k = 0; k < 3; k++) {
                                int a = mapping.bToA(i * 3 + k);
                                if (a != -1) {
                                    nd.data[a] = val;
                                    nd.used[a] = true;
                                    nd.min = Math.min(nd.min, val);
                                    nd.max = Math.max(nd.max, val);
                                }
                            }
                        } else {
                            int a = mapping.bToA(i);
                            if (a != -1) {
                                nd.data[a] = val;
                                nd.used[a] = true;
                                nd.min = Math.min(nd.min, val);
                                nd.max = Math.max(nd.max, val);
                            }
                        }
                    }
                } catch (NumberFormatException ex) {
                }
            }
            buffer.close();
            /* for (int i = 0; i < nd.data.length; i++) {
            if (nd.used[i]) {
            nd.normalisedData[i] = (nd.data[i] - nd.min) / nd.max;
            }
            }*/
        } catch (IOException ex) {
            Logger.getLogger(SequenceData1D.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nd;
    }

    public static SequenceData1D combine(String name, ArrayList<SequenceData1D> data) {
        SequenceData1D combined = null;

        int length = data.get(0).data.length;
        for (int i = 1; i < data.size(); i++) {
            if (length != data.get(i).data.length) {
                throw new Error("SequenceData not of equal length - 'A' mapping not correct?");
            }
        }

        combined = new SequenceData1D(name);

        combined.data = new float[length];
        combined.used = new boolean[length];


        for (int i = 0; i < data.size(); i++) {
            SequenceData1D temp = data.get(i);
            for (int j = 0; j < length; j++) {
                if (temp.used[j]) {
                    combined.data[j] = temp.data[j];
                    combined.used[j] = true;
                }
            }
            combined.min = Math.min(combined.min, temp.min);
            combined.max = Math.max(combined.max, temp.max);
        }
        return combined;
    }

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
}
