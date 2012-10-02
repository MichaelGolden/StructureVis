package structurevis.structures.metadata;

import java.io.File;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import structurevis.data.Mapping;

/**
 *
 * @author Michael Golden
 */
public class Metadata {
    
    public String getType()
    {
        return null;
    }

    /**
     * Write metadata to the specified XMLStreamWriter. NB! Do not close the writer in the implementation of this method.
     */
    public void writeToXMLStream(XMLStreamWriter writer) throws XMLStreamException
    {
        
    }
    
    private Object data = null;
    public Object getData()
    {
        return null;
    }
    
    public boolean canFree()
    {
        return false;
    }
    
    public void load (File file, Mapping mapping)
    {
        
    }
    
    public void free ()
    {
        
    }
}
