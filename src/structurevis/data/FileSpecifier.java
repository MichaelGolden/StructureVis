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
public class FileSpecifier {

    public File naspStructuresFile = null;
    public File naspAlignmentFile =  null;
    public File naspMatrixFile =  null;
    public File naspCtFile =  null;
    public File naspCtWithPvaluesFile =  null;
    public File coevolutionAlignmentFile =  null;
    public File coevolutionDir =  null;
    public ArrayList<File> geneAlignmentFiles =  new ArrayList<File>();
    public ArrayList<File> parrisMarginalsFiles =  new ArrayList<File>();
    public File genbankReferenceFile = null;
    public File collateDir = null;
    public String name;

    public FileSpecifier(File specifierFile, String name)
    {
        this.name = name;
        try
        {
            BufferedReader buffer = new BufferedReader(new FileReader(specifierFile));
            String textline = null;
            while((textline = buffer.readLine()) != null)
            {
                String [] split = textline.split("=");
                if(split.length == 2)
                {
                    if(split[0].equals("NASP_STRUCTURES_FILE"))
                    {
                        naspStructuresFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("NASP_ALIGNMENT_FILE"))
                    {
                        naspAlignmentFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("NASP_MATRIX_FILE"))
                    {
                        naspMatrixFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("NASP_CT_FILE"))
                    {
                        naspCtFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("NASP_CT_WITH_PVALUES_FILE"))
                    {
                        naspCtWithPvaluesFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("COEVOLUTION_ALIGNMENT_FILE"))
                    {
                        coevolutionAlignmentFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("COEVOLUTION_DIRECTORY"))
                    {
                        coevolutionDir = new File(split[1]);
                    }
                    else
                    if(split[0].equals("NASP_STRUCTURES_FILE"))
                    {
                        naspStructuresFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("GENE_ALIGMNENT_FILE"))
                    {
                        String [] files = split[1].split(";");
                        for(int i = 0 ; i < files.length ; i++)
                        {
                            geneAlignmentFiles.add(new File(files[i]));
                        }
                    }
                    else
                    if(split[0].equals("PARRIS_MARGINALS_FILE"))
                    {
                        String [] files = split[1].split(";");
                        for(int i = 0 ; i < files.length ; i++)
                        {
                            parrisMarginalsFiles.add(new File(files[i]));
                        }
                    }
                    else
                    if(split[0].equals("GENBANK_REFERENCE_FILE"))
                    {
                        genbankReferenceFile = new File(split[1]);
                    }
                    else
                    if(split[0].equals("COLLATE_DIRECTORY"))
                    {
                        collateDir = new File(split[1]);
                    }
                }
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
}

