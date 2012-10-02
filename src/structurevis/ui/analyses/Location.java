/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.analyses;

/**
 *
 * @author Michael
 */
class Location implements Comparable {

    int start;
    int end;

    public Location(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int compareTo(Object o) {
        Location other = (Location) o;

        if (this.start < other.start) {
            return -1;
        } else if (this.start > other.start) {
            return 1;
        } else if (this.end < other.end) {
            return -1;
        } else if (this.end > other.end) {
            return 1;
        }

        return 0;
    }

    public String toString() {
        return start + "-" + end;
    }
}