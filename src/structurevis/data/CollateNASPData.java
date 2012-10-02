/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.data;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import structurevis.structures.StructureCollection;
import structurevis.structures.StructureParser;
import structurevis.structures.io.StructureCollectionStAXWriter;
import structurevis.ui.layerpanel.GenomeOrganization;
import structurevis.structures.metadata.MetadataFromFile;
import structurevis.structures.metadata.SequenceData1D;
import structurevis.ui.ColorGradient;
import structurevis.ui.DataTransform;
import structurevis.ui.DataTransform.TransformType;

/**
 *
 * @author Michael Golden
 */
public class CollateNASPData {

  /*  public static void main(String[] args) {

       // FileSpecifier fs = new FileSpecifier(new File("D:/Nasp/BFDV/bfdv.txt"), "bfdv");
         FileSpecifier fs = new FileSpecifier(new File("C:/project/hepacivirus/hepacivirus2.txt"), "hepacivirus");

        File outputFolder = new File("collate3/");

        // create structure collection
        StructureCollection s = StructureParser.parseNaspFiles(fs.naspStructuresFile, fs.naspCtFile);
        String consensusStructure = StructureParser.parseNaspCtFile(fs.naspCtFile).getDotBracketString();
        s.dnaSequence = false; // ?
        s.genomeLength = consensusStructure.length();
        s.dotBracketStructure = consensusStructure;
        s.circularGenome = false; // ?
        s.metadata.add(new GenomeOrganization());

        // copy files into workspace
        IO.copyFile(fs.naspAlignmentFile, new File(outputFolder.getPath() + "/genome.fas"));
        IO.copyFile(fs.coevolutionAlignmentFile, new File(outputFolder.getPath() + "/co-evolution.fas"));
        IO.copyFile(fs.naspCtFile, new File(outputFolder.getPath() + "/nasp.ct"));
        if(fs.naspCtWithPvaluesFile != null)
        {
            IO.copyFile(fs.naspCtWithPvaluesFile, new File(outputFolder.getPath() + "/nasp-pvals.ct"));
        }

        // save synonymous substitution rates
        MetadataFromFile dsRatesMetada = new MetadataFromFile();
        MetadataFromFile omegaRatesMetada = new MetadataFromFile();
        for (int i = 0; i < fs.geneAlignmentFiles.size(); i++) {
            Mapping genomeToGene = Mapping.createMapping(fs.naspAlignmentFile, fs.geneAlignmentFiles.get(i), 1);
            genomeToGene.saveMapping(new File(outputFolder.getPath() + "/genome-to-gene" + i + ".mapping"));
            File geneFile = new File(outputFolder.getPath() + "/gene" + i + ".fas");
            IO.copyFile(fs.geneAlignmentFiles.get(i), geneFile);
            new ParrisData(fs.parrisMarginalsFiles.get(i)).saveToCSV(new File(outputFolder.getPath() + "/gene" + i + ".parris"));
            dsRatesMetada.files.add(new File("/gene" + i + ".parris"));
            dsRatesMetada.mappingFiles.add(new File("genome-to-gene" + i + ".mapping"));
            omegaRatesMetada.files.add(new File("/gene" + i + ".parris"));
            omegaRatesMetada.mappingFiles.add(new File("genome-to-gene" + i + ".mapping"));
        }
        dsRatesMetada.name = "Synonymous Substitution Rates";
        dsRatesMetada.type = "SequenceData1D";
        dsRatesMetada.useMin = true;
        dsRatesMetada.min = 0;
        dsRatesMetada.useMax = true;
        dsRatesMetada.max = 2;
        dsRatesMetada.colorGradient = new ColorGradient(Color.BLUE, Color.WHITE, Color.GREEN);
        dsRatesMetada.dataTransform = new DataTransform(0, 2, DataTransform.TransformType.LINEAR);
        dsRatesMetada.codonData = true;
        dsRatesMetada.dataColumns.add(10);
        s.metadata.add(dsRatesMetada);

        omegaRatesMetada.name = "Omega Rates";
        omegaRatesMetada.type = "SequenceData1D";
        omegaRatesMetada.useMin = true;
        omegaRatesMetada.min = 0;
        omegaRatesMetada.useMax = true;
        omegaRatesMetada.max = 2;
        omegaRatesMetada.colorGradient = new ColorGradient(Color.RED, Color.WHITE, Color.GREEN);
        omegaRatesMetada.dataTransform = new DataTransform(0, 2, DataTransform.TransformType.LINEAR);
        omegaRatesMetada.codonData = true;
        omegaRatesMetada.dataColumns.add(9);
        s.metadata.add(omegaRatesMetada);

        // save co-evolution
        Mapping genomeToCoevolution = Mapping.createMapping(fs.naspAlignmentFile, fs.coevolutionAlignmentFile, 1, false);
        genomeToCoevolution.saveMapping(new File(outputFolder.getPath() + "/genome-to-coevolution.mapping"));
        SparseMatrix coevolutionMatrixFormation = CompareCoevolution.getCoevolutionMatrix(fs.coevolutionDir, 3);
        coevolutionMatrixFormation.truncateRows();
        coevolutionMatrixFormation.saveSparseMatrixToFile(new File(outputFolder.getPath() + "/coevolution-formation.matrix"));
        SparseMatrix coevolutionMatrixAvoidance = CompareCoevolution.getCoevolutionMatrix(fs.coevolutionDir, 4);
        coevolutionMatrixAvoidance.truncateRows();
        coevolutionMatrixAvoidance.saveSparseMatrixToFile(new File(outputFolder.getPath() + "/coevolution-avoidance.matrix"));

        MetadataFromFile coevolutionFormationMetadata = new MetadataFromFile();
        coevolutionFormationMetadata.files.add(new File("coevolution-formation.matrix"));
        coevolutionFormationMetadata.mappingFiles.add(new File("genome-to-coevolution.mapping"));
        coevolutionFormationMetadata.name = "Nucleotide Co-evolution (Formation)";
        coevolutionFormationMetadata.type = "SequenceData2D";
        coevolutionFormationMetadata.codonData = false;
        coevolutionFormationMetadata.useMin = true;
        coevolutionFormationMetadata.min = 1e-7;
        coevolutionFormationMetadata.useMax = true;
        coevolutionFormationMetadata.max = 1e-2;
        coevolutionFormationMetadata.dataTransform = new DataTransform(1e-7, 1e-2, TransformType.EXPLOG);
        coevolutionFormationMetadata.colorGradient = new ColorGradient(new Color(255, 255, 0, 75),new Color(255, 0, 0, 255));
        s.metadata.add(coevolutionFormationMetadata);

        MetadataFromFile coevolutionAvoidanceMetadata = new MetadataFromFile();
        coevolutionAvoidanceMetadata.files.add(new File("coevolution-avoidance.matrix"));
        coevolutionAvoidanceMetadata.mappingFiles.add(new File("genome-to-coevolution.mapping"));
        coevolutionAvoidanceMetadata.name = "Nucleotide Co-evolution (Avoidance)";
        coevolutionAvoidanceMetadata.type = "SequenceData2D";
        coevolutionAvoidanceMetadata.codonData = false;
        coevolutionAvoidanceMetadata.useMin = true;
        coevolutionAvoidanceMetadata.min = 1e-7;
        coevolutionAvoidanceMetadata.useMax = true;
        coevolutionAvoidanceMetadata.max = 1e-2;
        coevolutionAvoidanceMetadata.dataTransform = new DataTransform(1e-7, 1e-2, TransformType.EXPLOG);
        coevolutionAvoidanceMetadata.colorGradient = new ColorGradient(new Color(255, 255, 0, 75), new Color(255, 0, 0, 255));
        s.metadata.add(coevolutionAvoidanceMetadata);

        StructureCollectionStAXWriter.saveStructureCollectionToFile(s, new File(outputFolder.getPath() + "/collection.xml"));


    }*/
}
