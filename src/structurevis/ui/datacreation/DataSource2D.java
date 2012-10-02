/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.datacreation;

import java.io.File;
import structurevis.ui.datacreation.DataModel.DataSourceType;

/**
 *
 * @author Michael Golden
 */
public class DataSource2D {

    File dataFile = new File("");
    File mappingFile = new File("");
    DataSourceType dataSourceType = DataSourceType.PAIRWISE_TAB_2D;
    int dataFileType = 0;
    boolean codonPositions = false;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataSource2D other = (DataSource2D) obj;
        if (this.dataFile != other.dataFile && (this.dataFile == null || !this.dataFile.equals(other.dataFile))) {
            return false;
        }
        if (this.dataSourceType != other.dataSourceType) {
            return false;
        }
        return true;
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.dataFile != null ? this.dataFile.hashCode() : 0);
        hash = 11 * hash + (this.dataSourceType != null ? this.dataSourceType.hashCode() : 0);
        return hash;
    }
    

}
