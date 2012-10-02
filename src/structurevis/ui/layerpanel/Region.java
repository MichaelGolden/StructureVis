package structurevis.ui.layerpanel;

import java.awt.Color;

/**
 *
 * @author Michael Golden
 */
public class Region {

    public int start;
    public int end;
    public String name;
    public Color color = Color.white;
    public int level = 0;

    public Region(int start, int end, String name) {
        this.start = start;
        this.end = end;
        this.name = name;
    }

    public String toString() {
        return start + ", " + end + ", " + name;
    }
}
