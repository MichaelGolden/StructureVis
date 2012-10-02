package structurevis.structures;


/**
 * Class representing the attributes of a NASP structure.
 *
 * @author Michael Golden
 */
public class NaspStructure {

    public int id;
    public int gappedStartA;
    public int gappedEndA;
    public int gappedStartB;
    public int gappedEndB;
    public int ungappedStartA;
    public int ungappedEndA;
    public int ungappedStartB;
    public int ungappedEndB;
    public int length;
    public double score;
    
    public boolean circularGenome;
    public int genomeLength;

    public NaspStructure() {
    }

    public static NaspStructure getNaspStructureFromString(String line) {
        NaspStructure s = new NaspStructure();
        s.id = Integer.parseInt(line.split(":")[0]);
        String afterColon = line.split(":")[1];
        String gappedRangeA = afterColon.split("(\\|)+")[0];
        String gappedRangeB = afterColon.split("(\\|)+")[1];
        String ungappedRangeA = afterColon.split("(\\|)+")[2];
        String ungappedRangeB = afterColon.split("(\\|)+")[3];
        s.gappedStartA = Integer.parseInt(gappedRangeA.split("-")[0].trim());
        s.gappedEndA = Integer.parseInt(gappedRangeA.split("-")[1].trim());
        s.gappedStartB = Integer.parseInt(gappedRangeB.split("-")[0].trim());
        s.gappedEndB = Integer.parseInt(gappedRangeB.split("-")[1].trim());
        s.ungappedStartA = Integer.parseInt(ungappedRangeA.split("-")[0].trim());
        s.ungappedEndA = Integer.parseInt(ungappedRangeA.split("-")[1].trim());
        s.ungappedStartB = Integer.parseInt(ungappedRangeB.split("-")[0].trim());
        s.ungappedEndB = Integer.parseInt(ungappedRangeB.split("-")[1].trim());
        s.length = Integer.parseInt(afterColon.split("(\\|)+")[4].trim());
        s.score = Double.parseDouble(afterColon.split("(\\|)+")[5].trim());
        return s;
    }

    public void print() {
        System.out.println("gapped A: " + gappedStartA + " - " + gappedEndA);
        System.out.println("gapped B: " + gappedStartB + " - " + gappedEndB);
        System.out.println("ungapp A: " + ungappedStartA + " - " + ungappedEndA);
        System.out.println("ungapp B: " + ungappedStartB + " - " + ungappedEndB);
        System.out.println("length: " + length);
        System.out.println("score: " + score);
    }

    public String toString() {
        return id + "\t" + gappedStartA + "\t" + gappedEndA + "\t" + gappedStartB + "\t" + gappedEndB + "\t" + length + "\t" + score;
    }
}