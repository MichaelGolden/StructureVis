/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.structures.io;

import iubio.readseq.BioseqFormats;
import iubio.readseq.BioseqWriterIface;
import iubio.readseq.Readseq;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Golden
 */
public class ReadseqTools {

    public ReadseqTools() {

    }

    public static void convertToFormat(int formatCode, File inputFile, File outputFile)
    {
          try {
            BioseqWriterIface seqwriter = BioseqFormats.newWriter(formatCode);
            seqwriter.setOutput(new FileWriter(outputFile));
            seqwriter.writeHeader();
            Readseq rd = new Readseq();
            rd.setInputObject(inputFile);
            if (rd.isKnownFormat() && rd.readInit()) {
                rd.readTo(seqwriter);
            }
            seqwriter.writeTrailer();
            seqwriter.close();
        } catch (IOException ex) {
            Logger.getLogger(ReadseqTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void convertToFastaFormat(File inputFile, File outputFile)
    {
          try {
            //int outid = BioseqFormats.formatFromName("fasta");
            BioseqWriterIface seqwriter = BioseqFormats.newWriter(8);
            seqwriter.setOutput(new FileWriter(outputFile));
            seqwriter.writeHeader();
            Readseq rd = new Readseq();
            rd.setInputObject(inputFile);
            if (rd.isKnownFormat() && rd.readInit()) {
                rd.readTo(seqwriter);
            }
            seqwriter.writeTrailer();
            seqwriter.close();
        } catch (IOException ex) {
            Logger.getLogger(ReadseqTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean isInFastaFormat(File file) {
        try {
            Readseq rd = new Readseq();
            rd.setInputObject(file);
            if (rd.isKnownFormat() && rd.readInit()) {
                if(rd.getFormat() == 8)
                {
                    rd.close();
                    return true;
                }
            }
            rd.close();
        } catch (IOException ex) {
            Logger.getLogger(ReadseqTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean isKnownFormat(File file)
    {
        try {
            Readseq rd = new Readseq();
            rd.setInput(file);
            //rd.
            //System.out.println(rd.readInit()+"kk" + rd.getFormat());
            if (rd.isKnownFormat() && rd.readInit()) {
                rd.close();
                return true;
            }
            rd.close();
        } catch (IOException ex) {
            Logger.getLogger(ReadseqTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static String getFormatName(File file)
    {
        try {
            Readseq rd = new Readseq();
            rd.setInput(file);
            if (rd.isKnownFormat() && rd.readInit()) {
                rd.close();
                return rd.getFormatName();
            }
            rd.close();
        } catch (IOException ex) {
            Logger.getLogger(ReadseqTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
