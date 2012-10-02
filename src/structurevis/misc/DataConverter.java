/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.misc;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import structurevis.data.IO;
import structurevis.data.SparseMatrix;
import structurevis.data.SparseMatrix.Index2D;
import structurevis.data.SparseMatrix.MatrixIterator;
import structurevis.structures.metadata.NucleotideComposition;

/**
 *
 * @author Michael
 */
public class DataConverter {
    
    Hashtable<Integer, File> fileTable = new Hashtable<Integer, File>();
    
    public DataConverter(File dir)
    {
        fileTable = getFileListing(dir, ".fas");
    }

    public static void convertSpidermonkeyMatrixToPairwise2DDelimitted(File input, File output, double pvalLessThanOrEqual, double rgainedGreaterThanOrEqual, double rgainedLessThanOrEqual, boolean printPvals) {
        String delimitter = "\t";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            String textline = null;
            while ((textline = reader.readLine()) != null) {
                String[] split = textline.split("\\s+");
                int i = Integer.parseInt(split[0]);
                int j = Integer.parseInt(split[1]);
                double pval = Double.parseDouble(split[4]);
                double rgained = Double.parseDouble(split[5]);

                if (pval <= pvalLessThanOrEqual && rgained <= rgainedLessThanOrEqual && rgained >= rgainedGreaterThanOrEqual) {
                    if (printPvals) {
                        writer.write(i + delimitter + j + delimitter + pval + "\n");
                    } else {
                        writer.write(i + delimitter + j + delimitter + rgained + "\n");
                    }
                }
            }
            writer.close();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void filterValuesOnGaps(File tabDelimittedMatrixIn, File tabDelimittedMatrixOut, double percentNonGapsCutOff) {
        SparseMatrix in = SparseMatrix.loadSparseMatrixFromPairwiseFile(tabDelimittedMatrixIn, "\t");
        SparseMatrix out = new SparseMatrix(in.m, in.n, in.emptyValue);
        MatrixIterator it = in.getMatrixIterator();
        while (it.hasNext()) {
            Index2D index = it.next();
            double percNonGapsAtI = getPercentNonGaps(index.i);
            double percNonGapsAtJ = getPercentNonGaps(index.j);
            if(percNonGapsAtI >= percentNonGapsCutOff && percNonGapsAtJ >= percentNonGapsCutOff)
            {
                out.set(index.i, index.j, in.get(index.i, index.j));
            }
        }
        out.saveSparseMatrixToFile(tabDelimittedMatrixOut);
    }
    
    public void filterValuesOnConservation(File tabDelimittedMatrixIn, File tabDelimittedMatrixOut, double percentNonGapsCutOff) {
        SparseMatrix in = SparseMatrix.loadSparseMatrixFromPairwiseFile(tabDelimittedMatrixIn, "\t");
        SparseMatrix out = new SparseMatrix(in.m, in.n, in.emptyValue);
        MatrixIterator it = in.getMatrixIterator();
        while (it.hasNext()) {
            Index2D index = it.next();
            double percNonGapsAtI = getConservation(index.i) / 2;
            double percNonGapsAtJ = getConservation(index.j) / 2;
            if(percNonGapsAtI <= percentNonGapsCutOff && percNonGapsAtJ <= percentNonGapsCutOff)
            {
                out.set(index.i, index.j, in.get(index.i, index.j));
            }
        }
        out.saveSparseMatrixToFile(tabDelimittedMatrixOut);
    }

    
    Hashtable<Integer, Double> percentNonGapsTable = new Hashtable<Integer, Double>();
    public double getPercentNonGaps(int pos) {
        Double percentNonGaps = percentNonGapsTable.get(pos);
        if(percentNonGaps != null )
        {
            return percentNonGaps.doubleValue();
        }
        
        File sequenceFile = fileTable.get(pos);
        if (sequenceFile != null) {
            int start = Integer.parseInt(sequenceFile.getName().split("_")[1]);
            int posInFile = pos - start;
            if (posInFile >= 0) {
                ArrayList<String> sequences = new ArrayList<String>();
                ArrayList<String> sequenceNames = new ArrayList<String>();
                IO.loadFastaSequences(sequenceFile, sequences, sequenceNames);
                int [] ungapped = NucleotideComposition.getNumNonGappedCharacters(sequences);
                if(posInFile < ungapped.length)
                {
                    double nonGapPercent = (double) ungapped[posInFile] / (double) sequences.size();
                    //System.out.println(pos +"\t" + nonGapPercent);
                    percentNonGapsTable.put(pos, nonGapPercent);
                    return nonGapPercent;
                }
            }
        }

        return -1;
    }
    
    Hashtable<Integer, Double> conservationTable = new Hashtable<Integer, Double>();
    public double getConservation(int pos) {
        Double conservationVal = conservationTable.get(pos);
        if(conservationVal != null )
        {
            return conservationVal.doubleValue();
        }
        
        File sequenceFile = fileTable.get(pos);
        if (sequenceFile != null) {
            int start = Integer.parseInt(sequenceFile.getName().split("_")[1]);
            int posInFile = pos - start;
            if (posInFile >= 0) {
                ArrayList<String> sequences = new ArrayList<String>();
                ArrayList<String> sequenceNames = new ArrayList<String>();
                IO.loadFastaSequences(sequenceFile, sequences, sequenceNames);                
                if(posInFile < sequences.get(0).length())
                {                    
                    double [] weights = new double[sequences.size()];
                    Arrays.fill(weights, 1);                
                    double [] fa = NucleotideComposition.getShannonEntropyAtI(sequences, posInFile, weights);
                    double conservationValue = fa[0];
                    for(int i = 1 ; i < 4 ; i++)
                    {
                        conservationValue = Math.max(conservationValue, fa[i]);
                    }
                    conservationTable.put(pos, conservationValue);
                    return conservationValue;
                }
            }
        }

        return -1;
    }

    public static Hashtable<Integer, File> getFileListing(File dir, String extension) {
        Hashtable<Integer, File> fileTable = new Hashtable<Integer, File>();
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.endsWith(".fas")) {
                name = name.replaceAll("\\.fas$", "");
                String[] split = name.split("_");
                if (split[0].equals("id")) {
                    int start = Integer.parseInt(split[1]);
                    int end = Integer.parseInt(split[2]);
                    for (int j = start; j < end; j++) {
                        fileTable.put(j, files[i]);
                    }
                }
            }
        }
        return fileTable;
    }

    public static void main(String[] args) {
        File inputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enterob.txt");
        File outputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enterob_svis.txt");
        //DataConverter dataConverter = new DataConverter(null);
        convertSpidermonkeyMatrixToPairwise2DDelimitted(inputFile, outputFile, 1, 1, Double.MAX_VALUE, true);
        
        inputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enterob.txt");
        outputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enterob_svis_avoidance.txt");
        //DataConverter dataConverter = new DataConverter(null);
        convertSpidermonkeyMatrixToPairwise2DDelimitted(inputFile, outputFile, 1, 0, 1, true);
        
        inputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroa.txt");
        outputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroa_svis.txt");
        //DataConverter dataConverter = new DataConverter(null);
        convertSpidermonkeyMatrixToPairwise2DDelimitted(inputFile, outputFile, 1, 1, Double.MAX_VALUE, true);
        
        inputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroa.txt");
        outputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroa_svis_avoidance.txt");
        //DataConverter dataConverter = new DataConverter(null);
        convertSpidermonkeyMatrixToPairwise2DDelimitted(inputFile, outputFile, 1, 0, 1, true);
        
        
        inputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroc.txt");
        outputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroc_svis.txt");
        //DataConverter dataConverter = new DataConverter(null);
        convertSpidermonkeyMatrixToPairwise2DDelimitted(inputFile, outputFile, 1, 1, Double.MAX_VALUE, true);
        
        inputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroc.txt");
        outputFile = new File("C:/Users/Michael/Dropbox/BrejAndMichael/Emil/enteroc_svis_avoidance.txt");
        //DataConverter dataConverter = new DataConverter(null);
        convertSpidermonkeyMatrixToPairwise2DDelimitted(inputFile, outputFile, 1, 0, 1, true);
        
        /*
        File dir = new File("C:/Users/Michael/Documents/NetBeansProjects/SequenceTools/hiv2010-500-seperated-500-250-raxml");
        File inputFile = new File("C:/Users/Michael/Documents/all.txt");
        File outputFile = new File("C:/Users/Michael/Documents/all.matrix");
        DataConverter dataConverter = new DataConverter(dir);
        convertSpidermonkeyMatrixToPairwise2DDelimitted(inputFile, outputFile, 1, 1, Double.MAX_VALUE, true);        
        //dataConverter.filterValuesOnGaps(outputFile, new File("C:/Users/Michael/Documents/filtered50.matrix"), 0.50);
        //dataConverter.filterValuesOnGaps(outputFile, new File("C:/Users/Michael/Documents/filtered80.matrix"), 0.80);
        //dataConverter.filterValuesOnGaps(outputFile, new File("C:/Users/Michael/Documents/filtered90.matrix"), 0.9);
        //dataConverter.filterValuesOnGaps(outputFile, new File("C:/Users/Michael/Documents/filtered95.matrix"), 0.95);
        //dataConverter.filterValuesOnGaps(outputFile, new File("C:/Users/Michael/Documents/filtered98.matrix"), 0.98);
        dataConverter.filterValuesOnGaps(outputFile, new File("C:/Users/Michael/Documents/allm.matrix"), 0);
        //dataConverter.filterValuesOnGaps(outputFile, new File("C:/Users/Michael/Documents/filtered80.matrix"), 0.80);
        //dataConverter.filterValuesOnConservation(new File("C:/Users/Michael/Documents/filtered80.matrix"), new File("C:/Users/Michael/Documents/filtered80conservation80coevolution.matrix"), 0.8);
        */
        
    }
}
