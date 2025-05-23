/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2015, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import javax.media.jai.PlanarImage;
import org.geotools.api.coverage.ColorInterpretation;
import org.geotools.api.geometry.Bounds;
import org.geotools.api.parameter.ParameterValueGroup;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.TypeMap;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.Viewer;
import org.geotools.image.ImageWorker;
import org.junit.Test;

/**
 * Tests the SelectSampleDimension operation.
 *
 * @author Simone Giannecchini (GeoSolutions)
 * @since 2.5
 */
public final class BandSelectTest extends GridProcessingTestBase {

    /** Tests the "SelectSampleDimension" operation in a simple way. */
    @Test
    public void testBandSelectSimple() {
        final CoverageProcessor processor = CoverageProcessor.getInstance();
        /*
         * Get the source coverage and build the cropped envelope.
         */
        final GridCoverage2D source = EXAMPLES.get(2);
        final Bounds envelope = source.getEnvelope();
        final RenderedImage rgbImage = new ImageWorker(source.getRenderedImage())
                .forceComponentColorModel()
                .getRenderedImage();
        final GridCoverage2D newCoverage =
                CoverageFactoryFinder.getGridCoverageFactory(null).create("sample", rgbImage, envelope);
        assertEquals(3, newCoverage.getNumSampleDimensions());

        /*
         * Do the crop without conserving the envelope.
         */
        ParameterValueGroup param =
                processor.getOperation("SelectSampleDimension").getParameters();
        param.parameter("Source").setValue(newCoverage);
        param.parameter("SampleDimensions").setValue(new int[] {2});
        GridCoverage2D singleBanded = (GridCoverage2D) processor.doOperation(param);
        if (SHOW) {
            Viewer.show(source);
            Viewer.show(singleBanded);
        } else {
            // Force computation
            assertNotNull(PlanarImage.wrapRenderedImage(singleBanded.getRenderedImage())
                    .getTiles());
        }
        RenderedImage raster = singleBanded.getRenderedImage();
        assertEquals(1, raster.getSampleModel().getNumBands());
        assertNotNull(raster.getColorModel());
    }

    /** Tests the "SelectSampleDimension" operation in a more complex way. */
    @Test
    public void testBandSelect() throws TransformException {
        final CoverageProcessor processor = CoverageProcessor.getInstance();
        /*
         * Get the source coverage and build the cropped envelope.
         */
        final GridCoverage2D source = EXAMPLES.get(2);
        final Bounds envelope = source.getEnvelope();
        final RenderedImage rgbImage = new ImageWorker(source.getRenderedImage())
                .forceComponentColorModel()
                .getRenderedImage();
        final SampleModel sm = rgbImage.getSampleModel();
        final ColorModel cm = rgbImage.getColorModel();
        final int numBands = sm.getNumBands();
        final GridSampleDimension[] bands = new GridSampleDimension[numBands];
        // setting bands names.
        for (int i = 0; i < numBands; i++) {
            final ColorInterpretation colorInterpretation = TypeMap.getColorInterpretation(cm, i);
            if (colorInterpretation == null) fail("Unrecognized sample dimension type");
            bands[i] = new GridSampleDimension(colorInterpretation.name());
        }
        final GridCoverage2D newCoverage = CoverageFactoryFinder.getGridCoverageFactory(null)
                .create("sample", rgbImage, envelope, bands, null, null);
        assertEquals(3, newCoverage.getNumSampleDimensions());

        /*
         * Do the band select on band 0
         */
        ParameterValueGroup param =
                processor.getOperation("SelectSampleDimension").getParameters();
        param.parameter("Source").setValue(newCoverage);
        param.parameter("SampleDimensions").setValue(new int[] {0});
        GridCoverage2D singleBanded = (GridCoverage2D) processor.doOperation(param);
        if (SHOW) {
            Viewer.show(source);
            Viewer.show(singleBanded);
        } else {
            // Force computation
            assertNotNull(PlanarImage.wrapRenderedImage(singleBanded.getRenderedImage())
                    .getTiles());
        }
        RenderedImage raster = singleBanded.getRenderedImage();
        assertEquals(1, raster.getSampleModel().getNumBands());
        assertNotNull(raster.getColorModel());

        /*
         * Do the crop without conserving the envelope.
         */
        param = processor.getOperation("SelectSampleDimension").getParameters();
        param.parameter("Source").setValue(newCoverage);
        param.parameter("SampleDimensions").setValue(new int[] {2});
        singleBanded = (GridCoverage2D) processor.doOperation(param);
        if (SHOW) {
            Viewer.show(source);
            Viewer.show(singleBanded);
        } else {
            // Force computation
            assertNotNull(PlanarImage.wrapRenderedImage(singleBanded.getRenderedImage())
                    .getTiles());
        }
        raster = singleBanded.getRenderedImage();
        assertEquals(1, raster.getSampleModel().getNumBands());
        assertNotNull(raster.getColorModel());
    }

    /** Tests the "SelectSampleDimension" operation in a simple way. */
    @Test
    public void testBandSelectMultiple() {
        final CoverageProcessor processor = CoverageProcessor.getInstance();
        /*
         * Get the source coverage and build the cropped envelope.
         */
        final GridCoverage2D source = EXAMPLES.get(4);
        final Bounds envelope = source.getEnvelope();
        final RenderedImage rgbImage = new ImageWorker(source.getRenderedImage())
                .forceComponentColorModel()
                .getRenderedImage();
        final GridCoverage2D newCoverage =
                CoverageFactoryFinder.getGridCoverageFactory(null).create("sample", rgbImage, envelope);
        assertEquals(1, newCoverage.getNumSampleDimensions());

        /*
         * Do the crop without conserving the envelope.
         */
        ParameterValueGroup param =
                processor.getOperation("SelectSampleDimension").getParameters();
        param.parameter("Source").setValue(newCoverage);
        param.parameter("SampleDimensions").setValue(new int[] {0, 0, 0, 0, 0});
        GridCoverage2D rgb = (GridCoverage2D) processor.doOperation(param);
        if (SHOW) {
            Viewer.show(source);
            Viewer.show(rgb);
        } else {
            // Force computation
            assertNotNull(PlanarImage.wrapRenderedImage(rgb.getRenderedImage()).getTiles());
        }
        RenderedImage raster = rgb.getRenderedImage();
        assertEquals(5, raster.getSampleModel().getNumBands());
        assertNotNull(raster.getColorModel());
    }
}
