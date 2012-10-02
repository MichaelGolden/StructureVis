package structurevis.structures.io;

import java.awt.Color;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import structurevis.data.Mapping;
import structurevis.structures.Structure;
import structurevis.structures.StructureCollection;
import structurevis.structures.StructureParser;
import structurevis.ui.layerpanel.GenomeOrganization;
import structurevis.structures.metadata.NucleotideComposition;
import structurevis.ui.layerpanel.Region;
import structurevis.structures.metadata.Metadata;
import structurevis.structures.metadata.MetadataFromFile;
import structurevis.ui.ColorGradient;
import structurevis.ui.ColorTools;
import structurevis.ui.DataTransform;

/**
 *
 * @author Michael Golden
 */
public class StructureCollectionStAXReader extends DefaultHandler {

    private StructureCollection collection;
    private Structure s = null;
    private CharArrayWriter contents = new CharArrayWriter();
    private String parseMetadata = null;
    private Metadata metadata = null;
    private GenomeOrganization genomeStructure = null;
    private NucleotideComposition nucleotideComposition = null;
    private MetadataFromFile metadataFromFile = null;

    public void startElement(String namespaceURI, String localName, String qName, Attributes attr) throws SAXException {
        contents.reset();
        if (parseMetadata != null && parseMetadata.equals("GenomeStructure")) {
            if (localName.equals("region")) {
                int start = Integer.parseInt(attr.getValue("start"));
                int end = Integer.parseInt(attr.getValue("end"));
                String name = attr.getValue("name");
                int level = 0;
                if (attr.getValue("level") != null) {
                    level = Integer.parseInt(attr.getValue("level"));
                }
                Color c = Color.white;
                if (attr.getValue("color") != null) {
                    c = ColorTools.getColorFromString(attr.getValue("color"));
                }
                Region region = new Region(start, end, name);
                region.level = level;
                region.color = c;
                genomeStructure.genome.add(region);
            }

        } else if (parseMetadata != null && parseMetadata.equals("MetadataFromFile")) {
            if (localName.equals("metadata-from-file")) {
                if (attr.getValue("name") != null) {
                    metadataFromFile.name = attr.getValue("name");
                }
                if (attr.getValue("type") != null) {
                    metadataFromFile.type = attr.getValue("type");
                }
                if (attr.getValue("file") != null) {
                    String[] split = attr.getValue("file").split(";");
                    metadataFromFile.files = new ArrayList<File>();
                    for (int i = 0; i < split.length; i++) {
                        metadataFromFile.files.add(new File(split[i]));
                    }
                }
                if (attr.getValue("mapping-file") != null) {
                    String[] split = attr.getValue("mapping-file").split(";");
                    metadataFromFile.mappingFiles = new ArrayList<File>();
                    for (int i = 0; i < split.length; i++) {
                        metadataFromFile.mappingFiles.add(new File(split[i]));
                    }
                }
                if (attr.getValue("codonData") != null) {
                    String[] split = attr.getValue("codonData").split(";");
                    metadataFromFile.codonData = new ArrayList<Boolean>();
                    for (int i = 0; i < split.length; i++) {
                        metadataFromFile.codonData.add(Boolean.parseBoolean(split[i]));
                    }
                }
                if (attr.getValue("dataColumn") != null) {
                    String[] split = attr.getValue("dataColumn").split(";");
                    metadataFromFile.dataColumns = new ArrayList<Integer>();
                    for (int i = 0; i < split.length; i++) {
                        if (split[i].length() > 0) {
                            metadataFromFile.dataColumns.add(Integer.parseInt(split[i]));
                        }
                    }
                }
                if (attr.getValue("useMin") != null) {
                    metadataFromFile.useMin = Boolean.parseBoolean(attr.getValue("useMin"));
                }
                if (attr.getValue("useMax") != null) {
                    metadataFromFile.useMax = Boolean.parseBoolean(attr.getValue("useMax"));
                }
                if (attr.getValue("min") != null) {
                    metadataFromFile.useMin = true;
                    metadataFromFile.min = Double.parseDouble(attr.getValue("min"));
                }
                if (attr.getValue("max") != null) {
                    metadataFromFile.useMax = true;
                    metadataFromFile.max = Double.parseDouble(attr.getValue("max"));
                }
                if (attr.getValue("transform") != null) {
                    metadataFromFile.dataTransform = new DataTransform(metadataFromFile.min, metadataFromFile.max, DataTransform.TransformType.valueOf(attr.getValue("transform")));
                }
                if (attr.getValue("color-gradient") != null) {
                    metadataFromFile.colorGradient = ColorGradient.getValue(attr.getValue("color-gradient"));
                }
                if (attr.getValue("color-gradient-secondary") != null) {
                    metadataFromFile.colorGradientSecondary = ColorGradient.getValue(attr.getValue("color-gradient-secondary"));
                } else {
                    metadataFromFile.colorGradientSecondary = metadataFromFile.colorGradient;
                }
            }

        } else if (localName.equals("structure-collection")) {
            collection = new StructureCollection();

            if (attr.getValue("name") != null) {
                collection.name = attr.getValue("name");
            }

            if (attr.getValue("genomeLength") != null) {
                collection.genomeLength = Integer.parseInt(attr.getValue("genomeLength"));
            }

            if (attr.getValue("circularGenome") != null) {
                collection.circularGenome = Boolean.parseBoolean(attr.getValue("circularGenome"));
            }

            if (attr.getValue("dnaSequence") != null) {
                collection.dnaSequence = Boolean.parseBoolean(attr.getValue("dnaSequence"));
            }

            if (attr.getValue("consensusStructure") != null) {
                collection.dotBracketStructure = attr.getValue("consensusStructure");
            }
        } else if (localName.equals("metadata")) {
            if (attr.getValue("type") != null) {
                parseMetadata = attr.getValue("type");
                if (parseMetadata.equals("GenomeStructure")) {
                    genomeStructure = new GenomeOrganization();
                } else if (parseMetadata.equals("NucleotideComposition")) {
                    nucleotideComposition = new NucleotideComposition();
                } else if (parseMetadata.equals("MetadataFromFile")) {
                    metadataFromFile = new MetadataFromFile();
                }
            }
        } else if (localName.equals("structure")) {
            int length = 0;
            if (attr.getValue("length") != null) {
                length = Integer.parseInt(attr.getValue("length"));
            }
            s = new Structure(length);

            if (attr.getValue("startPosition") != null) {
                s.startPosition = Integer.parseInt(attr.getValue("startPosition"));
            }
            
            if (attr.getValue("name") != null) {
                s.name = attr.getValue("name");
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

        if (localName.equals("metadata")) {
            parseMetadata = null;
            if (genomeStructure != null) {
                collection.metadata.add(genomeStructure);
                genomeStructure = null;
            } else if (nucleotideComposition != null) {
                collection.metadata.add(nucleotideComposition);
                nucleotideComposition = null;
            } else if (metadataFromFile != null) {
                collection.metadata.add(metadataFromFile);
                metadataFromFile = null;
            }
        } else if (localName.equals("dot-bracket") && s != null) {
            int[][] pairedSites = StructureParser.getPairedNucleotidePositions(contents.toString(), s.startPosition);
            s.pairedSites = pairedSites;
        } else if (localName.equals("structure")) {
            collection.structures.add(s);
            s = null;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        contents.write(ch, start, length);
    }

    public static StructureCollection loadStructureCollectionFromFile(File file) {
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            StructureCollectionStAXReader contentHandler = new StructureCollectionStAXReader();
            xr.setContentHandler(contentHandler);
            xr.parse(new InputSource(new FileReader(file)));
            StructureCollection collection = contentHandler.collection;
            collection.file = file;
            return collection;
        } catch (Exception ex) {
            Logger.getLogger(StructureCollectionStAXWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) {
        StructureCollection collection = loadStructureCollectionFromFile(new File("output/test.xml"));
        StructureCollectionStAXWriter.saveStructureCollectionToFile(collection, new File("output/test2.xml"));
    }
}
