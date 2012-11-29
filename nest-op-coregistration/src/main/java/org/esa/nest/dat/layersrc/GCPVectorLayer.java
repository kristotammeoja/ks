/*
 * Copyright (C) 2012 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.nest.dat.layersrc;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.grender.Rendering;
import com.bc.ceres.grender.Viewport;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.nest.dat.layers.ScreenPixelConverter;
import org.esa.nest.datamodel.AbstractMetadata;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the movement of GCP in a coregistered image
 *
 */
public class GCPVectorLayer extends Layer {

    private final Product product;
    private final Band band;
    private static final float lineThickness = 4.0f;
    private final List<GCPData> gcpList = new ArrayList<GCPData>(200);

    public GCPVectorLayer(PropertySet configuration) {
        super(LayerTypeRegistry.getLayerType(GCPVectorLayerType.class.getName()), configuration);
        setName("GCP Movement");
        product = (Product) configuration.getValue("product");
        band = (Band) configuration.getValue("band");

        getData();
    }

    private void getData() {
        final MetadataElement absRoot = AbstractMetadata.getAbstractedMetadata(product);
        if(absRoot != null) {
            final MetadataElement bandElem = AbstractMetadata.getBandAbsMetadata(absRoot, band.getName(), false);
            if(bandElem != null) {
                final MetadataElement warpDataElem = bandElem.getElement("WarpData");
                if(warpDataElem != null) {
                    final MetadataElement[] gcpElems = warpDataElem.getElements();
                    gcpList.clear();
                    for(MetadataElement gcpElem : gcpElems) {
                        final double mstX = gcpElem.getAttributeDouble("mst_x", 0);
                        final double mstY = gcpElem.getAttributeDouble("mst_y", 0);

                        final double slvX = gcpElem.getAttributeDouble("slv_x", 0);
                        final double slvY = gcpElem.getAttributeDouble("slv_y", 0);

                        gcpList.add(new GCPData(mstX, mstY, slvX, slvY));
                    }
                }
            }
        }
    }

    @Override
    protected void renderLayer(Rendering rendering) {
        if(gcpList.isEmpty())
            return;

        final Viewport vp = rendering.getViewport();
        final RasterDataNode raster = band;
        final ScreenPixelConverter screenPixel = new ScreenPixelConverter(vp, raster);

        if (!screenPixel.withInBounds()) {
            return;
        }

        final double zoom = rendering.getViewport().getZoomFactor();

        final Graphics2D graphics = rendering.getGraphics();
        graphics.setStroke(new BasicStroke(lineThickness));

        final double[] ipts = new double[8];
        final double[] vpts = new double[8];

        graphics.setColor(Color.RED);

        for(GCPData gcp : gcpList) {

            createArrow((int)gcp.slvX,
                        (int)gcp.slvY,
                        (int)gcp.mstX,
                        (int)gcp.mstY, 5, ipts, zoom);

            screenPixel.pixelToScreen(ipts, vpts);

            //arrowhead
            graphics.draw(new Line2D.Double(vpts[4], vpts[5], vpts[2], vpts[3]));
            graphics.draw(new Line2D.Double(vpts[6], vpts[7], vpts[2], vpts[3]));
            //body
            graphics.draw(new Line2D.Double(vpts[0], vpts[1], vpts[2], vpts[3]));
            //graphics.setColor(Color.BLUE);
            //graphics.drawOval((int)vpts[2], (int)vpts[3], 2, 2);
        }
    }

    private static void createArrow(int x, int y, int xx, int yy, int i1, double[] ipts, double zoom)
    {
        ipts[0] = x;
        ipts[1] = y;
        ipts[2] = xx;
        ipts[3] = yy;
        final double d = xx - x;
        final double d1 = -(yy - y);
        double mult = 1;//5/zoom;
        if(zoom > 2)
            mult = 1;
        double d2 = Math.sqrt(d * d + d1 * d1);
        final double d3;
        final double size = 2.0;
        if(d2 > (3.0 * i1))
            d3 = i1;
        else
            d3 = d2 / 3.0;
        if(d2 < 1.0)
            d2 = 1.0;
        if(d2 >= 1.0) {
            final double d4 = (d3 * d) / d2;
            final double d5 = -((d3 * d1) / d2);
            final double d6 = (double)xx - size * d4 * mult;
            final double d7 = (double)yy - size * d5 * mult;
            ipts[4] = (int)(d6 - d5);
            ipts[5] = (int)(d7 + d4);
            ipts[6] = (int)(d6 + d5);
            ipts[7] = (int)(d7 - d4);
        }
    }

    private static class GCPData {
        public final double mstX, mstY;
        public final double slvX, slvY;
        public GCPData(double mX, double mY, double sX, double sY) {
            this.mstX = mX;
            this.mstY = mY;
            this.slvX = sX;
            this.slvY = sY;
        }
    }
}