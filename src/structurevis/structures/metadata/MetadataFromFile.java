/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.structures.metadata;

import java.awt.Color;
import java.io.File;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import structurevis.data.Mapping;
import structurevis.ui.ColorGradient;
import structurevis.ui.DataTransform;

/**
 *
 * @author Michael Golden
 */
public class MetadataFromFile extends Metadata {

    public String name = "";
    public String type = "";
    public ArrayList<File> files;
    public ArrayList<File> mappingFiles;
    //public boolean codonData;
    public boolean useMin = false;
    public double min; // min displayable value
    public boolean useMax = false;
    public double max; // max displayable value
    public ArrayList<Integer> dataColumns;
    public ArrayList<Boolean> codonData;
    public DataTransform dataTransform;
    // color gradient used to display daata
    public ColorGradient colorGradient = new ColorGradient(Color.yellow, Color.red);
    public ColorGradient colorGradientSecondary = new ColorGradient(Color.yellow, Color.red);

    public MetadataFromFile() {
        files = new ArrayList<File>();
        mappingFiles = new ArrayList<File>();
        dataColumns = new ArrayList<Integer>();
        codonData = new ArrayList<Boolean>();
    }

    /*public MetadataFromFile(String type, File file, File mappingFile, boolean codonData) {
        this.type = type;
        this.file = file;
        this.mappingFile = mappingFile;
        this.codonData = codonData;
    }*/

    public String getType() {
        return "MetadataFromFile";
    }

    public void writeToXMLStream(XMLStreamWriter writer) throws XMLStreamException {
        try {
            writer.writeStartElement("metadata-from-file");
             if(name != null)
            {
                writer.writeAttribute("name", name);
            }
            if(type != null)
            {
                writer.writeAttribute("type", type);
            }
            if(files != null)
            {
                String fileString = "";
                for(int i = 0 ; i < files.size() ; i++)
                {
                    fileString +=  files.get(i).getPath() + ";";
                }
                writer.writeAttribute("file", fileString);
            }
            if(mappingFiles != null)
            {
                String fileString = "";
                for(int i = 0 ; i < mappingFiles.size() ; i++)
                {
                    fileString +=  mappingFiles.get(i).getPath() + ";";
                }
                writer.writeAttribute("mapping-file", fileString);
            }
            if(codonData != null)
            {
                String codonString = "";
                for(int i = 0 ; i < codonData.size() ; i++)
                {
                    codonString += codonData.get(i).toString() + ";";
                }
                writer.writeAttribute("codonData", codonString);
            }
            if(dataColumns != null)
            {
                String dataColumnString = "";
                for(int i = 0 ; i < dataColumns.size() ; i++)
                {
                    dataColumnString +=  dataColumns.get(i).intValue() + ";";
                }
                writer.writeAttribute("dataColumn", dataColumnString);
            }
            /*if (useMin) {
                
            }            
            if (useMax) {
                
            }*/
            writer.writeAttribute("useMin", Boolean.toString(useMin));
            writer.writeAttribute("useMax", Boolean.toString(useMax));
            writer.writeAttribute("min", Double.toString(min));
            writer.writeAttribute("max", Double.toString(max));
            if(dataTransform != null)
            {
                writer.writeAttribute("transform", dataTransform.type.toString());
            }
            if (colorGradient != null) {
                writer.writeAttribute("color-gradient", colorGradient.toString());
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
        } catch (XMLStreamException ex) {
            Logger.getLogger(MetadataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Object getData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canFree() {
        return false;
    }

    @Override
    public void load(File file, Mapping mapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void free() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
