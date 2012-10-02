package structurevis.structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for parsing various secondary structure formats into a standard format.
 *
 * @author Michael Golden
 */
public class StructureParser {

    /*
     * Parse connect format. @param ctFile connect format file to be parsed.
     * @return
     */
    public static ArrayList<Structure> parseCtFile(File ctFile) throws Exception {
        ArrayList<Structure> structures = new ArrayList<Structure>();

        BufferedReader buffer = new BufferedReader(new FileReader(ctFile));

        String textline = null;

        while ((textline = buffer.readLine()) != null) {
            String[] split = textline.split("(\\s)+");
            Structure s = new Structure(Integer.parseInt(split[0]));
            s.name = split[1];
            s.sequence = "";
            for (int i = 0; i < s.length && (textline = buffer.readLine()) != null; i++) {
                String[] split2 = textline.split("(\\t)+");
                s.sequence += split2[1].charAt(0);
                s.pairedSites[0][i] = Integer.parseInt(split2[0]);
                s.pairedSites[1][i] = Integer.parseInt(split2[4]);
            }
            structures.add(s);
        }

        buffer.close();
        return structures;
    }

    public static Structure parseTabDelimittedHelixFile(File helixFile, int length) throws Exception {
        ArrayList<Integer> c1 = new ArrayList<Integer>();
        ArrayList<Integer> c2 = new ArrayList<Integer>();
        ArrayList<Integer> c3 = new ArrayList<Integer>();
        int len = length;

        Structure s = null;
        BufferedReader buffer = new BufferedReader(new FileReader(helixFile));

        String textline = null;
        while ((textline = buffer.readLine()) != null) {
            String[] split = textline.split("(\\s)+");
            if (split.length == 3) {
                c1.add(Integer.parseInt(split[0]));
                c2.add(Integer.parseInt(split[1]));
                c3.add(Integer.parseInt(split[2]));

                len = Math.max(len, Integer.parseInt(split[1]));
            }
            else
            if(textline.length() > 0)
            {
                throw new Exception("Error parsing tab-delimitted helix file. 3 columns were expected, "+split.length+" were found.");
            }
        }
        buffer.close();

        s = new Structure(len);
        for (int i = 0; i < c1.size(); i++) {
            int a = c1.get(i);
            int b = c2.get(i);
            int helixLen = c3.get(i);

            for (int j = 0; j < helixLen; j++) {
                s.pairedSites[0][a - 1 + j] = a + j;
                s.pairedSites[1][a - 1 + j] = b - j;

                s.pairedSites[0][b - 1 - j] = b - j;
                s.pairedSites[1][b - 1 - j] = a + j;
            }
        }

        return s;
    }

    /*
     * Parse NASP connect format. @param ctFile NASP connect format file to be
     * parsed. @return
     */
    public static Structure parseNaspCtFile(File naspCtFile) throws Exception {
        Structure s = null;
        BufferedReader buffer = new BufferedReader(new FileReader(naspCtFile));

        String textline = buffer.readLine();
        String[] split = textline.split("(\\s)+");
        s = new Structure(Integer.parseInt(split[0]));
        s.sequence = "";
        for (int i = 0; i < s.length && (textline = buffer.readLine()) != null; i++) {
            String[] split2 = textline.split("(\\s)+");
            s.sequence += split2[1].charAt(0);
            s.pairedSites[0][i] = Integer.parseInt(split2[0]);
            s.pairedSites[1][i] = Integer.parseInt(split2[4]);
        }

        buffer.close();
        return s;
    }

    public static StructureCollection parseNaspFiles(File naspFile, String dotBracketString) {

        int[][] pairedSites = getPairedNucleotidePositions(dotBracketString);
        StructureCollection collection = new StructureCollection();
        collection.dotBracketStructure = dotBracketString;

        try {
            BufferedReader buffer = new BufferedReader(new FileReader(naspFile));
            String textline = null;
            boolean readStructures = false;

            while ((textline = buffer.readLine()) != null) {
                if (textline.startsWith("1:")) {
                    readStructures = true;
                } else if (textline.startsWith("Sequence shape:") && textline.toLowerCase().contains("circular")) {
                    collection.circularGenome = true;
                } else if (textline.contains("length:")) {
                    collection.genomeLength = Integer.parseInt(textline.split(":")[1].trim());
                }

                if (readStructures) {
                    if (!textline.startsWith("---")) {
                        NaspStructure ns = NaspStructure.getNaspStructureFromString(textline);
                        Structure s = null;

                        int structureLength = Math.abs(ns.gappedEndB - ns.gappedStartA) + 1;
                        int starta = ns.gappedStartA;
                        boolean circularize = false;
                        // if circularGenome genome, use smaller structure
                        if (collection.circularGenome && structureLength > collection.genomeLength / 2) {
                            // circularize the structure
                            structureLength = (collection.genomeLength - ns.gappedStartB + 1) + ns.gappedEndA;
                            starta = ns.gappedStartB;
                            circularize = true;
                        }

                        s = new Structure(structureLength);
                        s.name = ns.id+"";
                        s.startPosition = starta;

                        // get structure
                        int a = 0;
                        for (int i = starta - 1; a < structureLength; i = (i + 1) % collection.genomeLength) {
                            s.pairedSites[0][a] = pairedSites[0][i];
                            s.pairedSites[1][a] = pairedSites[1][i];
                            if (circularize && s.pairedSites[0][a] > collection.genomeLength / 2) {
                                s.pairedSites[0][a] = -(-s.pairedSites[0][a] + collection.genomeLength) - 1;
                            }
                            if (circularize && s.pairedSites[1][a] > collection.genomeLength / 2) {
                                s.pairedSites[1][a] = -(-s.pairedSites[1][a] + collection.genomeLength) - 1;
                            }
                            a++;
                        }
                        collection.structures.add(s);
                    } else {
                        break;
                    }
                }
            }
            buffer.close();
        } catch (IOException ex) {
            Logger.getLogger(StructureParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return collection;
    }

    public static StructureCollection parseNaspFiles(File naspFile, File naspCtFile) throws Exception {

        Structure consensusStructure = parseNaspCtFile(naspCtFile);
        StructureCollection collection = new StructureCollection();
        collection.dotBracketStructure = consensusStructure.getDotBracketString();

        try {
            BufferedReader buffer = new BufferedReader(new FileReader(naspFile));
            String textline = null;
            boolean readStructures = false;

            while ((textline = buffer.readLine()) != null) {
                if (textline.startsWith("1:")) {
                    readStructures = true;
                } else if (textline.startsWith("Sequence shape:") && textline.toLowerCase().contains("circular")) {
                    collection.circularGenome = true;
                } else if (textline.contains("length:")) {
                    collection.genomeLength = Integer.parseInt(textline.split(":")[1].trim());
                }

                if (readStructures) {
                    if (!textline.startsWith("---")) {
                        NaspStructure ns = NaspStructure.getNaspStructureFromString(textline);
                        Structure s = null;

                        int structureLength = Math.abs(ns.gappedEndB - ns.gappedStartA) + 1;
                        int starta = ns.gappedStartA;
                        boolean circularize = false;
                        // if circularGenome genome, use smaller structure
                        if (collection.circularGenome && structureLength > collection.genomeLength / 2) {
                            // circularize the structure
                            structureLength = (collection.genomeLength - ns.gappedStartB + 1) + ns.gappedEndA;
                            starta = ns.gappedStartB;
                            circularize = true;
                        }

                        s = new Structure(structureLength);
                        s.startPosition = starta;
                        // get structure
                        int a = 0;
                        for (int i = starta - 1; a < structureLength; i = (i + 1) % collection.genomeLength) {
                            s.pairedSites[0][a] = consensusStructure.pairedSites[0][i];
                            s.pairedSites[1][a] = consensusStructure.pairedSites[1][i];
                            if (circularize && s.pairedSites[0][a] > collection.genomeLength / 2) {
                                s.pairedSites[0][a] = -(-s.pairedSites[0][a] + collection.genomeLength) - 1;
                            }
                            if (circularize && s.pairedSites[1][a] > collection.genomeLength / 2) {
                                s.pairedSites[1][a] = -(-s.pairedSites[1][a] + collection.genomeLength) - 1;
                            }
                            a++;
                        }
                        collection.structures.add(s);
                    } else {
                        break;
                    }
                }
            }
            buffer.close();
        } catch (IOException ex) {
            Logger.getLogger(StructureParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return collection;
    }
    
    public static Structure parseDotBracketString(String dotBracketString)
    {
        Structure s = new Structure(dotBracketString.length());
        s.pairedSites = StructureParser.getPairedNucleotidePositions(dotBracketString);
        return s;
    }

    public static int[][] getPairedNucleotidePositions(String dotBracketString) {
        return getPairedNucleotidePositions(dotBracketString, 1);
    }

    public static int[][] getPairedNucleotidePositions(String dotBracketString, int startPosition) {
        int[][] pairedSites = new int[2][dotBracketString.length()];
        for (int i = 0; i < dotBracketString.length(); i++) {
            pairedSites[0][i] = startPosition + i;
            int pairedPosition = 0;
            if (dotBracketString.charAt(i) == '(') {
                int c = 1;
                for (int j = i + 1; c != 0 && j < dotBracketString.length(); j++) {
                    if (dotBracketString.charAt(j) == '(') {
                        c++;
                    } else if (dotBracketString.charAt(j) == ')') {
                        c--;
                    }
                    pairedPosition++;
                }

                if (c == 0) {
                    pairedSites[1][i] = startPosition + i + pairedPosition;
                    pairedSites[1][i + pairedPosition] = pairedSites[0][i];
                }
            }
        }
        return pairedSites;
    }

    /**
     * Returns a list of adjacent non-overlapping substructures. Method used to
     * display a full genomic structure representation.
     *
     * @param dotBracketString
     * @param maxLength the maximum length a substructure may be.
     * @return a list of adjacent non-overlapping substructures.
     */
    /*
     * public static ArrayList<Structure> enumerateAdjacentSubstructures(String
     * dotBracketString, int minLength, int maxLength) { ArrayList<Structure>
     * structures = new ArrayList<Structure>(); int[][] pairedSites =
     * getPairedNucleotidePositions(dotBracketString, 1); for (int i = 0; i <
     * pairedSites[0].length; i++) { int x = pairedSites[0][i]; int y =
     * pairedSites[1][i]; //System.out.println(x+"\t"+y); if (y > 0 & y - x + 1
     * > 0) { // System.out.println("Length:"+(y-x+1)); Structure s = new
     * Structure(y - x + 1); int[][] pairedSitesSub = new int[2][y - x + 1]; for
     * (int j = 0; j < pairedSitesSub[0].length; j++) { pairedSitesSub[0][j] =
     * pairedSites[0][i + j]; pairedSitesSub[1][j] = pairedSites[1][i + j];
     * //System.out.println(pairedSitesSub[0][j]+"\t"+pairedSitesSub[1][j]); }
     * s.pairedSites = pairedSitesSub; s.startPosition = x; if (maxLength == 0
     * || s.length <= maxLength) { i += s.length;
     * ///System.out.println(s.startPosition + "\t" + s.getDotBracketString() +
     * "\t" + (s.startPosition + s.length)); if (s.length >= minLength) {
     * structures.add(s); } } } }
     *
     * return structures;
    }
     */
    public static String getDotBracketStringInverse(String dotBracketString) {
        String ret = dotBracketString.replaceAll("\\(", "A");
        ret = ret.replaceAll("\\)", "B");
        ret = ret.replaceAll("A", ")");
        ret = ret.replaceAll("B", "(");
        return ret;
    }

    /**
     * Returns a list of adjacent non-overlapping substructures. Method used to
     * display a full genomic structure representation.
     *
     * @param dotBracketString
     * @param maxLength the maximum length a substructure may be.
     * @return a list of adjacent non-overlapping substructures.
     */
    public static ArrayList<Structure> enumerateAdjacentSubstructures(String dotBracketString, int minLength, int maxLength, boolean circularize) {
        ArrayList<Structure> structures = new ArrayList<Structure>();
        int[][] pairedSites = getPairedNucleotidePositions(dotBracketString, 1);
        int genomeLength = dotBracketString.length();

        if (circularize) {
            int[][] pairedSitesCircularized = new int[2][genomeLength * 2];
            for (int i = 0; i < genomeLength; i++) {
                pairedSitesCircularized[0][i] = pairedSites[0][i];
                pairedSitesCircularized[1][i] = pairedSites[1][i];
            }
            for (int i = 0; i < genomeLength; i++) {
                if (pairedSites[1][i] - pairedSites[0][i] > genomeLength / 2) {
                    int end = pairedSites[1][i] - 1;
                    pairedSitesCircularized[0][end] = pairedSites[0][end]; // not really necessary
                    pairedSitesCircularized[1][end] = pairedSites[0][i] + genomeLength; // artificially wrap
                    pairedSitesCircularized[1][i] = 0; // make unpaired
                }
            }
            for (int i = 0; i < genomeLength * 2; i++) {
                pairedSitesCircularized[0][i] = i + 1;
                if (pairedSitesCircularized[1][i] != 0) {
                    pairedSitesCircularized[0][pairedSitesCircularized[1][i] - 1] = pairedSitesCircularized[1][i];
                    pairedSitesCircularized[1][pairedSitesCircularized[1][i] - 1] = pairedSitesCircularized[0][i];
                }
            }

            pairedSites = pairedSitesCircularized;
        }

        boolean lastStructureAdded = false;
        for (int i = 0; i < pairedSites[0].length; i++) {
            int x = pairedSites[0][i];
            int y = pairedSites[1][i];

            if (y > 0 & y - x + 1 > 0) {
                Structure s = new Structure(y - x + 1);

                int[][] pairedSitesSub = new int[2][y - x + 1];
                for (int j = 0; j < pairedSitesSub[0].length; j++) {
                    pairedSitesSub[0][j] = pairedSites[0][i + j];
                    pairedSitesSub[1][j] = pairedSites[1][i + j];
                }
                s.pairedSites = pairedSitesSub;
                s.startPosition = x;
                s.name = structures.size()+"";
                if (maxLength == 0 || s.length <= maxLength) {
                    i += s.length;

                    if (s.length >= minLength && x + s.length < genomeLength) { 
                        structures.add(s);
                    }

                    if (!lastStructureAdded && x < genomeLength && x + s.length >= genomeLength && circularize) {
                        structures.add(s);
                        lastStructureAdded = true;
                    }
                }
            }
        }

        return structures;
    }
    
    public static ArrayList<Structure> enumerateSubstructures(String dotBracketString, int minLength, int maxLength, boolean circularize)
    {
        ArrayList<Structure> structures = enumerateAdjacentSubstructures(dotBracketString, minLength, maxLength, circularize);
        recursivelyEnumerateSubstructures(minLength, maxLength, structures, 0, 0);
        return structures;
    }
    
    private static void recursivelyEnumerateSubstructures(int minLength, int maxLength, ArrayList<Structure> structures, int startIndex, int level) {
        int added = 0;
        
        int end = structures.size();
        for (int k = startIndex; k < end ; k++) {
            int kAdded = 0;
            
            int[][] pairedSites = getPairedNucleotidePositions(structures.get(k).getDotBracketString(), structures.get(k).getStartPosition());
            int fullStructureLength = structures.get(k).getDotBracketString().length();

            for (int i = 0 ; i < pairedSites[0].length ; i++) {
                int x = pairedSites[0][i];
                int y = pairedSites[1][i];
                int length = y - x + 1;
                
                if (y > 0 & length > 0) {
                    Structure s = new Structure(length);

                    int[][] pairedSitesSub = new int[2][length];
                    for (int j = 0; j < pairedSitesSub[0].length; j++) {
                        pairedSitesSub[0][j] = pairedSites[0][i + j];
                        pairedSitesSub[1][j] = pairedSites[1][i + j];
                    }
                    s.pairedSites = pairedSitesSub;
                    s.startPosition = x;
                    
                    if (maxLength == 0 || s.length < fullStructureLength*0.75) {
                        i += s.length;
                        if (s.length >= minLength && s.length < fullStructureLength) {
                           if(!structures.contains(s))
                           {
                                s.name = structures.size()+"";
                                //s.name = structures.get(k).name + "." + kAdded;
                                structures.add(s);
                                added++;
                                kAdded++;
                           }
                           else
                           {
                               int index = structures.indexOf(s);
                               //System.out.println("already contains " + s.toString() + "\t" +index+"\t"+ structures.get(index));
                           }
                        }
                    }
                }
            }
        }
        
        if(added > 0)
        {            
            //System.out.println("added=" + added + ", n="+structures.size());
            recursivelyEnumerateSubstructures(minLength, maxLength, structures, end, level+1);
        }
    }

    public static String getDotBracketStringFromFile(File dotBracketFile) throws Exception {
        BufferedReader buffer = new BufferedReader(new FileReader(dotBracketFile));
        String textline = buffer.readLine().trim();
        buffer.close();
        return textline;
    }

    public static boolean isDotBracketString(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != '(' && text.charAt(i) != ')' && text.charAt(i) != '.') {
                return false;
            }
        }
        return true;
    }
}
