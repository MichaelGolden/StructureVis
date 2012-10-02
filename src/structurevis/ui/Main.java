/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import net.hanjava.svg.SVG2EMF;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 *
 * @author Michael Golden
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        
        // Create a JPEG transcoder
        try {
            String svgUrl = "file:///C:/structure10.svg";
            File emfFile = new File("C:/structure10.emf");
            SVG2EMF.convert(svgUrl, emfFile);
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new FileReader("sequence.gb"));
// a namespace to override that in the file
        Namespace ns = RichObjectFactory.getDefaultNamespace();
// we are reading DNA sequences
        RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(br, ns);
        while (seqs.hasNext()) {
            try {
                RichSequence rs = seqs.nextRichSequence();
                Iterator<Feature> it = rs.features();
                while (it.hasNext()) {
                    Feature ft = it.next();
                    System.out.println(ft.getType() + "\n" + ft.getLocation() + "\n" + ft.getAnnotation());
                    //System.out.println(ft.getAnnotation().asMap());
                    if (ft.getAnnotation().containsProperty("product")) {
                        System.out.println(">" + ft.getAnnotation().getProperty("product"));
                    }
                    System.out.println("---------------------------------------------");
                }
            } catch (NoSuchElementException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BioException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        //StructureParser.parseNaspFiles(new File("d:/Nasp/BFDV/BFDV_10Seq.out"),new File("d:/Nasp/BFDV/BFDV_10Seq.ct"));
        //Structure s = StructureParser.parseCtFile(new File("C:/Program Files/UNAFold/input/seq0.fas.25.ct")).get(0);
       /* System.out.println(s.name);
        System.out.println(s.sequence);
        System.out.println(s.pairedSites[0][2]);
        System.out.println(s.getDotBracketString());*/

        //Structure s = StructureParser.parseNaspCtFile(new File("C:/project/hepacivirus/10seq_aligned_d0.fasta.ct"));
        // System.out.println("A"+s.name);
        //System.out.println("B"+s.sequence.substring(0,100));
        //System.out.println("C"+s.pairedSites[1][3]);
    }
}
