package org.esa.nest.gpf;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductUtils;
import org.esa.snap.datamodel.Unit;
import org.esa.snap.gpf.OperatorUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liis on 25.03.15.
 */

@OperatorMetadata(alias = "Mowing-Detection",
        category = "Image Analysis/Feature Extraction",
        authors = "Liis Reisberg",
        copyright = "Copyright (C) 2015",
        description = "Detect mowed areas.", internal = true)

public class MowingDetectionOp extends Operator{
    @SourceProduct(alias = "source")
    private Product sourceProduct;
    @TargetProduct
    private Product targetProduct = null;

    @Parameter(description = "The list of source bands.", alias = "sourceBands", itemAlias = "band",
            rasterDataNodeType = Band.class, label = "Source Bands")
    private String[] sourceBandNames = null;

    private final HashMap<String, String> targetBandNameToSourceBandName = new HashMap<String, String>();


    public static final String MASK_NAME = "_Mow";

    @Override
    public void initialize() throws OperatorException {

        try {

            createTargetProduct();

        } catch (Throwable e) {
            OperatorUtils.catchOperatorException(getId(), e);
        }
    }



    /**
     * Create target product.
     *
     * @throws Exception The exception.
     */
    private void createTargetProduct() throws Exception {

        targetProduct = new Product(sourceProduct.getName(),
                sourceProduct.getProductType(),
                sourceProduct.getSceneRasterWidth(),
                sourceProduct.getSceneRasterHeight());

        ProductUtils.copyProductNodes(sourceProduct, targetProduct);

        addSelectedBands();
    }



    /**
     * Add the user selected bands to target product.
     *
     * @throws org.esa.beam.framework.gpf.OperatorException The exceptions.
     */
    private void addSelectedBands() throws OperatorException {

        /*
        if (sourceBandNames == null || sourceBandNames.length == 0) { // if user did not select any band
            final Band[] bands = sourceProduct.getBands();
            final List<String> bandNameList = new ArrayList<String>(sourceProduct.getNumBands());
            for (Band band : bands) {
                if (band.getUnit() != null && band.getUnit().equals(Unit.INTENSITY)) {
                    bandNameList.add(band.getName());
                    if (bandNameList.size() == 2)    //----------
                        break;                      //----------
                }   //----------
            }
           sourceBandNames = bandNameList.toArray(new String[bandNameList.size()]);
        }





        final Band[] sourceBands = new Band[sourceBandNames.length];
        for (int i = 0; i < sourceBandNames.length; i++) {
            final String sourceBandName = sourceBandNames[i];
            final Band sourceBand = sourceProduct.getBand(sourceBandName);
            if (sourceBand == null) {
                throw new OperatorException("Source band not found: " + sourceBandName);
            }
            sourceBands[i] = sourceBand;
        }

        for (Band srcBand : sourceBands) {
            final String srcBandNames = srcBand.getName();
            final String unit = srcBand.getUnit();
            if (unit == null) {
                throw new OperatorException("band " + srcBandNames + " requires a unit");
            }

            if (unit.contains(Unit.IMAGINARY) || unit.contains(Unit.REAL) || unit.contains(Unit.PHASE)) {
                throw new OperatorException("Please select amplitude or intensity band");
            }

            final String targetBandName = srcBandNames + MASK_NAME;
            targetBandNameToSourceBandName.put(targetBandName, srcBandNames);

            final Band targetBand = ProductUtils.copyBand(srcBandNames, sourceProduct, targetProduct, false);
            targetBand.setSourceImage(srcBand.getSourceImage());
        }

        final Band mstBand = targetProduct.getBandAt(0);
        final Band slvBand = targetProduct.getBandAt(1);
        final Band terrainMask = targetProduct.getBand("Terrain_Mask");



        //create Mask
        String expression = "(" + mstBand.getName() + " < 0.05 && " + mstBand.getName() + " > 0)";
        if (slvBand != null) {
            expression += " && !(" + slvBand.getName() + " < 0.05 && " + slvBand.getName() + " > 0)";
        }



        if (terrainMask != null) {
            expression += " && " + terrainMask.getName() + " == 0";
        }

        final Mask mask = new Mask(mstBand.getName() + "_mowing",
                mstBand.getSceneRasterWidth(),
                mstBand.getSceneRasterHeight(),
                Mask.BandMathsType.INSTANCE);

        mask.setDescription("Flood");
        mask.getImageConfig().setValue("color", Color.BLUE);
        mask.getImageConfig().setValue("transparency", 0.7);
        mask.getImageConfig().setValue("expression", expression);
        mask.setNoDataValue(0);
        mask.setNoDataValueUsed(true);
        targetProduct.getMaskGroup().add(mask);
        */
    }

    /**
     * Operator SPI.
     */
    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MowingDetectionOp.class);
        }
    }
}
