/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import net.hanjava.svg.SVG2EMF;
import structurevis.structures.metadata.MetadataFromFile;
import structurevis.ui.DataTransform.TransformType;

/**
 *
 * @author Michael Golden
 */
public class DataLegend extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    //public JColorChooser colorChooser = new JColorChooser();
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public int orientation = HORIZONTAL;
    public String label;
    public DataTransform transform;
    public ColorGradient colorGradient;
    public ColorGradient colorGradientDefault;
    static DecimalFormat decimalFormat = new DecimalFormat("0.00");
    static DecimalFormat exponentialFormat = new DecimalFormat("0.0E0");
    double barOffsetX = 10;
    double barOffsetY = 10;
    double barWidth = 20;
    double barHeight = 0;
    int pixelsBetweenTickMarks = 25;
    Color backgroundColor = Color.LIGHT_GRAY;
    // slider settings
    double arrowWidth = 10;
    double arrowHeight = 6;
    float downSliderPercentY = 0;
    double downSliderPosY = barOffsetY;
    float upSliderPercentY = 1;
    double upSliderPosY = barOffsetY + 100;
    boolean upSliderOpen = false;
    boolean downSliderOpen = false;
    int sliderSelected = -1;
    int sliderIndicatorPosX = 0;
    int sliderIndicatorPosY = 0;
    String sliderIndicatorText = null;
    JTextPane pane = new JTextPane();
    boolean activeLegend;
    boolean edit = false;
    double colorArrowWidth = 10;
    double colorArrowHeight = 10;
    int colorSliderSelected = -1;
    int hideColorSlider = -1;
    int mousePosX = 0;
    int mousePosY = 0;
    JPopupMenu editPopupMenu = new JPopupMenu();
    JMenuItem modeItem = new JMenuItem("Edit gradient");
    JMenu colorPresetsMenu = new JMenu("Use preset gradient");
    JMenuItem blueWhiteGreenItem = new JMenuItem("Blue-white-green");
    JMenuItem orangeRedItem = new JMenuItem("Orange-red");
    JMenuItem whiteBlackItem = new JMenuItem("White-black");
    JMenuItem distributeColorsItem = new JMenuItem("Distribute colours");
    JMenuItem reverseColorsItem = new JMenuItem("Reverse gradient");
    JMenuItem useDefaultItem = new JMenuItem("Restore default gradient");
    JMenu saveImageMenu = new JMenu("Save legend image");
    JMenuItem saveAsPNGItem = new JMenuItem("Save as PNG");
    JMenuItem saveAsSVGItem = new JMenuItem("Save as SVG");
    JMenuItem saveAsEMFItem = new JMenuItem("Save as EMF");
    boolean showMissingAndFilteredData = false;
    Color filterColor = Color.lightGray;
    Color missingDataColor = Color.gray;
    int missingAndFilteredDataHeight = 50;
    int missingAndFilteredDataBlockSize = 14;
    int missingAndFilteredDataPadding = 10;

    public DataLegend() {

        modeItem.addActionListener(this);
        editPopupMenu.add(modeItem);

        // create edit popup menu
        blueWhiteGreenItem.addActionListener(this);
        orangeRedItem.addActionListener(this);
        whiteBlackItem.addActionListener(this);
        colorPresetsMenu.add(blueWhiteGreenItem);
        colorPresetsMenu.add(orangeRedItem);
        colorPresetsMenu.add(whiteBlackItem);
        editPopupMenu.add(colorPresetsMenu);

        distributeColorsItem.addActionListener(this);
        editPopupMenu.add(distributeColorsItem);

        reverseColorsItem.addActionListener(this);
        editPopupMenu.add(reverseColorsItem);

        useDefaultItem.addActionListener(this);
        editPopupMenu.add(useDefaultItem);

        saveAsPNGItem.addActionListener(this);
        saveImageMenu.add(saveAsPNGItem);
        saveAsSVGItem.addActionListener(this);
        saveImageMenu.add(saveAsSVGItem);
        saveAsEMFItem.addActionListener(this);
        saveImageMenu.add(saveAsEMFItem);
        editPopupMenu.add(saveImageMenu);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void showEditMode() {
        edit = true;
        modeItem.setText("Set range");
        repaint();
    }

    public void showRangeMode() {
        edit = false;
        modeItem.setText("Change colour gradient");
        repaint();
    }

    public void showFilteredAndMissingData(Color filteredData, Color missingData) {
        showMissingAndFilteredData = true;
        filterColor = filteredData;
        missingDataColor = missingData;
    }

    public DataLegend(String label, DataTransform transform, ColorGradient colorGradient) {
    }

    public void initialise(MetadataFromFile data) {
        initialise(data.name, data.dataTransform, data.colorGradientSecondary, data.colorGradient);
        //System.out.println("I"+data.name+"\t"+data.dataTransform.min+"\t"+data.dataTransform.max);
    }

    public void initialise(String label, DataTransform transform, ColorGradient colorGradient, ColorGradient colorGradientDefault) {
        this.activeLegend = true;
        this.label = label;
        this.transform = transform;
        this.colorGradient = colorGradient;
        this.colorGradientDefault = colorGradientDefault.clone();


        if (label != null) {
            this.setLayout(null);
            pane.setOpaque(false);
            pane.setEditable(false);
            pane.setText(label);
            this.add(pane);
            // center text
            StyledDocument doc = pane.getStyledDocument();
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);
        }

        setVisible(true);
        repaint();

    }
    int labelHash = 0;
    int[] labelPositions = new int[4];

    public void saveAsSVG(File file) throws IOException {
        BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
        buffer.write(getLegendAsSVG());
        buffer.close();
    }

    public void saveAsEMF(File file) throws IOException {
        File tempFile = new File("temp.svg");
        saveAsSVG(tempFile);
        String svgUrl = "file:///" + tempFile.getAbsolutePath();
        SVG2EMF.convert(svgUrl, file);
    }

    public void saveAsPNG(File file) {
        int panelWidth = 200;
        int panelHeight = (int) (barOffsetY * 2 + barHeight);
        Dimension d = new Dimension((int) Math.ceil(panelWidth), (int) Math.ceil(panelHeight));

        BufferedImage bufferedImage = null;
        try {
            bufferedImage = (BufferedImage) (this.createImage(d.width, d.height));
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(Color.white);
            g.fillRect(0, 0, panelWidth, panelHeight);
            StringReader reader = new StringReader(getLegendAsSVG());
            SVGCache.getSVGUniverse().clear();

            SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram(SVGCache.getSVGUniverse().loadSVG(reader, "myImage"));
            if (diagram != null) {
                diagram.render(g);
            } else {
                throw new Exception("Diagram could not be saved. Unknown reason.");
            }

            if (bufferedImage != null) {
                ImageIO.write(bufferedImage, "png", file);
            }

        } catch (SVGException ex) {
            // JOptionPane.showMessageDialog(this.mainapp, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            //    Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (Exception ex) {
            //JOptionPane.showMessageDialog(this.mainapp, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public String getLegendAsSVG() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        int barHeight = 176;
        int barWidth = 20;
        int panelWidth = 200;
        int panelHeight = (int) (barOffsetY * 2 + barHeight);

        // initialise svg
        pw.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        pw.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
        pw.println("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + panelWidth + "\" height=\"" + panelHeight + "\" style=\"fill:none;stroke-width:2\">");

        pw.println("<defs>");
        pw.println("<linearGradient id=\"grad1\" x1=\"0%\" y1=\"0%\" x2=\"0%\" y2=\"100%\">");
        for (int i = 0; i < colorGradient.colours.length; i++) {
            double perc = colorGradient.positions[i] * 100;
            Color c = colorGradient.colours[i];
            pw.println("<stop offset=\"" + perc + "%\" style=\"stop-color:rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ");stop-opacity:" + (((double) c.getAlpha()) / 255.0) + "\" />");
        }
        pw.println("</linearGradient>");
        pw.println("</defs>");
        pw.println("<rect x=\"" + barOffsetX + "\" y=\"" + barOffsetY + "\" width=\"" + barWidth + "\" height=\"" + barHeight + "\" fill=\"url(#grad1)\"/>");


        // if (!edit) {


        double min = transform.transform(transform.min);
        double max = transform.transform(transform.max);
        for (int i = 0; i < barHeight; i++) {
            double h = (double) i / (double) (barHeight - 1);
            double x = transform.inverseTransform(min + h * (max - min));
            if (i % 25 == 0) {
                int xpos = (int) (barOffsetX + barWidth);
                if (edit) {
                    xpos += arrowWidth;
                }
                int ypos = (int) (barOffsetY + h * barHeight);
                if (!edit) {
                    pw.println("<polyline points=\"" + (xpos - 1) + " " + ypos + " " + (xpos + 1) + " " + ypos + "\" style=\"stroke-width:1;stroke:#000000\"/>");
                }
                String text = formatValue(x, transform, 2);
                pw.println("<text x=\"" + (xpos + 5) + "\" y=\"" + (ypos + 3) + "\" style=\"font-size:10px;stroke:none;fill:black;\" text-anchor=\"start\">");
                pw.println("<tspan>" + text + "</tspan>");
                pw.println("</text>");
            }
        }
        int xpos = (int) (barOffsetX + barWidth);
        int ypos = (int) (barOffsetY + barHeight) / 2;
        pw.println("<text x=\"" + (xpos + 35) + "\" y=\"" + (ypos) + "\" style=\"font-size:12px;stroke:none;fill:black;\" text-anchor=\"start\">");
        pw.println("<tspan>" + label + "</tspan>");
        pw.println("</text>");
        pw.println("</svg>");
        return sw.toString();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (transform != null) {
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics();
            //g.fillR
            int width = getWidth();
            //int height = getHeight();
            int height = getHeight();
            barHeight = height - 2 * barOffsetX;

            if (this.showMissingAndFilteredData) {
                barHeight -= missingAndFilteredDataHeight;
            }

            if (label != null) {
                int labelOffsetX = fm.stringWidth("######") + 5;
                labelPositions[0] = (int) (barOffsetX + barWidth + labelOffsetX);
                labelPositions[1] = (int) (barOffsetY + (barHeight - pane.getPreferredSize().height) / 2);
                labelPositions[2] = width - (int) (barOffsetX + barWidth + labelOffsetX);
                labelPositions[3] = pane.getPreferredSize().height;
                if (Arrays.hashCode(labelPositions) != labelHash) {
                    pane.setLocation(labelPositions[0], labelPositions[1]);
                    pane.setSize(labelPositions[2], labelPositions[3]);
                    labelHash = Arrays.hashCode(labelPositions);
                }
            }

            //g.setColor(backgroundColor);
            // g.fillRect(0, 0, width, height);
            // draw gradient
            double min = transform.transform(transform.min);
            double max = transform.transform(transform.max);
            for (int i = 0; i < barHeight; i++) {
                double h = (double) i / (barHeight - 1);
                double x = min + h * (max - min);
                Rectangle2D.Double rect = new Rectangle2D.Double(barOffsetX, barOffsetY + h * barHeight, barWidth, 2);
                g.setColor(colorGradient.getColor((float) x));
                g.fill(rect);
            }

            g.setColor(Color.black);
            // if (!edit) {
            for (int i = 0; i < barHeight; i++) {
                double h = (double) i / barHeight;
                double x = transform.inverseTransform(min + h * (max - min));
                if (i % pixelsBetweenTickMarks == 0) {
                    int xpos = (int) (barOffsetX + barWidth);
                    if (edit) {
                        xpos += arrowWidth;
                    }
                    int ypos = (int) (barOffsetY + h * barHeight);
                    if (!edit) {
                        g.drawLine(xpos - 2, ypos, xpos + 1, ypos);
                    }
                    g.drawString(formatValue(x, transform, 2), xpos + 5, ypos + fm.getAscent() / 2);
                }
            }

            if (!edit) {
                downSliderPosY = barOffsetY + downSliderPercentY * barHeight - arrowHeight;
                upSliderPosY = barOffsetY + upSliderPercentY * barHeight + arrowHeight;

                if (downSliderOpen) {
                    fillVerticalArrow(g, barOffsetX + (barWidth / 2), downSliderPosY + arrowHeight, arrowWidth, arrowHeight, UP, Color.white);
                } else {
                    fillVerticalArrow(g, barOffsetX + (barWidth / 2), downSliderPosY, arrowWidth, arrowHeight, DOWN, Color.black);
                }
                if (upSliderOpen) {
                    fillVerticalArrow(g, barOffsetX + (barWidth / 2), upSliderPosY - arrowHeight, arrowWidth, arrowHeight, DOWN, Color.white);
                } else {
                    fillVerticalArrow(g, barOffsetX + (barWidth / 2), upSliderPosY, arrowWidth, arrowHeight, UP, Color.black);
                }

                if (sliderIndicatorText != null) {
                    g.setColor(Color.white);
                    int stringWidth = fm.stringWidth(sliderIndicatorText);
                    int blockHeight = fm.getHeight() + 2;
                    int blockWidth = stringWidth + 4;
                    g.fillRect(sliderIndicatorPosX - (blockWidth / 2), -blockHeight + sliderIndicatorPosY, blockWidth, blockHeight);
                    g.setColor(Color.black);
                    g.drawString(sliderIndicatorText, sliderIndicatorPosX + 2 - (blockWidth / 2), -blockHeight + sliderIndicatorPosY + blockHeight / 2 + fm.getAscent() / 2);
                }
            }

            if (edit) {
                // draw color arrows
                for (int i = 0; i < colorGradient.positions.length; i++) {
                    if (i != hideColorSlider) {
                        //Rectangle2D.Double rect = new Rectangle2D.Double(barOffsetX, barOffsetY + h * barHeight, barWidth, 2);
                        g.setColor(Color.black);
                        double ypos = colorGradient.positions[i] * barHeight;
                        //System.out.println((barOffsetX+width) + " : " (barOffsetY+ypos));
                        fillHorizontalArrow(g, barOffsetX + barWidth, barOffsetY + ypos, 10, 10, 1, colorGradient.colours[i]);
                    } else {
                        g.setColor(Color.black);
                        double ypos = colorGradient.positions[i] * barHeight;
                        //System.out.println((barOffsetX+width) + " : " (barOffsetY+ypos));
                        fillHorizontalArrow(g, mousePosX - (arrowWidth / 2), mousePosY, 10, 10, 1, colorGradient.colours[i]);
                    }
                }
            }

            if (this.showMissingAndFilteredData) {
                g.setColor(filterColor);
                g.fillRect((int) (barOffsetX + barWidth / 2) - missingAndFilteredDataBlockSize / 2, (int) barOffsetY + (int) barHeight + 13, missingAndFilteredDataBlockSize, missingAndFilteredDataBlockSize);
                g.setColor(Color.black);
                g.drawString("Filtered data", (int) (barOffsetX + barWidth / 2) - missingAndFilteredDataBlockSize / 2 + missingAndFilteredDataBlockSize + 5, (int) barOffsetY + (int) barHeight + 13 + missingAndFilteredDataBlockSize - 3);
                g.setColor(missingDataColor);
                g.fillRect((int) (barOffsetX + barWidth / 2) - missingAndFilteredDataBlockSize / 2, (int) barOffsetY + (int) barHeight + 32, missingAndFilteredDataBlockSize, missingAndFilteredDataBlockSize);
                g.setColor(Color.black);
                g.drawString("Missing data", (int) (barOffsetX + barWidth / 2) - missingAndFilteredDataBlockSize / 2 + missingAndFilteredDataBlockSize + 5, (int) barOffsetY + (int) barHeight + 32 + missingAndFilteredDataBlockSize - 3);
            }
        }

        //System.out.println(getLegendAsSVG());
    }
    public static final int UP = -1;
    public static final int DOWN = 1;

    public static String formatValue(double value, DataTransform transform, int fractionDigits) {
        if (transform.type == TransformType.LINEAR) {
            decimalFormat.setMaximumFractionDigits(fractionDigits);
            return decimalFormat.format(value);
        } else if (transform.type == TransformType.EXPLOG) {
            exponentialFormat.setMaximumFractionDigits(fractionDigits);
            return exponentialFormat.format(value);
        }
        decimalFormat.setMaximumFractionDigits(fractionDigits);
        return decimalFormat.format(value);
    }

    public void fillVerticalArrow(Graphics2D g, double x, double y, double width, double height, int direction, Color fillColor) {
        Path2D.Double arrow = new Path2D.Double();

        arrow.moveTo(x - (width / 2), y);
        arrow.lineTo(x + (width / 2), y);
        arrow.lineTo(x, y + direction * height);
        arrow.lineTo(x - (width / 2), y);

        g.setColor(fillColor);
        g.fill(arrow);
        g.setColor(Color.DARK_GRAY);
        g.draw(arrow);
    }

    public void fillHorizontalArrow(Graphics2D g, double x, double y, double width, double height, int direction, Color fillColor) {
        Path2D.Double arrow = new Path2D.Double();

        arrow.moveTo(x, y);
        arrow.lineTo(x + direction * width, y - (height / 2));
        arrow.lineTo(x + direction * width, y + (height / 2));
        arrow.lineTo(x, y);

        g.setColor(fillColor);
        g.fill(arrow);
        g.setColor(Color.DARK_GRAY);
        g.draw(arrow);
    }

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.getClickCount() >= 2) {
                if (edit) {
                    // if edit show ColorChooser
                    if (colorSliderSelected != -1) {
                        Color c = JColorChooser.showDialog(this, "Pick a color", colorGradient.colours[colorSliderSelected]);
                        if (c != null) {
                            colorGradient.colours[colorSliderSelected] = c;
                        }
                        colorSliderSelected = -1;
                    }
                } else {
                    // if not edit close/open arrow
                    if (e.getY() >= downSliderPosY && e.getY() < downSliderPosY + 1.5 * arrowHeight) {
                        // down
                        downSliderOpen = !downSliderOpen;
                    } else if (e.getY() > upSliderPosY - 1.5 * arrowHeight && e.getY() <= upSliderPosY) {
                        upSliderOpen = !upSliderOpen;
                    }
                }

            } else if (edit && colorSliderSelected == -1) {
                if (e.getX() >= barOffsetX + barWidth && e.getX() < barOffsetX + barWidth + arrowWidth * 1.5) {
                    Color[] colors = new Color[colorGradient.colours.length + 1];
                    float[] positions = new float[colorGradient.colours.length + 1];
                    int r = 0;
                    float ypos = Math.max(Math.min((float) ((e.getY() - barOffsetY) / barHeight), 1), 0);
                    for (int i = 0; i < colorGradient.colours.length; i++) {
                        if (ypos < colorGradient.positions[i]) {
                            colors[r] = colorGradient.getColor(ypos);
                            positions[r] = ypos;
                            r++;
                            ypos = Float.MAX_VALUE;
                        }
                        colors[r] = colorGradient.colours[i];
                        positions[r] = colorGradient.positions[i];
                        r++;
                    }

                    if (ypos != Float.MAX_VALUE) {
                        colors[r] = colorGradient.getColor(ypos);
                        positions[r] = ypos;
                    }
                    colorGradient.colours = colors;
                    colorGradient.positions = positions;
                }
            }
            repaint();
            fireChangeEvent(new ChangeEvent(this));
        }

    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (!edit) {
                if (e.getY() >= downSliderPosY && e.getY() < downSliderPosY + 1.5 * arrowHeight) {
                    sliderSelected = DOWN;
                } else if (e.getY() > upSliderPosY - 1.5 * arrowHeight && e.getY() <= upSliderPosY) {
                    sliderSelected = UP;
                }
            } else {
                colorSliderSelected = -1;
                for (int i = 0; i < colorGradient.positions.length; i++) {
                    double x = barOffsetX + barWidth;
                    double y = barOffsetY + (colorGradient.positions[i] * barHeight);
                    if (e.getY() >= y - (0.5 * colorArrowHeight) && e.getY() < y + (0.5 * colorArrowHeight)) {
                        colorSliderSelected = i;
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            fireChangeEvent(new ChangeEvent(this));
            sliderIndicatorText = null;
            repaint();

            if (hideColorSlider != -1) {

                // remove colour slider if dragged to the right
                Color[] colors = new Color[colorGradient.colours.length - 1];
                float[] positions = new float[colorGradient.colours.length - 1];
                int r = 0;
                for (int i = 0; i < colorGradient.colours.length; i++) {
                    if (hideColorSlider != i) {
                        colors[r] = colorGradient.colours[i];
                        positions[r] = colorGradient.positions[i];
                        r++;
                    }
                }
                colorGradient.colours = colors;
                colorGradient.positions = positions;
                hideColorSlider = -1;
                colorSliderSelected = -1;
            }
            fireChangeEvent(new ChangeEvent(this));
        } else if (SwingUtilities.isRightMouseButton(e)) {
            this.editPopupMenu.show(this, e.getX(), e.getY());
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (!edit) {
            if (sliderSelected == DOWN) {
                downSliderPercentY = Math.max(Math.min((float) ((e.getY() - barOffsetY) / barHeight), 1), 0);
                upSliderPercentY = Math.max(upSliderPercentY, downSliderPercentY);

                sliderIndicatorPosX = e.getX();
                sliderIndicatorPosY = e.getY();
                double min = transform.transform(transform.min);
                double max = transform.transform(transform.max);
                double x = transform.inverseTransform(min + downSliderPercentY * (max - min));
                if (transform.type == TransformType.LINEAR) {
                    sliderIndicatorText = decimalFormat.format(x);
                } else if (transform.type == TransformType.EXPLOG) {
                    sliderIndicatorText = exponentialFormat.format(x);
                } else {
                    sliderIndicatorText = decimalFormat.format(x);
                }
            } else if (sliderSelected == UP) {
                upSliderPercentY = Math.max(Math.min((float) ((e.getY() - barOffsetY) / barHeight), 1), 0);
                downSliderPercentY = Math.min(upSliderPercentY, downSliderPercentY);

                sliderIndicatorPosX = e.getX();
                sliderIndicatorPosY = e.getY();
                double min = transform.transform(transform.min);
                double max = transform.transform(transform.max);
                double x = transform.inverseTransform(min + upSliderPercentY * (max - min));
                if (transform.type == TransformType.LINEAR) {
                    sliderIndicatorText = decimalFormat.format(x);
                } else if (transform.type == TransformType.EXPLOG) {
                    sliderIndicatorText = exponentialFormat.format(x);
                } else {
                    sliderIndicatorText = decimalFormat.format(x);
                }
            }
            downSliderPosY = barOffsetY + downSliderPercentY * barHeight - arrowHeight;
            upSliderPosY = barOffsetY + upSliderPercentY * barHeight + arrowHeight;
        } else {
            if (colorSliderSelected != -1) {
                if (colorGradient.colours.length > 2 && e.getX() > barOffsetX + barWidth + arrowWidth * 1.5) {
                    hideColorSlider = colorSliderSelected;
                    //sliderSelected = -1;
                } else {
                    if (hideColorSlider == -1) {
                        // move colour slider
                        double lower = colorSliderSelected > 0 ? colorGradient.positions[colorSliderSelected - 1] : 0;
                        double upper = colorSliderSelected < colorGradient.positions.length - 1 ? colorGradient.positions[colorSliderSelected + 1] : 1;
                        double f = (e.getY() - barOffsetY) / barHeight;
                        f = Math.max(Math.min(upper, f), lower);
                        colorGradient.positions[colorSliderSelected] = (float) f;
                    } else {
                        hideColorSlider = -1;
                    }
                }
                mousePosX = e.getX();
                mousePosY = e.getY();
            }
        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    void fireChangeEvent(ChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(evt);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.modeItem)) {
            if (edit) {
                showRangeMode();
            } else {
                showEditMode();
            }
            repaint();
        } else if (e.getSource().equals(this.blueWhiteGreenItem)) {
            colorGradient = new ColorGradient(Color.blue, Color.white, Color.green);
            repaint();
        } else if (e.getSource().equals(this.orangeRedItem)) {
            colorGradient = new ColorGradient(Color.orange, Color.red);
            repaint();
        } else if (e.getSource().equals(this.whiteBlackItem)) {
            colorGradient = new ColorGradient(Color.white, Color.black);
            repaint();
        } else if (e.getSource().equals(distributeColorsItem)) {
            colorGradient.distributeColors();
            repaint();
        } else if (e.getSource().equals(reverseColorsItem)) {
            colorGradient.reverseOrder();
            repaint();
        } else if (e.getSource().equals(useDefaultItem)) {
            colorGradient = colorGradientDefault.clone();
            System.out.println("Using default");
            repaint();
        } else if (e.getSource().equals(saveAsPNGItem)) {
            String name = "legend";
            MainApp.fileChooserSave.setDialogTitle("Save as PNG");
            MainApp.fileChooserSave.setSelectedFile(new File(MainApp.fileChooserSave.getCurrentDirectory().getPath() + "/" + name + ".png"));
            int returnVal = MainApp.fileChooserSave.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                saveAsPNG(MainApp.fileChooserSave.getSelectedFile());
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            MainApp.fileChooserSave.setDialogTitle("Open");
        } else if (e.getSource().equals(saveAsSVGItem)) {
            MainApp.fileChooserSave.setDialogTitle("Save as SVG");
            String name = "legend";
            MainApp.fileChooserSave.setSelectedFile(new File(MainApp.fileChooserSave.getCurrentDirectory().getPath() + "/" + name + ".svg"));
            int returnVal = MainApp.fileChooserSave.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    saveAsSVG(MainApp.fileChooserSave.getSelectedFile());
                } catch (IOException ex) {
                    Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            MainApp.fileChooserSave.setDialogTitle("Open");
        } else if (e.getSource().equals(saveAsEMFItem)) {
            MainApp.fileChooserSave.setDialogTitle("Save as EMF");
            String name = "legend";
            MainApp.fileChooserSave.setSelectedFile(new File(MainApp.fileChooserSave.getCurrentDirectory().getPath() + "/" + name + ".emf"));
            int returnVal = MainApp.fileChooserSave.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    saveAsEMF(MainApp.fileChooserSave.getSelectedFile());
                } catch (IOException ex) {
                    Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            MainApp.fileChooserSave.setDialogTitle("Open");
        }
        fireChangeEvent(new ChangeEvent(this));
    }
}
