package structurevis.ui.layerpanel;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import structurevis.data.Mapping;
import structurevis.structures.metadata.Metadata;
import structurevis.ui.ColorGradient;
import structurevis.ui.ColorTools;

/**
 *
 * @author Michael Golden
 */
public class GenomeOrganization extends Metadata {

    public ArrayList<Region> genome = new ArrayList<Region>();

    public GenomeOrganization() {
    }

    public GenomeOrganization(File genomeStructureFile, Mapping mapping) {
    }

    public String getType() {
        return "GenomeStructure";
    }

    public void writeToXMLStream(XMLStreamWriter writer) throws XMLStreamException {
        try {
            for (int i = 0; i < genome.size(); i++) {
                writer.writeStartElement("region");
                writer.writeAttribute("name", genome.get(i).name);
                writer.writeAttribute("start", genome.get(i).start + "");
                writer.writeAttribute("end", genome.get(i).end + "");
                writer.writeAttribute("level", genome.get(i).level + "");
                writer.writeAttribute("color", ColorTools.colorToString(genome.get(i).color) + "");
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(GenomeOrganization.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
