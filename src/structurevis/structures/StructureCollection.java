package structurevis.structures;

import java.io.File;
import java.util.ArrayList;
import structurevis.ui.layerpanel.GenomeOrganization;
import structurevis.structures.metadata.NucleotideComposition;
import structurevis.structures.metadata.Metadata;
import structurevis.structures.metadata.SequenceData1D;
import structurevis.structures.metadata.SequenceData2D;

/**
 *
 * @author Michael Golden
 */
public class StructureCollection {

    public File file;

    public String name = "";

    public ArrayList<Structure> structures = new ArrayList<Structure>();

    public int genomeLength;

    public String dotBracketStructure;

    /*
     * True if genome is circularGenome, false if genome is linear.
     */
    public boolean circularGenome = false;

    public ArrayList<Metadata> metadata = new ArrayList<Metadata>();

    public ArrayList<NucleotideComposition> nucleotideComposition = new ArrayList<NucleotideComposition>();
    public ArrayList<SequenceData1D> sequenceData1D = new ArrayList<SequenceData1D>();
    public ArrayList<SequenceData2D> sequenceData2D = new ArrayList<SequenceData2D>();

    /*
     * DNA if true, RNA if false.
     */
    public boolean dnaSequence = false;

    public GenomeOrganization getGenomeOrganization ()
    {
        for(int i = 0 ; i < metadata.size() ; i++)
        {
            if(metadata.get(i).getType().equals("GenomeStructure"))
            {
                return (GenomeOrganization) metadata.get(i);
            }
        }
        return null;
    }

    public NucleotideComposition getNucleotideComposition ()
    {
        for(int i = 0 ; i < metadata.size() ; i++)
        {
            if(metadata.get(i).getType().equals("NucleotideComposition"))
            {
                return (NucleotideComposition) metadata.get(i);
            }
        }
        return null;
    }
}
