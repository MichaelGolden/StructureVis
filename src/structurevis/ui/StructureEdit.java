/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

import java.util.Arrays;

/**
 *
 * @author Michael Golden <michaelgolden0@gmail.com>
 */
public class StructureEdit {

    int[] originalPairedSites;
    int[] pairedSites;

    public StructureEdit(int[] pairedSites) {
        this.originalPairedSites = Arrays.copyOf(pairedSites, pairedSites.length);
        this.pairedSites = Arrays.copyOf(pairedSites, pairedSites.length);
    }
    
    public void reset()
    {
        this.pairedSites = Arrays.copyOf(originalPairedSites, originalPairedSites.length);
    }

    public void makeSingleStranded(int i) {
        int y = pairedSites[i] - 1;

        pairedSites[i] = 0;
        if (y >= 0) {
            pairedSites[y] = 0;
        }
    }

    public boolean canMakeBasePair(int a, int b) {
        int i = a;
        int j = b;
        if(a > b)
        {
            i = b;
            j = a;
        }
        
        for (int x = i + 1; x < j; x++) {
            int y = pairedSites[x]-1;
            if (y >= 0) {
                if (y < i || y > j) {
                    return false;
                }
            }
        }

        return true;
    }

    public void makeBasePair(int i, int j) {
        if (canMakeBasePair(i, j)) {
            makeSingleStranded(i);
            makeSingleStranded(j);
            pairedSites[i] = j + 1;
            pairedSites[j] = i + 1;
        } else {
            System.err.println("Cannot make base-pair " + i + ", " + j + ".");
        }
    }
    
    public boolean isBasePaired(int i, int j)
    {
        if(pairedSites[i] == j+1)
        {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String dbs = ".......((((((((.(....)))).(((((.............)))))..)))))....";
        StructureEdit edit = new StructureEdit(RNAFoldingTools.getPairedSitesFromDotBracketString(dbs));

        for (int i = 0; i < dbs.length(); i++) {
            System.out.print((i % 10));
        }
        System.out.println();

        System.out.println(RNAFoldingTools.getDotBracketStringFromPairedSites(edit.pairedSites));
        edit.makeBasePair(0, 2);
        System.out.println(RNAFoldingTools.getDotBracketStringFromPairedSites(edit.pairedSites));
        edit.makeBasePair(19, 31);
        System.out.println(RNAFoldingTools.getDotBracketStringFromPairedSites(edit.pairedSites));
        edit.makeBasePair(17, 20);
        System.out.println(RNAFoldingTools.getDotBracketStringFromPairedSites(edit.pairedSites));
        edit.makeBasePair(6, 56);
        System.out.println(RNAFoldingTools.getDotBracketStringFromPairedSites(edit.pairedSites));
    }
}
