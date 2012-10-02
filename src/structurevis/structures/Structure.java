package structurevis.structures;

import java.io.File;

/**
 * Class that represents a secondary structure and metadata.
 *
 * @author Michael Golden
 */
public class Structure {

    /**
     * 2 x genomeLength array of paired nucleotide positions. pairedSites[0][i] = nucleotidePosition of nucleotide i, pairedSites[1][i] = nucleotide position of pairing partner of i or 0 zero if unpaired.
     * If pairedSites[a][b] =< -1, then nucleotide position = genomeLength + pairedSites[a][b] + 1
     */
    public int[][] pairedSites;
    public String name = "";
    /**
     * String representing the nucleotide sequence of this structure.
     */
    public String sequence = "";
    public int startPosition = 0;
    public int length;
    public int index;

    public Structure(int length) {
        this.length = length;
        this.pairedSites = new int[2][length];
    }

    public String getDotBracketString() {
        String pairString = "";
        for (int i = 0; i < length; i++) {
            if (pairedSites[1][i] == 0) {
                pairString += ".";
            } else if (pairedSites[0][i] < pairedSites[1][i]) {
                pairString += "(";
            } else {
                pairString += ")";
            }
        }
        return pairString;
    }

    /**
     *
     * @return the start position of the structure in the parent genome (one-offset)
     */
    public int getStartPosition ()
    {
        return startPosition;
    }

    /**
     * @return the end position (inclusive) of the structure in the parent genome.
     */
    public int getEndPosition ()
    {
        return startPosition+length-1;
    }

    public int getLength ()
    {
        return length;
    }

    @Override
    public String toString ()
    {
        return name + " ["+getStartPosition()+"-"+getEndPosition()+"]:"+getDotBracketString();
    }

    public int [][] allShortestPaths ()
    {
        int [] [] distance = new int[length][length];
        return distance;
    }
    
    

    /*public static void main(String [] args)
    {
        try {
            Structure s = StructureParser.parseNaspCtFile(new File("D:/NASP/BFDV/BFDV_10Seq.ct"));
            for (int i = 0; i < s.length; i++)
            {
                System.out.println(s.pairedSites[0][i]+"\t"+s.pairedSites[1][i]);
            }
            System.out.println(s.getDotBracketString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Structure other = (Structure) obj;
        if (this.startPosition != other.startPosition) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
