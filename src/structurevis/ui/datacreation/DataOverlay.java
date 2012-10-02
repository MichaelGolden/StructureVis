/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.datacreation;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import structurevis.structures.metadata.NucleotideComposition;
import structurevis.ui.ColorGradient;
import structurevis.ui.DataTransform;

/**
 *
 * @author Michael Golden
 */
class DataOverlay {

    ArrayList<DataSource1D> dataSources1D = new ArrayList<DataSource1D>();
    ArrayList<DataSource2D> dataSources2D = new ArrayList<DataSource2D>();
    File nucleotideAlignmentFile = null;
    String fieldName = "";
    DataTransform.TransformType transformType = DataTransform.TransformType.LINEAR;
    double minValue = 0;
    double maxValue = 1;
    ColorGradient colorGradient = new ColorGradient(Color.blue, Color.white, Color.green);
    String type = "SequenceData1D";
    boolean useMin = true;
    boolean useMax = true;
}
