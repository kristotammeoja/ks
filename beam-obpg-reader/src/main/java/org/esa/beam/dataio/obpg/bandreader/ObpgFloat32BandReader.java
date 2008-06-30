package org.esa.beam.dataio.obpg.bandreader;

import ncsa.hdf.hdflib.HDFException;
import org.esa.beam.dataio.obpg.hdf.lib.HDF;
import org.esa.beam.framework.datamodel.ProductData;

public class ObpgFloat32BandReader extends ObpgBandReader {

    private float[] _line;
    private float min;
    private float max;
    private float fill;
    private float[] targetData;
    private int targetIdx;

    public ObpgFloat32BandReader(final int sdsId, final int layer, final boolean is3d) {
        super(sdsId, layer, is3d);
    }

    /**
     * Retrieves the data type of the band
     *
     * @return always {@link org.esa.beam.framework.datamodel.ProductData#TYPE_INT8}
     */
    @Override
    public int getDataType() {
        return ProductData.TYPE_FLOAT32;
    }

    protected void prepareForReading(final int sourceOffsetX, final int sourceOffsetY, final int sourceWidth,
                                     final int sourceHeight, final int sourceStepX, final int sourceStepY,
                                     final ProductData destBuffer) {
        fill = (float) _fillValue;
        if (_validRange == null) {
            min = Float.MIN_VALUE;
            max = Float.MAX_VALUE;
        } else {
            min = (float) _validRange.getMin();
            max = (float) _validRange.getMax();
        }
        targetData = (float[]) destBuffer.getElems();
        targetIdx = 0;
        ensureLineWidth(sourceWidth);
    }

    protected void readLine() throws HDFException {
        HDF.getWrap().SDreaddata(_sdsId, _start, _stride, _count, _line);
    }

    protected void validate(final int x) {
        final float value = _line[x];
        if (value < min || value > max) {
            _line[x] = fill;
        }
    }

    protected void assign(final int x) {
        targetData[targetIdx++] = _line[x];
    }

    private void ensureLineWidth(final int sourceWidth) {
        if ((_line == null) || (_line.length != sourceWidth)) {
            _line = new float[sourceWidth];
        }
    }
}