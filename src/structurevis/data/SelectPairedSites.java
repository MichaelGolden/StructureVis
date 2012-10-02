/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.data;

import java.io.File;
import java.util.ArrayList;
import structurevis.structures.Structure;
import structurevis.structures.StructureParser;

/**
 *
 * @author Michael Golden
 */
public class SelectPairedSites {

    public static void main(String[] args) {
        try
        {
        File naspAlignment = new File("C:/project/hepacivirus/10seq_aligned_d0.fasta");
        File naspCtFile = new File("C:/project/hepacivirus/10seq_aligned_d0.fasta.ct");
        File coevolutionAlignment = new File("C:/project/hepacivirus/datamonkey_aligned_trim.fas");

        Mapping m = Mapping.createMapping(naspAlignment, coevolutionAlignment, 1, false, "selectmapping");
        ArrayList<String> sequences = new ArrayList<String>();
        ArrayList<String> sequenceNames = new ArrayList<String>();
        IO.loadFastaSequences(coevolutionAlignment, sequences, sequenceNames);
        Structure s = StructureParser.parseNaspCtFile(naspCtFile);
        int[] paired = new int[sequences.get(0).length()];
        for (int i = 0; i < paired.length; i++) {
            System.out.print(i+"\t");
            int x = m.bToA(i);
            if (x != -1) {
                if (s.pairedSites[1][x] > 0) {
                    paired[i] = x+1;
                    System.out.print("Paired");
                } else {
                    paired[i] = -x-1;
                    System.out.print("Unpaired");
                }
            }
            System.out.println();
        }

        ArrayList<String> pairedSequences = new ArrayList<String>();
        ArrayList<String> unpairedSequences = new ArrayList<String>();
        for (int j = 0; j < sequences.size(); j++) {
            String pairedSequence = "";
            String unpairedSequence = "";
            for (int i = 0; i < paired.length; i++) {
                if (paired[i] >= 1) {
                    pairedSequence += sequences.get(j).charAt(i);
                    //System.out.println(sequences.get(j).charAt(i)+"\t"+paired[i]+"\t"+i);
                } else if (paired[i] <= -1) {
                    unpairedSequence += sequences.get(j).charAt(i);
                }
            }
            pairedSequences.add(pairedSequence);
            unpairedSequences.add(unpairedSequence);

            //break;
        }

        IO.saveToFASTAfile(pairedSequences, sequenceNames, new File("paired.fasta"));
        IO.saveToFASTAfile(unpairedSequences, sequenceNames, new File("unpaired.fasta"));
        }
        catch(Exception ex)
        {
            
        }
    }
}
