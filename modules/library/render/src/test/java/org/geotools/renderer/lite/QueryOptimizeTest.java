package org.geotools.renderer.lite;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.style.Style;
import org.geotools.data.property.PropertyDataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.RenderListener;
import org.geotools.test.TestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the optimized data loading does merge the filters properly (was never released, but a certain point in time
 * only the first one was passed down to the datastore)
 */
public class QueryOptimizeTest {

    private static final long TIME = 2000;

    SimpleFeatureSource squareFS;
    ReferencedEnvelope bounds;
    StreamingRenderer renderer;
    MapContent context;
    int count = 0;

    @Before
    public void setUp() throws Exception {
        // setup data
        File property = new File(TestData.getResource(this, "square.properties").toURI());
        PropertyDataStore ds = new PropertyDataStore(property.getParentFile());
        squareFS = ds.getFeatureSource("square");
        bounds = new ReferencedEnvelope(0, 10, 0, 10, DefaultGeographicCRS.WGS84);

        renderer = new StreamingRenderer();
        context = new MapContent();
        renderer.setMapContent(context);
        Map<Object, Object> hints = new HashMap<>();
        hints.put("maxFiltersToSendToDatastore", 2);
        hints.put("optimizedDataLoadingEnabled", true);
        renderer.setRendererHints(hints);

        //        System.setProperty("org.geotools.test.interactive", "true");
    }

    @Test
    public void testLessFilters() throws Exception {
        Style style = RendererBaseTest.loadStyle(this, "fillSolidTwoRules.sld");

        MapContent mc = new MapContent();
        mc.addLayer(new FeatureLayer(squareFS, style));

        renderer.setMapContent(mc);
        renderer.addRenderListener(new RenderListener() {

            @Override
            public void featureRenderer(SimpleFeature feature) {
                count++;
            }

            @Override
            public void errorOccurred(Exception e) {}
        });

        RendererBaseTest.showRender("OneSquare", renderer, TIME, bounds);
        Assert.assertEquals(2, count);
    }
}
