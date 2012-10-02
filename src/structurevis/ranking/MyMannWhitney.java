/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ranking;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Michael
 */
public class MyMannWhitney {

    double ux = 0;
    double uy = 0;
    double nx;
    double ny;
    double N;
    double tieCorrectionFactor = 0;
    Hashtable<Double, Double> groups = new Hashtable<Double, Double>();

    public double getGroup(double value) {
        Double val = groups.get(value);
        if (val != null) {
            return val.doubleValue();
        }

        return 0;
    }

    /*
     * public MyMannWhitney(double[] x, double[] y) { this.nx = x.length;
     * this.ny = y.length; this.N = this.nx + this.ny;
     *
     * ArrayList<Pair> data = new ArrayList<Pair>(); for (int i = 0; i <
     * x.length; i++) { data.add(new Pair(x[i], "x")); }
     *
     * for (int i = 0; i < y.length; i++) { data.add(new Pair(y[i], "y")); }
     *
     * Collections.sort(data); double[] counts = new double[data.size()]; for
     * (int i = 0; i < counts.length; i++) { Pair pairi = data.get(i);
     * groups.put(pairi.val, getGroup(pairi.val) + 1); for (int j = 0; j < i;
     * j++) { Pair pairj = data.get(j);
     *
     * if (!pairi.label.equals(pairj.label) && pairi.val == pairj.val) { // deal
     * with ties counts[i] += 0.5; counts[j] += 0.5; } else if
     * (!pairi.label.equals(pairj.label) && pairi.val > pairj.val) {
     * counts[i]++; } } }
     *
     * for (int i = 0; i < counts.length; i++) { if
     * (data.get(i).label.equals("x")) { ux += counts[i]; } else { uy +=
     * counts[i]; } }
     *
     *
     * tieCorrectionFactor = 0; Enumeration<Double> keys = groups.keys(); while
     * (keys.hasMoreElements()) { double c = groups.get(keys.nextElement());
     * double q = (Math.pow(c, 3) - c) / 12; tieCorrectionFactor += q; } }
     * 
     * 
     */
   /* public MyMannWhitney(double[] x, double[] y) {
        this.nx = x.length;
        this.ny = y.length;
        this.N = this.nx + this.ny;

        ArrayList<Pair> data = new ArrayList<Pair>();
        for (int i = 0; i < x.length; i++) {
            data.add(new Pair(x[i], "x"));
        }

        for (int i = 0; i < y.length; i++) {
            data.add(new Pair(y[i], "y"));
        }

        Collections.sort(data);
        double[] counts = new double[data.size()];
        for (int i = 0; i < counts.length; i++) {
            Pair pairi = data.get(i);
            groups.put(pairi.val, getGroup(pairi.val) + 1);
            for (int j = 0; j < i; j++) {
                Pair pairj = data.get(j);

                if (!pairi.label.equals(pairj.label) && pairi.val == pairj.val) {
                    // deal with ties
                    counts[i] += 0.5;
                    counts[j] += 0.5;
                } else if (!pairi.label.equals(pairj.label) && pairi.val > pairj.val) {
                    counts[i]++;
                }
            }
        }

        for (int i = 0; i < counts.length; i++) {
            if (data.get(i).label.equals("x")) {
                ux += counts[i];
            } else {
                uy += counts[i];
            }
        }


        tieCorrectionFactor = 0;
        Enumeration<Double> keys = groups.keys();
        while (keys.hasMoreElements()) {
            double c = groups.get(keys.nextElement());
            double q = (Math.pow(c, 3) - c) / 12;
            tieCorrectionFactor += q;
        }
    }*/
    
    public MyMannWhitney(double[] x, double[] y) {
        this.nx = x.length;
        this.ny = y.length;
        this.N = this.nx + this.ny;

        for (int i = 0; i < x.length; i++) {
            groups.put(x[i], getGroup(x[i]) + 1);
        }

        for (int i = 0; i < y.length; i++) {
            groups.put(y[i], getGroup(y[i]) + 1);
        }

        double[] counts_x = new double[x.length];
        double[] counts_y = new double[y.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < y.length; j++) {
                if (x[i] == y[j]) {
                    // deal with ties
                    counts_x[i] += 0.5;
                    counts_y[j] += 0.5;
                } else if (x[i] > y[j]) {
                    counts_x[i]++;
                } else {
                    counts_y[j]++;
                }
            }
        }

        for (int i = 0; i < counts_x.length; i++) {
            ux += counts_x[i];
        }

        for (int i = 0; i < counts_y.length; i++) {
            uy += counts_y[i];
        }

        tieCorrectionFactor = 0;
        Enumeration<Double> keys = groups.keys();
        while (keys.hasMoreElements()) {
            double c = groups.get(keys.nextElement());
            double q = (Math.pow(c, 3) - c) / 12;
            tieCorrectionFactor += q;
        }
    }

    public double getTestStatistic() {
        return Math.max(ux, uy);
    }

    public double getZ() {
        double n = ((nx * ny) / (N * (N - 1)));
        double d = ((Math.pow(N, 3) - N) / 12 - tieCorrectionFactor);

        double variance = Math.sqrt(n * d);
        //System.out.println(ux + "\t" + nx + "\t" + ny + "\t" + tieCorrectionFactor + "\t" + n + "\t" + d + "\t" + variance);
        return (ux - (nx * ny / 2)) / variance;
    }
}
