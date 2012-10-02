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
public class DataSource1D {

    File dataFile = new File("");
    File mappingFile = new File("");
    DataSourceType dataSourceType = DataSourceType.CSV_1D;
    String[] headers = {};
    int dataFileType = 0;
    boolean hasHeader = true;
    int positionColumn = -1;
    boolean codonPositions = false;
    int dataColumn = 0;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataSource1D other = (DataSource1D) obj;
        if (this.dataFile != other.dataFile && (this.dataFile == null || !this.dataFile.equals(other.dataFile))) {
            return false;
        }
        if (this.dataSourceType != other.dataSourceType) {
            return false;
        }
        if (this.dataColumn != other.dataColumn) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.dataFile != null ? this.dataFile.hashCode() : 0);
        hash = 47 * hash + (this.dataSourceType != null ? this.dataSourceType.hashCode() : 0);
        hash = 47 * hash + this.dataColumn;
        return hash;
    }
}
