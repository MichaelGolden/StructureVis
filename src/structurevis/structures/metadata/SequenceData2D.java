package structurevis.structures.metadata;

import java.io.File;
import structurevis.data.Mapping;
import structurevis.data.SparseMatrix;
import structurevis.data.SparseMatrix.Index2D;
import structurevis.data.SparseMatrix.MatrixIterator;

/**
 *
 * @author Michael Golden
 */
public class SequenceData2D extends MetadataFromFile {

    public SparseMatrix matrix;
    public SparseMatrix matrixTranspose;
    public double min = Float.MAX_VALUE;
    public double max = Float.MIN_VALUE;

    private SequenceData2D() {
        this.type = "SequenceData2D";
    }

    public SequenceData2D(String name) {
        this.type = "SequenceData2D";
        this.name = name;
    }

    public static SequenceData2D getMappedNucleotideData2D(SparseMatrix unmappedDataB, Mapping mapping) {
        SequenceData2D nd = new SequenceData2D();
        nd.matrix = new SparseMatrix(mapping.getALength(), unmappedDataB.emptyValue);
        MatrixIterator it = unmappedDataB.getMatrixIterator();
        while (it.hasNext()) {
            Index2D index = it.next();
            int i = index.i;
            int j = index.j;
            int x = mapping.bToA(i);
            int y = mapping.bToA(j);

            if (x != -1 && y != -1) {
                double val = unmappedDataB.get(i, j);
                if (val != unmappedDataB.emptyValue) {
                    nd.matrix.set(x, y, unmappedDataB.get(i, j));
                    nd.min = Math.min(nd.min, val);
                    nd.max = Math.min(nd.max, val);
                }
            }

        }
        nd.matrixTranspose = nd.matrix.transpose();
        return nd;
    }

    public static SequenceData2D loadFromSparseMatrixFile(File matrixFile, Mapping mapping) throws Exception {
        SequenceData2D data = getMappedNucleotideData2D(SparseMatrix.loadSparseMatrixFromFile(matrixFile, "\t", true), mapping);
        return data;
    }
/*
    @Override
    public boolean canFree() {
        return true;
    }

    @Override
    public void load(File file, Mapping mapping) {
        DataHolder.mapping = mapping;
        loadFromSparseMatrixFile(file, mapping);
    }

    @Override
    public void free() {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
    
    @Override
    public Object getData()
    {
        return null;
    }

    @Override
    public boolean canFree() {
        return false;
    }

    @Override
    public void load(File file, Mapping mapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void free() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
