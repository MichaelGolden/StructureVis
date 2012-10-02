/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.structures.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import structurevis.structures.Structure;
import structurevis.structures.StructureCollection;
import structurevis.structures.StructureParser;
import structurevis.ui.layerpanel.GenomeOrganization;
import structurevis.ui.layerpanel.Region;
import structurevis.structures.metadata.Metadata;

/**
 *
 * @author Michael Golden
 */
public class StructureCollectionStAXWriter {

    public static void saveStructureCollectionToFile(StructureCollection collection, File file) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter(file));

            writer.writeStartDocument();
            writer.writeCharacters("\n");
            writer.writeStartElement("structure-collection");
            if (collection != null && collection.name != null) {
                if (collection.name != null) {
                    writer.writeAttribute("name", collection.name);
                    writer.writeAttribute("dnaSequence", Boolean.toString(collection.dnaSequence));
                    writer.writeAttribute("consensusStructure", collection.dotBracketStructure);
                }

                if(collection.genomeLength > 0)
                {
                    writer.writeAttribute("genomeLength", collection.genomeLength + "");
                }

                for (int i = 0; i < collection.metadata.size(); i++) {
                    writer.writeCharacters("\n");
                    writer.writeStartElement("metadata");
                    writer.writeAttribute("type", collection.metadata.get(i).getType());
                    writer.writeCharacters("\n");
                    collection.metadata.get(i).writeToXMLStream(writer);
                    writer.writeEndElement(); // metadata
                }
                writer.writeCharacters("\n");

                writer.writeStartElement("structures");
                writer.writeCharacters("\n");
                for (int i = 0; i < collection.structures.size(); i++) {
                    Structure s = collection.structures.get(i);
                    writer.writeStartElement("structure");
                    writer.writeAttribute("name", s.name);
                    writer.writeAttribute("length", s.length + "");
                    writer.writeAttribute("startPosition", s.startPosition + "");
                    writer.writeCharacters("\n");
                    writer.writeStartElement("dot-bracket");
                    writer.writeCharacters(s.getDotBracketString());
                    writer.writeEndElement(); // dot-bracket
                    writer.writeCharacters("\n");
                    writer.writeEndElement(); // structure
                    writer.writeCharacters("\n");
                }
                writer.writeEndElement(); // structures
            }

            writer.writeCharacters("\n");
            writer.writeEndElement(); // collection
            writer.writeEndDocument();

            writer.flush();
            writer.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(StructureCollectionStAXWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StructureCollectionStAXWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        try
        {
        StructureCollection s = StructureParser.parseNaspFiles(new File("d:/Nasp/BFDV/BFDV_10Seq.out"), new File("d:/Nasp/BFDV/BFDV_10Seq.ct"));
        float[] data = {0.3f, 4f, 3f, 4.3f, 3.4f, 3f};
        float[][] data2D = {{0.3f, 4f, 3f}, {4.3f, 3.4f, 3f}};
        GenomeOrganization genomeStructure = new GenomeOrganization();
        genomeStructure.genome.add(new Region(100, 1000, "NS1"));
        genomeStructure.genome.add(new Region(1005, 1203, "Core"));

        s.metadata.add(genomeStructure);
        //Metadata a = Metadata.class.cast(d).;
        saveStructureCollectionToFile(s, new File("output/test.xml"));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
