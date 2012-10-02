/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import structurevis.structures.Structure;
import structurevis.ui.MainApp.NucleotideCompositionType;

/**
 *
 * @author Michael Golden
 */
public class StructureDrawPanel1 extends JPanel implements MouseListener, MouseMotionListener {

    public static final int SHOW = 0;
    public static final int HIDE = 1;
    public int oneDimensionalData = SHOW;
    public boolean show2DData = true;
    public static final int NASP_SHOW = 0;
    public static final int NASP_HIDE = 1;
    public int naspType = NASP_HIDE;
    //public int maxDistance = -1;
    public BufferedImage bufferedImage = null;
    public Graphics2D g = null;
    public boolean repaint = true;
    public int currentStructure = -1;
    int numStructures;
    MainApp mainapp = null;
    static ArrayList<String> sequences = new ArrayList<String>();
    static ArrayList<String> sequenceNames = new ArrayList<String>();
    static double[] weights;
    Font f1 = new Font("Arial", Font.PLAIN, 100);
    Font f2 = new Font("Arial", Font.PLAIN, 12);
    ArrayList<Interaction> covariationInteractions = new ArrayList<Interaction>();
//    AnnotatedStructure structure = null;
    final static float dash1[] = {7.0f};
    final static BasicStroke dashedStroke = new BasicStroke(2.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            7.0f, dash1, 0.0f);
    final static BasicStroke normalStroke = new BasicStroke(6f);
    boolean dnasequence = false;
    //File naspStructuresFile = new File("C:/project/hepacivirus/10seq_aligned_d0.fasta.out");
    //File naspAlignmentFile = new File("C:/project/hepacivirus/10seq_aligned_d0.fasta");
    boolean saveStructures = false;
    Structure structure = null;
    Point2D.Double[] nucleotidePositions;
    int nucleotideDiameter = 40;
    double xoffset = 150;
    ArrayList<Point2D.Double> np = null;
    double minx = Double.MAX_VALUE;
    double miny = Double.MAX_VALUE;
    double maxx = Double.MIN_VALUE;
    double maxy = Double.MIN_VALUE;

    public StructureDrawPanel1() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void initialise(MainApp mainapp) {
        this.mainapp = mainapp;
        numStructures = 1;
        nucleotidePositions = null;
        nextStructure();

    }

    public Point2D getPointAlongArc(double x, double y, double width, double height, double startAngle, double offsetAngle) {
        return new Arc2D.Double(x, y, width, height, startAngle, offsetAngle, Arc2D.OPEN).getEndPoint();
    }

    public Ellipse2D getCircleCenteredAt(double x, double y, double diameter) {
        return new Ellipse2D.Double(x - (diameter / 2), y - (diameter / 2), diameter, diameter);
    }

    public void previousStructure() {
        currentStructure = (currentStructure - 1);
        structure = mainapp.structureCollection.structures.get(currentStructure);
        if (currentStructure < 0) {
            currentStructure += numStructures;
        }
        gotoStructure(currentStructure);
    }

    public void nextStructure() {
        currentStructure = (currentStructure + 1) % numStructures;
        if (mainapp.structureCollection != null && mainapp.structureCollection.structures != null) {
            structure = mainapp.structureCollection.structures.get(currentStructure);
            gotoStructure(currentStructure);
        }
    }

    public void openStructure(Structure s) {
        structure = s;
//        mainapp.genomeLegend1.setSelectedRegion(s.startPosition - 1, s.getEndPosition() - 1);
        computeAndDraw();

        if (saveStructures) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(StructureDrawPanel1.class.getName()).log(Level.SEVERE, null, ex);
            }
            mainapp.callNext();
        }
    }

    public void gotoStructure(int i) {
        currentStructure = (i + 1) % numStructures;
        structure = mainapp.structureCollection.structures.get(currentStructure);
        //complexStructure = ComplexStructure.getComplexStructure(structure, mainapp.fs.naspStructuresFile, mainapp.fs.naspAlignmentFile);
        computeAndDraw();
    }

    public void computeAndDraw() {
        computeStructureToBeDrawn(structure);
        repaint = true;
        repaint();
    }

    public void redraw() {
        repaint = true;
        repaint();
    }

    public void computeStructureToBeDrawn(Structure structure) {
        if (structure == null) {
            return;
        }

        np = getStructureCoordinates(structure.getDotBracketString());        

        minx = Double.MAX_VALUE;
        miny = Double.MAX_VALUE;
        maxx = Double.MIN_VALUE;
        maxy = Double.MIN_VALUE;

        for (int i = 0; i < np.size(); i++) {
            Point2D.Double pos = np.get(i);
            minx = Math.min(minx, pos.x);
            miny = Math.min(miny, pos.y);
            maxx = Math.max(maxx, pos.x);
            maxy = Math.max(maxy, pos.y);
        }
        nucleotidePositions = new Point2D.Double[np.size()];
        for (int i = 0; i < nucleotidePositions.length; i++) {
            nucleotidePositions[i] = new Point2D.Double();
            nucleotidePositions[i].x = xoffset + (np.get(i).x - minx) * 3.7;
            nucleotidePositions[i].y = 50 + (np.get(i).y - miny) * 3.5;
        }

    }

    public void drawComplexStructure() {
        if (structure == null || nucleotidePositions == null) {
            return;
        }
        int panelWidth = (int) ((maxx - minx) * 4 + xoffset * 2);
        int panelHeight = (int) ((maxy - miny) * 4 + 100);
        Dimension d = new Dimension(panelWidth, panelHeight);
        setPreferredSize(d);
        revalidate();

        try {
            if ((bufferedImage == null || d.width != bufferedImage.getWidth() || d.height != bufferedImage.getHeight())) {
                bufferedImage = (BufferedImage) (this.createImage(d.width, d.height));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.mainapp,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        g = (Graphics2D) bufferedImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.white);
        g.fillRect(0, 0, d.width, d.height);

        g.setColor(Color.black);

        int length = structure.length;
        covariationInteractions.clear();
        if (show2DData && mainapp.data2D != null) {
            for (int i = structure.getStartPosition(); i <= structure.getEndPosition(); i++) {
                for (int j = structure.getStartPosition(); j <= structure.getEndPosition(); j++) {
                    if (mainapp.maxDistance == -1 || Math.abs(j - i) <= mainapp.maxDistance) {
                        Color c = null;
                        int k = i - structure.getStartPosition();
                        int l = j - structure.getStartPosition();

                        double p = mainapp.data2D.matrix.get(i - 1, j - 1);
                        if (p == mainapp.data2D.matrix.emptyValue) {
                            c = null;
                        } else if (((!mainapp.useLowerThreshold2D || p >= mainapp.thresholdMin2D) && (!mainapp.useUpperThreshold2D || p <= mainapp.thresholdMax2D))) {
                            //  Sy
                            if (mainapp.data2D != null) {
                                //System.out.println(p);
                                c = mainapp.data2D.colorGradientSecondary.getColor((float) mainapp.data2D.dataTransform.transform(p));
                            }
                        }

                        if (c != null) {
                            double x1 = nucleotidePositions[k].getX();
                            double y1 = nucleotidePositions[k].getY();
                            double x2 = nucleotidePositions[l].getX();
                            double y2 = nucleotidePositions[l].getY();

                            Shape shape = null;
                            int structureMidpoint = structure.getStartPosition() + (structure.length / 2);

                            /*if (i >= structure.gapStartA && i <= structure.gapEndA && j > structureMidpoint && j < structure.gapStartB) {
                            shape = new QuadCurve2D.Double(x1, y1, (x1 + x2) / 2, y2, x2, y2);
                            } else if (i > structure.gapEndA && i <= structureMidpoint && j > structure.gapStartB && j < structure.gapEndB) {
                            shape = new QuadCurve2D.Double(x1, y1, (x1 + x2) / 2, y1, x2, y2);
                            } else if (i >= structure.gapEndA && i <= structure.gapStartB && j >= structure.gapEndA && j <= structure.gapStartB) {
                            shape = new Line2D.Double(nucleotidePositions[k], nucleotidePositions[l]);
                            } else*/ if (i <= structureMidpoint && j <= structureMidpoint) { // both on left side
                                double x1p = Math.max(x1 - Math.abs((y1 - y2) / 2), 0);
                                shape = new QuadCurve2D.Double(x1, y1, x1p, (y1 + y2) / 2, x2, y2);
                            } else if (i > structureMidpoint && j > structureMidpoint) { // both on right side
                                double x2p = Math.min(x2 + Math.abs((y1 - y2) / 2), panelWidth);
                                shape = new QuadCurve2D.Double(x1, y1, x2p, (y1 + y2) / 2, x2, y2);
                            } else {
                                shape = new Line2D.Double(nucleotidePositions[k], nucleotidePositions[l]);
                            }
                            covariationInteractions.add(new Interaction(shape, i, j));

                            g.setColor(c);
                            g.setStroke(normalStroke);
                            g.draw(shape);

                            g.setColor(Color.black);
                            g.setStroke(new BasicStroke());
                        }
                    }
                }
            }
        }

        int[] tetraloop = new int[nucleotidePositions.length];
        /*for (int i = 0; i < nucleotidePositions.length; i++) {
        if (consensusSequence.substring(i).matches("^G[ACGT][GA]A.*")) {
        tetraloop[i] = 1;
        tetraloop[i + 1] = 1;
        tetraloop[i + 2] = 1;
        tetraloop[i + 3] = 1;
        //System.out.println("MATCH GNRA " + i);
        }
        if (consensusSequence.substring(i).matches("^A[ACGT]CG.*")) {
        tetraloop[i] = 2;
        tetraloop[i + 1] = 2;
        tetraloop[i + 2] = 2;
        tetraloop[i + 3] = 2;
        //System.out.println("MATCH ANCG " + i);
        }
        if (consensusSequence.substring(i).matches("^CAAG.*")) {
        tetraloop[i] = 3;
        tetraloop[i + 1] = 3;
        tetraloop[i + 2] = 3;
        tetraloop[i + 3] = 3;
        //System.out.println("MATCH CAAG " + i);
        }
        }*/

        // draw the nucleotides
        for (int i = 0; i < nucleotidePositions.length; i++) {
            int pos = (structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength;
            Ellipse2D stemNucleotide = getCircleCenteredAt(nucleotidePositions[i].getX(), nucleotidePositions[i].getY(), nucleotideDiameter);
            g.setColor(Color.white);
            Color nucleotideBackgroundColor = mainapp.missingDataColor;
            if (oneDimensionalData == SHOW && mainapp.data1D != null && mainapp.data1D.used[pos]) {
                double p = mainapp.data1D.data[pos];
                if (mainapp.data1D.used[pos] && ((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                    nucleotideBackgroundColor = mainapp.data1D.colorGradientSecondary.getColor(mainapp.data1D.dataTransform.transform((float) p));
                } else if (!((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                    nucleotideBackgroundColor = mainapp.filteredDataColor;
                }
                g.setColor(nucleotideBackgroundColor);
                g.fill(stemNucleotide);
                g.setColor(Color.black);
                // drawStringCentred(g, nucleotidePositions[i].getX(), nucleotidePositions[i].getY()+10, val.toString());
            } else {
                g.setColor(nucleotideBackgroundColor);
                g.fill(stemNucleotide);
            }

            if (tetraloop[i] > 0) {
                g.setColor(Color.magenta);
                g.setStroke(new BasicStroke((float) 3));
                g.draw(stemNucleotide);
            } else {
                g.setColor(Color.black);
                g.draw(stemNucleotide);
            }
            g.setStroke(new BasicStroke());

            // draw the information
            g.setColor(ColorTools.selectBestForegroundColor(nucleotideBackgroundColor, Color.white, Color.black));
            if (mainapp.nucleotideComposition != null) {
                if (mainapp.nucleotideCompositionType == NucleotideCompositionType.SHANNON) {
                    // fa = structure.shannonFrequencies[i];
                    double[] fa = Arrays.copyOf(mainapp.nucleotideComposition.mappedShannonComposition[(structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength], 5);
                    for (int k = 0; k < 4; k++) {
                        fa[k] = fa[k] / 2;
                    }
                    drawSequenceLogo(g, nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - (nucleotideDiameter / 2) + 3, nucleotideDiameter, nucleotideDiameter - 5, fa);
                    g.setFont(f2);
                } else if (mainapp.nucleotideCompositionType == NucleotideCompositionType.FREQUENCY) {
                    double[] fa = mainapp.nucleotideComposition.mappedFrequencyComposition[(structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength];
                    drawSequenceLogo(g, nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - (nucleotideDiameter / 2) + 3, nucleotideDiameter, nucleotideDiameter - 5, fa);
                    g.setFont(f2);
                }
            }
        }

        // draw position lines
        for (int i = 0; i < nucleotidePositions.length; i++) {
            int offsetx = 0;
            double side = 1;
            if (i < length / 2) {
                offsetx = -(int) (nucleotideDiameter - 3);

                side = -1;
            } else {
                offsetx = (int) (nucleotideDiameter - 3);
            }

            if (nucleotidePositions[i] != null) {
                g.setColor(Color.black);
                int pos = (structure.getStartPosition() + i - 1) % mainapp.structureCollection.genomeLength + 1;
                drawStringCentred(g, offsetx + nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - 2, "" + pos);
                g.setColor(Color.black);
                g.draw(new Line2D.Double(nucleotidePositions[i].getX() + (side * nucleotideDiameter / 2) - 2, nucleotidePositions[i].getY(), nucleotidePositions[i].getX() + (side * nucleotideDiameter / 2) + 2, nucleotidePositions[i].getY()));
            }
        }
    }

    public void saveAsPNG(File file) {
        try {
            if (bufferedImage != null) {
                ImageIO.write(bufferedImage, "png", file);
            }
        } catch (IOException ex) {
            Logger.getLogger(StructureDrawPanel1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void drawSequenceLogo(Graphics2D g, double x, double y, double width, double height, double[] h) {
        double fontHeight = g.getFontMetrics(f1).getHeight();

        double scale = (height / (fontHeight - g.getFontMetrics(f1).getDescent() + 2));
        double base = y;
        for (int i = 0; i < h.length; i++) {
            double fontHeightScale = (h[i]);
            Font tallerFont = f1.deriveFont(AffineTransform.getScaleInstance(scale, fontHeightScale * scale));

            String a = "";
            switch (i) {
                case 0:
                    a = "A";
                    break;
                case 1:
                    a = "C";
                    break;
                case 2:
                    a = "G";
                    break;
                case 3:
                    if (dnasequence) {
                        a = "T";
                    } else {
                        a = "U";
                    }
                    break;
            }
            String b = a.length() > 0 ? a : "X";
            g.setFont(tallerFont);
            base += g.getFontMetrics(tallerFont).getStringBounds(b, g).getHeight() - g.getFontMetrics().getDescent();
            g.drawString(a, (float) (x + (-g.getFontMetrics().getStringBounds(a, g).getWidth() / 2)), (float) (base));
        }
    }

    /*public static double transformPval(double pval, double maxPval, double minPval) {
    double q = Math.log(1 / 255.0);
    double min = (Math.log10(minPval) - Math.log10(maxPval));
    double scale = q / min / 2;
    double f = Math.exp((Math.log10(pval) - Math.log10(maxPval)) * scale);
    return f;
    }*/

    /*public static Color getColorForDsValue(double ds) {
    float x = (float)Math.max(Math.min(ds/2, 1), 0);
    return mainapp.data1D.colorGradient.getColor(x);
    }*/
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);


        if (repaint == true) {
            repaint = false;
            drawComplexStructure();
        }

        graphics.drawImage(bufferedImage, 0, 0, this);
        if (selectedNucleotide != -1) {
            //Ellipse2D circle = new Ellipse2D.Double(posx-2,posy-2, 4, 4);
            //graphics.setColor(Color.red);
            //graphics.fillOval((int)(posx-2),(int)(posy-2), 4, 4);
            graphics.setColor(Color.black);
            graphics.drawOval((int) posx - (nucleotideDiameter / 2), (int) posy - (nucleotideDiameter / 2), (int) (nucleotideDiameter), (int) (nucleotideDiameter));
            nucleotidePositions[selectedNucleotide] = new Point2D.Double(posx, posy);
        }
    }

    public static void drawStringCentred(Graphics2D g, double x, double y, String s) {
        FontMetrics fm = g.getFontMetrics();
        java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, g);

        int textHeight = (int) (rect.getHeight());
        int textWidth = (int) (rect.getWidth());
        double x1 = x + (-textWidth / 2);
        double y1 = y + (-textHeight / 2 + fm.getAscent());

        g.drawString(s, (float) x1, (float) y1);  // Draw the string.
    }
    double posx = -1;
    double posy = -1;

    public void mouseDragged(MouseEvent e) {
        posx = e.getX();
        posy = e.getY();
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
        mainapp.data2DLabel.setText("");
        for (int i = 0; i < covariationInteractions.size(); i++) {
            if (covariationInteractions.get(i).shape instanceof QuadCurve2D) {
                int c = 0;
                boolean[] count = new boolean[4];
                count[0] = covariationInteractions.get(i).shape.intersects(e.getX() - 2, e.getY() - 2, 4, 4);
                count[1] = covariationInteractions.get(i).shape.intersects(e.getX() + 2, e.getY() - 2, 4, 4);
                count[2] = covariationInteractions.get(i).shape.intersects(e.getX() - 2, e.getY() + 2, 4, 4);
                count[3] = covariationInteractions.get(i).shape.intersects(e.getX() + 2, e.getY() + 2, 4, 4);
                for (int k = 0; k < count.length; k++) {
                    if (count[k]) {
                        c++;
                    }
                }
                if (c > 0) {
                    //System.out.println(c);
                }
                if (c >= 1 && c <= 3) {// mouse over information
                    //Interaction interaction = covariationInteractions.get(i);
                    //mainapp.jLabel6.setText(interaction.nucleotidei + " <-> " + interaction.nucleotidej + "  =  " + structure.getCoevolutionPval(interaction.nucleotidei, interaction.nucleotidej));
                    // System.out.println("INTERACTION " + covariationInteractions.get(i));
                }
            } else {
            }
        }

    }
    int selectedNucleotide = -1;

    public void mouseClicked(MouseEvent e) {

        /*  for(int i = 0 ; ; i++)
        {
        saveAsPNG(new File("c:/project/hepacivirus/images/hcv-"+(i+1)+".png"));
        mainapp.selected = (mainapp.selected + 1) % mainapp.directoryStructureFiles.size();
        mainapp.openStructure(mainapp.selected);
        }*/
    }

    public void mousePressed(MouseEvent e) {
        int minIndex = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < nucleotidePositions.length; i++) {
            double distance = nucleotidePositions[i].distance(e.getPoint());
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }
        if (minDistance <= nucleotideDiameter / 2) {
            selectedNucleotide = minIndex;
        }
    }

    public void mouseReleased(MouseEvent e) {
        selectedNucleotide = -1;
        redraw();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    class Interaction {

        Shape shape;
        int nucleotidei;
        int nucleotidej;

        public Interaction(Shape shape, int nucleotidei, int nucleotidej) {
            this.shape = shape;
            this.nucleotidei = nucleotidei;
            this.nucleotidej = nucleotidej;
        }

        public String toString() {
            return nucleotidei + " <-> " + nucleotidej;
        }
    }

    public static Point2D.Double[] normaliseStructureCoordinates(ArrayList<Point2D.Double> coordinates) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double maxy = Double.MIN_VALUE;

        for (int i = 0; i < coordinates.size(); i++) {
            Point2D.Double pos = coordinates.get(i);
            minx = Math.min(minx, pos.x);
            miny = Math.min(miny, pos.y);
            maxx = Math.max(maxx, pos.x);
            maxy = Math.max(maxy, pos.y);
        }
        Point2D.Double normalisedPositions[] = new Point2D.Double[coordinates.size()];
        for (int i = 0; i < normalisedPositions.length; i++) {
            normalisedPositions[i] = new Point2D.Double();
            normalisedPositions[i].x = 0 + (coordinates.get(i).x - minx);
            normalisedPositions[i].y = 0 + (coordinates.get(i).y - miny);
        }
        return normalisedPositions;
    }

    public static ArrayList<Point2D.Double> getStructureCoordinates(String dotBracketString) {
        ArrayList<Point2D.Double> coordinates = new ArrayList<Point2D.Double>();

        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(new File("temp.dbn")));
            buffer.write(">temp");
            buffer.newLine();
            for (int i = 0; i < dotBracketString.length(); i++) {
                buffer.write("A");
            }
            buffer.newLine();
            buffer.write(dotBracketString);
            buffer.newLine();
            buffer.close();

            Process p = Runtime.getRuntime().exec("cmd /c RNAPlot -t 1 -o ps < temp.dbn");

            if (p.waitFor() == 0) {
                BufferedReader bufferIn = new BufferedReader(new FileReader(new File("temp_ss.ps")));
                String textline = null;
                boolean readCoord = false;
                while ((textline = bufferIn.readLine()) != null) {
                    if (textline.startsWith("/coor")) {
                        readCoord = true;
                    } else if (readCoord) {
                        String[] split = textline.substring(1).replaceAll("]", "").split("\\s");
                        if (textline.startsWith("[")) {
                            Point2D.Double point = new Point2D.Double(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                            coordinates.add(point);
                        } else {
                            readCoord = false;
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(StructureDrawPanel1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(StructureDrawPanel1.class.getName()).log(Level.SEVERE, null, ex);
        }

        return coordinates;
    }
}
