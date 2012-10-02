/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.datacreation;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.event.EventListenerList;
import structurevis.data.IO;
import structurevis.data.MapCache;
import structurevis.data.Mapping;
import structurevis.data.ParrisData;
import structurevis.structures.StructureCollection;
import structurevis.structures.StructureParser;
import structurevis.structures.io.ReadseqTools;
import structurevis.structures.io.StructureCollectionStAXReader;
import structurevis.structures.io.StructureCollectionStAXWriter;
import structurevis.structures.metadata.MetadataFromFile;
import structurevis.ui.DataTransform;
import structurevis.ui.layerpanel.GenomeOrganization;

/**
 *
 * @author Michael Golden
 */
public class DataModel {
    // structure panel

    MapCache mapCache = new MapCache();
    File projectDirectory;
    File genomeStructureFile;

    public enum GenomeStructureFileType {

        STANDARD_CT_FILE, NASP_CT_FILE, DOT_BRACKET_FILE, TAB_DELIMITTED_HELIX
    };
    GenomeStructureFileType genomeStructureFileType = GenomeStructureFileType.STANDARD_CT_FILE;
    private File referenceNucleotideAlignment;
    File referenceNucleotideAlignmentFASTA;
    boolean nucleicAcidRNA = true;
    boolean conformationCircular = false;
    File substructureFile = null;

    public enum SubstructuresStructureFileType {

        NASP_FILE_TYPE, EXISTING_COLLECTION
    };
    SubstructuresStructureFileType substructuresStructureFileType = SubstructuresStructureFileType.NASP_FILE_TYPE;
    int minSubstructureSize = 10;
    int maxSubstructureSize = 150;
    // genome organization
    GenomeOrganization genomeOrganization = new GenomeOrganization();
    File genomeOrganizationMappingFile = null;

    // data sources
    public enum DataSourceType {

        CSV_1D,
        PARRIS_MARGINALS,
        PAIRWISE_TAB_2D
    };
    ArrayList<DataOverlay> overlays = new ArrayList<DataOverlay>();

    public boolean isFieldNameUsed(String name, DataOverlay thisOne) {
        for (int i = 0; i < overlays.size(); i++) {
            if (overlays.get(i).fieldName.equalsIgnoreCase(name) && !overlays.get(i).equals(thisOne)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return genomeStructureFile + "\n" + genomeStructureFileType + "\n" + nucleicAcidRNA + "\n" + conformationCircular + "\n" + substructureFile + "\n" + substructuresStructureFileType + "\n" + this.minSubstructureSize + "\n" + this.maxSubstructureSize;
    }

    public void loadProject() {
    }

    public void saveProject() throws Exception {
        projectDirectory.mkdir();

        StructureCollection s = new StructureCollection();

        switch (genomeStructureFileType) {
            case STANDARD_CT_FILE:
                s.dotBracketStructure = StructureParser.parseCtFile(genomeStructureFile).get(0).getDotBracketString();
            case NASP_CT_FILE:
                s.dotBracketStructure = StructureParser.parseNaspCtFile(genomeStructureFile).getDotBracketString();
                break;
            case DOT_BRACKET_FILE:
                s.dotBracketStructure = StructureParser.getDotBracketStringFromFile(genomeStructureFile);
            case TAB_DELIMITTED_HELIX:
                s.dotBracketStructure = StructureParser.parseTabDelimittedHelixFile(genomeStructureFile, 0).getDotBracketString();
        }

        referenceNucleotideAlignmentFASTA = new File(projectDirectory.getPath() + "/reference-alignment.fas");
        ReadseqTools.convertToFastaFormat(referenceNucleotideAlignment, referenceNucleotideAlignmentFASTA);

        if (genomeStructureFile != null && genomeStructureFile.exists()) {
            String filename = registerFile(genomeStructureFile);
            String consensusFile = projectDirectory.getPath() + File.separatorChar + filename + "-" + genomeStructureFile.getName() + ".consensus_structure";
            IO.copyFile(genomeStructureFile, new File(consensusFile));
        }

        s.dnaSequence = !nucleicAcidRNA;
        s.genomeLength = s.dotBracketStructure.length();
        s.circularGenome = conformationCircular;
        s.metadata.add(genomeOrganization);

        if (substructureFile != null) {
            // substructures
            switch (substructuresStructureFileType) {
                case NASP_FILE_TYPE:
                    StructureCollection temp = StructureParser.parseNaspFiles(substructureFile, s.dotBracketStructure);
                    s.structures = temp.structures;
                    break;
                case EXISTING_COLLECTION:
                    s.structures = StructureCollectionStAXReader.loadStructureCollectionFromFile(substructureFile).structures;
                    // TODO should do some error checking to test whether structures match up correctly.
                    break;
            }
        } else {
            s.structures = StructureParser.enumerateSubstructures(s.dotBracketStructure, minSubstructureSize, maxSubstructureSize, conformationCircular);
            //s.structures = StructureParser.enumerateAdjacentSubstructures(s.dotBracketStructure, minSubstructureSize, maxSubstructureSize, conformationCircular);
        }

        int totalDataSources = 0;
        for (int j = 0; j < overlays.size(); j++) {
            totalDataSources += Math.max(Math.max(overlays.get(j).dataSources1D.size(), overlays.get(j).dataSources2D.size()), 1);
        }
        //System.out.println("totalDataSources="+totalDataSources);
        int numDataSourcesProcessed = 0;


        // 1-dimensional data
        for (int j = 0; j < overlays.size(); j++) {
            MetadataFromFile metadata = new MetadataFromFile();
            DataOverlay overlay = overlays.get(j);
            metadata.type = overlay.type;
            if (overlay.type.equals("SequenceData1D")) {
                for (int i = 0; i < overlay.dataSources1D.size(); i++) {
                    DataSource1D dataSource = overlay.dataSources1D.get(i);
                    File mappingFileFASTA = new File(projectDirectory.getPath() + "/" + dataSource.mappingFile.getName() + ".fas");
                    ReadseqTools.convertToFastaFormat(dataSource.mappingFile, mappingFileFASTA);
                    String filename = registerFile(dataSource.dataFile);
                    String dataFile = filename + "-" + dataSource.dataFile.getName() + ".csv";
                    String mappingFile = "reference-to-" + filename + ".mapping";
                    if (!new File(projectDirectory.getPath() + File.separatorChar + mappingFile).exists()) {
                        File mappingFilePath = new File(projectDirectory.getPath() + "/" + mappingFile);
                        if (mapCache.filePairExists(referenceNucleotideAlignmentFASTA, mappingFileFASTA)) {
                            IO.copyFile(mapCache.getMappingFile(referenceNucleotideAlignmentFASTA, mappingFileFASTA), mappingFilePath);
                        } else {
                            mapCache.registerFilePair(referenceNucleotideAlignmentFASTA, mappingFileFASTA, mappingFilePath);
                            Mapping referenceToB = Mapping.createMapping(referenceNucleotideAlignmentFASTA, mappingFileFASTA, 1);
                            referenceToB.saveMapping(mappingFilePath);
                        }
                    }
                    switch (dataSource.dataSourceType) {
                        case CSV_1D:
                            if (!new File(projectDirectory.getPath() + "/" + dataFile).exists()) {
                                IO.copyFile(dataSource.dataFile, new File(projectDirectory.getPath() + "/" + dataFile));
                            }
                            metadata.files.add(new File(dataFile));
                            metadata.mappingFiles.add(new File(mappingFile));
                            metadata.dataColumns.add(dataSource.dataColumn);
                            metadata.codonData.add(dataSource.codonPositions);
                            break;
                        case PARRIS_MARGINALS:
                            if (!new File(projectDirectory.getPath() + "/" + dataFile).exists()) {
                                ParrisData parrisData = new ParrisData(dataSource.dataFile);
                                parrisData.saveToCSV(new File(projectDirectory.getPath() + "/" + dataFile));
                            }
                            metadata.files.add(new File(dataFile));
                            metadata.dataColumns.add((dataSource.dataColumn + 7));
                            metadata.mappingFiles.add(new File(mappingFile));
                            metadata.codonData.add(new Boolean(true));
                            break;
                    }
                    numDataSourcesProcessed++;
                    fireDataSourceProcessed(numDataSourcesProcessed, totalDataSources, null);
                }
                metadata.name = overlay.fieldName;
                metadata.useMin = true;
                metadata.min = overlay.minValue;
                metadata.useMax = true;
                metadata.max = overlay.maxValue;
                metadata.dataTransform = new DataTransform(overlay.minValue, overlay.maxValue, overlay.transformType);
                metadata.colorGradient = overlay.colorGradient;

            } else if (overlay.type.equals("SequenceData2D")) {
                for (int i = 0; i < overlay.dataSources2D.size(); i++) {
                    System.out.println("SequenceData2D " +i);
                    DataSource2D dataSource = overlay.dataSources2D.get(i);
                    String filename = registerFile(dataSource.dataFile);
                    String dataFile = filename + "-" + dataSource.dataFile.getName() + ".matrix";
                    String mappingFile = "reference-to-" + filename + ".mapping";
                    File mappingFileFASTA = new File(projectDirectory.getPath() + "/" + dataSource.mappingFile.getName() + ".fas");
                    if (!mappingFileFASTA.exists() || !new File(projectDirectory.getPath() + "/" + mappingFile).exists()) {
  
                        ReadseqTools.convertToFastaFormat(dataSource.mappingFile, mappingFileFASTA);

                        File mappingFilePath = new File(projectDirectory.getPath() + "/" + mappingFile);
    
                        if (mapCache.filePairExists(referenceNucleotideAlignmentFASTA, mappingFileFASTA)) {
                            IO.copyFile(mapCache.getMappingFile(referenceNucleotideAlignmentFASTA, mappingFileFASTA), mappingFilePath);   
                        } else {               
                            mapCache.registerFilePair(referenceNucleotideAlignmentFASTA, mappingFileFASTA, mappingFilePath);
                            Mapping referenceToB = Mapping.createMapping(referenceNucleotideAlignmentFASTA, mappingFileFASTA, 1);   
                            referenceToB.saveMapping(mappingFilePath);
                        }
                        numDataSourcesProcessed++;
                        fireDataSourceProcessed(numDataSourcesProcessed, totalDataSources, null);
                    }
                    switch (dataSource.dataSourceType) {                            
                        case PAIRWISE_TAB_2D:
                            if (!new File(projectDirectory.getPath() + "/" + dataFile).exists()) {
                                IO.copyFile(dataSource.dataFile, new File(projectDirectory.getPath() + "/" + dataFile));
                            }
                            metadata.files.add(new File(dataFile));
                            metadata.mappingFiles.add(new File(mappingFile));
                            metadata.codonData.add(dataSource.codonPositions);
                            break;
                        default:
                            throw new Exception("Matrix type not supported.");
                    }
                }
                metadata.name = overlay.fieldName;
                metadata.type = "SequenceData2D";
                metadata.useMin = true;
                metadata.min = overlay.minValue;
                metadata.useMax = true;
                metadata.max = overlay.maxValue;
                metadata.dataTransform = new DataTransform(overlay.minValue, overlay.maxValue, overlay.transformType);
                metadata.colorGradient = overlay.colorGradient;
            } else if (overlay.type.equals("NucleotideComposition")) {
                metadata.name = overlay.fieldName;
                metadata.type = "NucleotideComposition";
                String filename = registerFile(overlay.nucleotideAlignmentFile);
                String dataFile = filename + "-" + overlay.nucleotideAlignmentFile.getName() + ".fas";
                String mappingFile = "reference-to-" + filename + ".mapping";
                File nucleotideFileFASTA = new File(projectDirectory.getPath() + "/" + dataFile);
                if (!nucleotideFileFASTA.exists() || !new File(projectDirectory.getPath() + "/" + mappingFile).exists()) {
                    ReadseqTools.convertToFastaFormat(overlay.nucleotideAlignmentFile, nucleotideFileFASTA);                 
                    File mappingFilePath = new File(projectDirectory.getPath() + "/" + mappingFile);
                    if (mapCache.filePairExists(referenceNucleotideAlignmentFASTA, nucleotideFileFASTA)) {
                        IO.copyFile(mapCache.getMappingFile(referenceNucleotideAlignmentFASTA, nucleotideFileFASTA), mappingFilePath);
                    } else {
                        mapCache.registerFilePair(referenceNucleotideAlignmentFASTA, nucleotideFileFASTA, mappingFilePath);
                        Mapping referenceToB = Mapping.createMapping(referenceNucleotideAlignmentFASTA, nucleotideFileFASTA, 1);
                        referenceToB.saveMapping(mappingFilePath);
                    }
                }
                metadata.files.add(new File(dataFile));
                metadata.mappingFiles.add(new File(mappingFile));
                numDataSourcesProcessed++;
                fireDataSourceProcessed(numDataSourcesProcessed, totalDataSources, null);
            }

            s.metadata.add(metadata);
        }

        StructureCollectionStAXWriter.saveStructureCollectionToFile(s, new File(projectDirectory.getPath() + "/collection.xml"));
    }

    public String getNextFileName(String filePath, String startsWith) {
        for (int i = 0;; i++) {
            if (!new File(filePath + "/" + (startsWith + i)).exists()) {
                return startsWith + i;
            }
        }
    }

    public String getNextName(String startWith) {
        for (int i = 0;; i++) {
            String namei = startWith + i;
            boolean match = false;
            for (int j = 0; j < overlays.size(); j++) {
                if (overlays.get(j).fieldName.equalsIgnoreCase(namei)) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                return namei;
            }
        }
    }

    public static double getMin(double[] data) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < data.length; i++) {
            min = Math.min(data[i], min);
        }
        return min;
    }

    public static double getMax(double[] data) {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < data.length; i++) {
            max = Math.max(data[i], max);
        }
        return max;
    }
    HashMap<File, String> fileMap = new HashMap<File, String>();
    int fileid = 0;
    DecimalFormat df3 = new DecimalFormat("000");

    public String registerFile(File file) {
        String filename = fileMap.get(file);
        if (filename == null) {
            filename = df3.format(fileid);
            fileMap.put(file, filename);
            fileid++;
        }
        return filename;
    }

    public boolean fileExists(File file) {
        return fileMap.containsKey(file);
    }

    public File getReferenceAlignment() {
        return referenceNucleotideAlignment;
    }

    public void setReferenceAlignment(File referenceAlignment) {
        File oldReferenceAlignment = this.referenceNucleotideAlignment;
        this.referenceNucleotideAlignment = referenceAlignment;
        fireReferenceAlignmentChanged(oldReferenceAlignment, referenceAlignment);
    }
    EventListenerList listenerList = new EventListenerList();

    public void addDataModelListener(DataModelListener l) {
        listenerList.add(DataModelListener.class, l);
    }

    public void removeDataModelListener(DataModelListener l) {
        listenerList.remove(DataModelListener.class, l);
    }

    public void fireReferenceAlignmentChanged(File oldReferenceAlignment, File newReferenceAlignment) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == DataModelListener.class) {
                ((DataModelListener) listeners[i + 1]).referenceAlignmentChanged(oldReferenceAlignment, newReferenceAlignment);
            }
        }
    }

    public void fireDataSourceProcessed(int n, int total, Object dataSource) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == DataModelListener.class) {
                ((DataModelListener) listeners[i + 1]).dataSourceProcessed(n, total, dataSource);
            }
        }
    }
}
