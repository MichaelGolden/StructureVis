/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.data;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author Michael Golden
 */
public class MapCache {

    HashMap<FilePair, Mapping> mappingMap = new HashMap<FilePair, Mapping>();
    HashMap<FilePair, File> filePairMap = new HashMap<FilePair, File>();
    int mapid = 0;

    public void registerMap(File fileA, File fileB, Mapping mapping) {
        mappingMap.put(new FilePair(fileA, fileB), mapping);
    }

    public Mapping getMap(File fileA, File fileB)
    {
         return mappingMap.get(new FilePair(fileA, fileB));
    }

    public File getMappingFile(File fileA, File fileB) {
        return filePairMap.get(new FilePair(fileA, fileB));
    }

    public void clearMappingMap()
    {
        mappingMap.clear();
    }

    public void registerFilePair(File fileA, File fileB, File mapFile) {
        filePairMap.put(new FilePair(fileA, fileB), mapFile);
    }

    public boolean filePairExists(File fileA, File fileB) {
        return filePairMap.containsKey(new FilePair(fileA, fileB));
    }

    class FilePair {

        File fileA;
        File fileB;

        public FilePair(File fileA, File fileB) {
            this.fileA = fileA;
            this.fileB = fileB;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FilePair other = (FilePair) obj;
            if (this.fileA != other.fileA && (this.fileA == null || !this.fileA.equals(other.fileA))) {
                return false;
            }
            if (this.fileB != other.fileB && (this.fileB == null || !this.fileB.equals(other.fileB))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.fileA != null ? this.fileA.hashCode() : 0);
            hash = 29 * hash + (this.fileB != null ? this.fileB.hashCode() : 0);
            return hash;
        }
    }

    public void clearFilePairMap()
    {
        filePairMap.clear();
    }
}
