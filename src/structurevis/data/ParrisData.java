/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Michael Golden
 */
public class ParrisData {

    public int numSites = 0;
    public int numRateClasses;
    public int numSynonymousRateClasses;
    public double[] rateClasses;
    public double[][] posteriorProbabilities;
    public double[] weightedOmega;
    public double[] weightedSynonymous;
    public double[] weightedOmegaNormalised;
    public double[] weightedSynonymousNormalised;

    public ParrisData(File file) {
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(file));
            String textline = null;
            boolean readTable = false;
            int synonmousColStart = -1;
            while ((textline = buffer.readLine()) != null) {
                if (readTable) {
                    if (textline.matches("^[0-9]+.*$")) {
                        numSites++;
                    }
                }

                if (textline.matches("^Rates/Site(\\s)*Omega=.*$")) {
                    String[] split = textline.split("(\\s)+");
                    numRateClasses = split.length - 1;
                    int j = 0;
                    for (int i = 0; i < split.length; i++) {
                        if (split[i].startsWith("S=")) {
                            if (synonmousColStart == -1) {
                                synonmousColStart = i;
                            }
                            j++;
                        }
                    }
                    numSynonymousRateClasses = j;
                    rateClasses = new double[numRateClasses];
                    for (int i = 0; i < numRateClasses; i++) {
                        rateClasses[i] = Double.parseDouble(split[i + 1].replaceAll("[a-zA-Z]+=", ""));
                    }
                    readTable = true;
                }
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        posteriorProbabilities = new double[numRateClasses][numSites];
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(file));
            String textline = null;
            boolean readTable = false;
            int j = 0;
            while ((textline = buffer.readLine()) != null) {
                if (readTable) {
                    if (textline.matches("^[0-9]+.*$")) {
                        String[] split = textline.split("(\\s)+");
                        for (int i = 0; i < numRateClasses; i++) {
                            posteriorProbabilities[i][j] = Double.parseDouble(split[i + 1]);
                        }
                        j++;
                    }
                }

                if (textline.matches("^Rates/Site(\\s)*Omega=.*$")) {
                    readTable = true;
                }
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        weightedOmega = new double[numSites];
        for (int j = 0; j < numSites; j++) {
            for (int i = 0; i < numRateClasses - numSynonymousRateClasses; i++) {
                weightedOmega[j] += rateClasses[i] * posteriorProbabilities[i][j];
            }
        }

        weightedSynonymous = new double[numSites];
        for (int j = 0; j < numSites; j++) {
            for (int i = numRateClasses - numSynonymousRateClasses; i < numRateClasses; i++) {
                weightedSynonymous[j] += rateClasses[i] * posteriorProbabilities[i][j];
            }
        }

        // normalise omega
        weightedOmegaNormalised = new double[numSites];
        double maxomega = 0;
        for (int i = 0; i < numRateClasses - numSynonymousRateClasses; i++) {
            maxomega = Math.max(maxomega, rateClasses[i]);
        }
        double power = Math.log(2) / Math.log(maxomega);
        for (int i = 0; i < weightedOmega.length; i++) {
            weightedOmegaNormalised[i] = Math.pow(weightedOmega[i], power);
        }

        // normalise dS
        weightedSynonymousNormalised = new double[numSites];
        double maxds = 0;
        for (int i = numRateClasses - numSynonymousRateClasses; i < numRateClasses; i++) {
            maxds = Math.max(maxds, rateClasses[i]);
        }
        power = Math.log(2) / Math.log(maxds);
        for (int i = 0; i < weightedSynonymous.length; i++) {
            weightedSynonymousNormalised[i] = Math.pow(weightedSynonymous[i], power);
        }
    }

    public void saveToCSV(File csvFile) {
        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(csvFile));
            buffer.write("Codon,");
            for (int i = 0; i < numRateClasses - numSynonymousRateClasses; i++) {
                buffer.write("Omega=" + rateClasses[i] + ",");
            }
            for (int i = numSynonymousRateClasses; i < numRateClasses; i++) {
                buffer.write("S=" + rateClasses[i] + ",");
            }
            buffer.write("Weighted Omega,Weighted dS,Weighted Omega normalised,Weighted dS normalised\n");

            for (int i = 0; i < weightedSynonymous.length; i++) {
                buffer.write((i + 1) + ",");
                for (int j = 0; j < numRateClasses; j++) {
                    buffer.write(posteriorProbabilities[j][i] + ",");
                }
                buffer.write(weightedOmega[i] + ",");
                buffer.write(weightedSynonymous[i] + ",");
                buffer.write(weightedOmegaNormalised[i] + ",");
                buffer.write(weightedSynonymousNormalised[i] + "\n");
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
