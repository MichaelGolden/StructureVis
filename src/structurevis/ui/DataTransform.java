/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

/**
 *
 * @author Michael Golden
 */
public class DataTransform {

    public enum TransformType {
        LINEAR, // transform data between 0 and 1
        EXPLOG, // transform data using e^(logx), useful for pvalues
        IDENTITY
    };
    public double min;
    public double max;
    public TransformType type;

    public DataTransform(double min, double max, TransformType type) {
        this.min = min;
        this.max = max;
        this.type = type;
    }

    public float transform(float x) {
        return (float) transform((double) x);
    }

    public double transform(double x) {
        switch (type) {
            case IDENTITY:
                return x;
            case LINEAR:
                return (x - min) / (max - min);
            case EXPLOG:
                double q = Math.log10(1 / 255.0); // calcuate last value in colour range to display
                double minp = (Math.log10(max) - Math.log10(min));
                double scale = q / minp / 1.75;
                double f = Math.exp((Math.log10(x) - Math.log10(min)) * scale);
                return 1-f;
        }
        return 0;
    }

    public double inverseTransform(double y) {
        switch (type) {
            case IDENTITY:
                return y;
            case LINEAR:
                return (y * (max - min)) + min;
            case EXPLOG:
                double q = Math.log10(1 / 255.0); // calcuate last value in colour range to display
                double minp = (Math.log10(max) - Math.log10(min));
                double scale = q / minp / 1.75;
                return Math.min(Math.pow(10, (Math.log(1 - y) / scale) + Math.log10(min)), max);
        }
        return 0;
    }
}
