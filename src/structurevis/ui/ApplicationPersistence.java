/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import structurevis.data.Mapping;

/**
 *
 * @author Michael Golden
 */
public class ApplicationPersistence {

    public File persistenceFile;
    Hashtable<String, String> map = new Hashtable<String, String>();

    public enum OS {

        WINDOWS, LINUX, MAC
    };
    OS defaultOS = OS.WINDOWS;
    OS currentOS = OS.WINDOWS;
    String lastDatasetUsedKey = "lastUsedDataset";

    public ApplicationPersistence(File persistenceFile) {
        this.persistenceFile = persistenceFile;
        this.currentOS = getOS();
        load();
    }

    public void load() {
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(persistenceFile));
            String textline = null;
            while ((textline = buffer.readLine()) != null) {
                String[] split = textline.split("=");
                if (split.length >= 2) {
                    String key = split[0];
                    String value = split[1];
                    map.put(key, value);
                }
            }
            buffer.close();
        } catch (Exception ex) {
        }

        switch (currentOS) {
            case WINDOWS:
                Mapping.setMuscleExecutable("muscle3.8.31_i86win32.exe");
                break;
            case LINUX:
                Mapping.setMuscleExecutable("./muscle3.8.31_i86linux32");
                break;
            case MAC:
                Mapping.setMuscleExecutable("./muscle3.8.31_i86darwin32");
                break;
        }
    }

    public void save(File persistenceFile) {
        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(persistenceFile));
            Enumeration<String> keys = map.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                buffer.write(key + "=" + map.get(key));
                buffer.newLine();
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setLastDatasetUsed(File lastUsedDataset) {
        this.map.put(lastDatasetUsedKey, lastUsedDataset.getAbsolutePath());
    }

    public File getLastDatasetUsed() {
        if (map.get(lastDatasetUsedKey) == null) {
            return null;
        }

        return new File(map.get(lastDatasetUsedKey));
    }

    /*
     * public String [] getRNAPlotCommandForOS(String dbnFile) { switch
     * (currentOS) { case WINDOWS: String[] cmd = {"/bin/bash","-c","./RNAplot
     * -t 1 -o ps < temp.dbn"}; //return "cmd /c RNAplot -t 1 -o ps < " +
     * dbnFile; case LINUX: return "/bin/bash -c ./RNAplot -t 1 -o ps < " +
     * dbnFile; case MAC: return "/bin/bash -c ./RNAplot -t 1 -o ps < " +
     * dbnFile; } return null;
    }
     */
    public Process execRNAPlot(String dbnFile) throws IOException {
        Process p = null;
        if (currentOS.equals(OS.WINDOWS)) {
            p = Runtime.getRuntime().exec("cmd /c RNAplot -t 1 -o ps < " + dbnFile);
        } else if (currentOS.equals(OS.LINUX) || currentOS.equals(OS.MAC)) {
            String[] cmd = {"/bin/bash", "-c", "./RNAplot -t 1 -o ps < temp.dbn"};
            p = Runtime.getRuntime().exec(cmd);
        }
        return p;
    }

    public OS getOS() {
        if (isWindows()) {
            return OS.WINDOWS;
        } else if (isMac()) {
            return OS.MAC;
        } else if (isUnix()) {
            return OS.LINUX;
        }

        return defaultOS;
    }

    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        String os = System.getProperty("os.name").toLowerCase();
        // linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

    }

    public static boolean isSolaris() {

        String os = System.getProperty("os.name").toLowerCase();
        // Solaris
        return (os.indexOf("sunos") >= 0);

    }
}
