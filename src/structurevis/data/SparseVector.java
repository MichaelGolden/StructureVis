/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package structurevis.data;

import java.util.Hashtable;

/**
 *
 * @author Michael Golden
 */
public class SparseVector
{
    double emptyValue;
    public Hashtable<Integer, Double> table = new Hashtable<Integer, Double>();

    public SparseVector(double emptyValue)
    {
        this.emptyValue = emptyValue;
    }

    public void set(int i, double val)
    {
        if(val != emptyValue)
        {
            table.put(i, val);
        }
    }

    public double get(int i)
    {
        Double val = table.get(i);
        if(val == null)
        {
            return emptyValue;
        }
        return val.doubleValue();
    }
}
