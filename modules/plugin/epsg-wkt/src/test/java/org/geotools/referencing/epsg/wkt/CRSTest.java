/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.referencing.epsg.wkt;

import static org.junit.Assert.assertArrayEquals;

import org.geotools.api.geometry.Position;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.CoordinateOperation;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.GeneralPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.GeoTools;
import org.geotools.util.factory.Hints;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

/** @author Jody Garnett */
public class CRSTest {
    /** Makes sure that the transform between two EPSG:4326 is the identity transform. */
    @Test
    public void testFindMathTransformIdentity() throws FactoryException {
        CoordinateReferenceSystem crs1default = CRS.decode("EPSG:4326", false);
        CoordinateReferenceSystem crs2default = CRS.decode("EPSG:4326", false);
        MathTransform tDefault = CRS.findMathTransform(crs1default, crs2default);
        Assert.assertTrue("WSG84 transformed to WSG84 should be Identity", tDefault.isIdentity());

        CoordinateReferenceSystem crs1force = CRS.decode("EPSG:4326", true);
        CoordinateReferenceSystem crs2force = CRS.decode("EPSG:4326", true);
        MathTransform tForce = CRS.findMathTransform(crs1force, crs2force);
        Assert.assertTrue("WSG84 transformed to WSG84 should be Identity", tForce.isIdentity());
    }

    @Test
    public void testEPSG42102() throws Exception {
        CoordinateReferenceSystem bc = CRS.decode("EPSG:42102");
        Assert.assertNotNull("bc", bc);
    }

    @Test
    public void testEPSG28992toWGS84() throws Exception {
        /*
         * Unit test to accompany the fix for https://osgeo-org.atlassian.net/browse/GEOT-5077
         */
        CoordinateReferenceSystem epsg28992 = CRS.decode("EPSG:28992");
        CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84;

        MathTransform transform = CRS.findMathTransform(epsg28992, wgs84);

        /*
         * Test using 4 reference points from the corners of the CRS, obtained from the website of the Dutch national
         * mapping agency at https://rdinfo.kadaster.nl/
         *
         * https://rdinfo.kadaster.nl/overzicht_rd_medewerker/rd_punt.html?punt={ID}
         *
         * ID      X-RD         Y-RD          Latitude             Longitude
         * 250317  121784.6113  487036.9695   52° 22' 12.74367''   4° 53' 58.15203''
         *                                    52.370206575         4.899486675
         *
         * 610306  176135.0779  317654.5066   50° 50' 54.01748''   5° 41' 14.23435''
         *                                    50.8483381889        5.68728731944
         *
         * 079342  233473.7307  581727.0264   53° 12' 59.05571''   6° 33' 43.15655''
         *                                    53.2164043639        6.56198793056
         *
         * 489101   31935.2867  391557.3350   51° 29' 58.46250''   3° 36' 53.15985''
         *                                    51.4995729167        3.614766625
         */
        double[] srcCoords = {
            /* Id: 250317 */ 121784.6113, 487036.9695,
            /* Id: 610306 */ 176135.0779, 317654.5066,
            /* Id: 079342 */ 233473.7307, 581727.0264,
            /* Id: 489101 */ 31935.2867, 391557.3350
        };

        double[] expectedTransformedCoords = {
            /* Id: 250317 */ 4.899486675, 52.370206575,
            /* Id: 610306 */ 5.68728731944, 50.8483381889,
            /* Id: 079342 */ 6.56198793056, 53.2164043639,
            /* Id: 489101 */ 3.614766625, 51.4995729167,
        };

        double[] transformedCoords = new double[8];

        transform.transform(srcCoords, 0, transformedCoords, 0, 4);

        // No transformation is perfect, so we allow a small delta
        assertArrayEquals(expectedTransformedCoords, transformedCoords, 0.000003);
    }

    @Test
    public void testAUTO4200() throws Exception {
        CoordinateReferenceSystem utm = CRS.decode("AUTO:42001,0.0,0.0");
        Assert.assertNotNull("auto-utm", utm);
    }

    @Test
    public void test4269() throws Exception {
        CoordinateReferenceSystem latlong = CRS.decode("EPSG:4269");
        Assert.assertNotNull("latlong", latlong);
        try {
            latlong = CRS.decode("4269");
            Assert.fail("Shoudl not be able to decode 4269 without EPSG authority");
        } catch (NoSuchAuthorityCodeException e) {
            // expected
        }
        Assert.assertNotNull("latlong", latlong);
    }

    @Test
    public void testManditoryTranform() throws Exception {
        CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326"); // latlong

        CoordinateOperation op =
                ReferencingFactoryFinder.getCoordinateOperationFactory(null).createOperation(WGS84, WGS84);
        MathTransform math = op.getMathTransform();

        Position pt1 = new GeneralPosition(0.0, 0.0);
        Position pt2 = math.transform(pt1, null);
        Assert.assertNotNull(pt2);

        double[] pts = {
            1187128, 395268, 1187128, 396027,
            1188245, 396027, 1188245, 395268,
            1187128, 395268
        };
        double[] tst = new double[pts.length];
        math.transform(pts, 0, new double[pts.length], 0, pts.length / 2);
        for (int i = 0; i < pts.length; i++) Assert.assertNotEquals("pts[" + i + "]", pts[i], tst[i]);
    }
    /** Taken from empty udig map calculation of scale. */
    @Test
    public void testSamplePixel() throws Exception {
        Hints global = new Hints();
        GeoTools.init(global);

        // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 :
        // -0.0]
        CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326");
        double[] pixelBounds = {-0.24291497975705742, 0.24291497975711265, -0.5056179775280899, 0.0};
        CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
        MathTransform mt = CRS.findMathTransform(EPSG4326, WGS84, true);
        double[] result = new double[4];
        mt.transform(pixelBounds, 0, result, 0, 2);
        assertArrayEquals(result, pixelBounds, 0);
    }

    @Test
    public void testReprojection() throws Exception {
        // origional bc alberts
        double[] poly1 = {
            1187128, 395268, 1187128, 396027,
            1188245, 396027, 1188245, 395268,
            1187128, 395268
        };

        // transformed
        double[] poly3 = {
            -123.47009555832284, 48.543261561072285,
            -123.46972894676578, 48.55009592117936,
            -123.45463828850829, 48.54973520267304,
            -123.4550070827961, 48.54290089070186,
            -123.47009555832284, 48.543261561072285
        };

        CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326"); // latlong
        CoordinateReferenceSystem BC_ALBERS = CRS.decode("EPSG:42102");

        MathTransform transform = CRS.findMathTransform(BC_ALBERS, WGS84);

        double[] polyAfter = new double[10];
        transform.transform(poly1, 0, polyAfter, 0, 5);

        assertArrayEquals(poly3, polyAfter, 0.00000000000001);
    }

    @Test
    public void testReprojectionDefault() throws Exception {
        // origional bc alberts
        double[] poly1 = {
            1187128, 395268, 1187128, 396027,
            1188245, 396027, 1188245, 395268,
            1187128, 395268
        };

        // transformed
        double[] poly3 = {
            -123.47009555832284, 48.543261561072285,
            -123.46972894676578, 48.55009592117936,
            -123.45463828850829, 48.54973520267304,
            -123.4550070827961, 48.54290089070186,
            -123.47009555832284, 48.543261561072285
        };

        CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
        CoordinateReferenceSystem BC_ALBERS = CRS.decode("EPSG:42102");

        MathTransform transform = CRS.findMathTransform(BC_ALBERS, WGS84);
        double[] polyAfter = new double[poly1.length];
        transform.transform(poly1, 0, polyAfter, 0, 5);
        assertArrayEquals(poly3, polyAfter, 0.00000000000001);
    }

    public static GeometryFactory factory = new GeometryFactory();

    protected void assertEnvelopeEquals(Geometry a, Geometry b, double tolerance) {
        Envelope aEnv = a.getEnvelopeInternal();
        Envelope bEnv = b.getEnvelopeInternal();

        Assert.assertEquals(aEnv.getMinX(), bEnv.getMinX(), tolerance);
        Assert.assertEquals(aEnv.getMaxX(), bEnv.getMaxX(), tolerance);
        Assert.assertEquals(aEnv.getMinY(), bEnv.getMinY(), tolerance);
        Assert.assertEquals(aEnv.getMaxY(), bEnv.getMaxY(), tolerance);
    }
}
