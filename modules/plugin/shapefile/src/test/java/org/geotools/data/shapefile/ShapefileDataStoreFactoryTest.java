/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2012, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.shapefile;

import static org.geotools.data.shapefile.ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX;
import static org.geotools.data.shapefile.ShapefileDataStoreFactory.FSTYPE;
import static org.geotools.data.shapefile.ShapefileDataStoreFactory.URLP;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.geotools.TestData;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.QueryCapabilities;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.Name;
import org.geotools.api.filter.sort.SortBy;
import org.geotools.api.filter.sort.SortOrder;
import org.junit.After;
import org.junit.Test;

/**
 * Test the functionality of ShapefileDataStoreFactory; specifically the handling of connection parameters.
 *
 * @author Jody Garnett
 */
public class ShapefileDataStoreFactoryTest extends TestCaseSupport {

    private ShapefileDataStore store = null;
    private ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

    @Override
    @After
    public void tearDown() throws Exception {
        if (store != null) {
            store.dispose();
        }
        super.tearDown();
    }

    @Test
    public void testFSTypeParameter() throws Exception {
        URL url = TestData.url(STATE_POP);

        Map<String, Serializable> params = new HashMap<>();
        params.put(URLP.key, url);

        assertTrue("Sorting is optional", factory.canProcess(params));

        params.put(FSTYPE.key, "shape-ng");
        assertTrue("Shape NG supported", factory.canProcess(params));

        params.put(FSTYPE.key, "shape");
        assertTrue("Plain shape supported", factory.canProcess(params));

        params.put(FSTYPE.key, "index");
        assertTrue("Plain index supported", factory.canProcess(params));

        params.put(FSTYPE.key, "smurf");
        assertFalse("Feeling blue; don't try a smruf", factory.canProcess(params));
    }

    @Test
    public void testQueryCapabilities() throws Exception {
        URL url = TestData.url(STATE_POP);

        Map<String, Serializable> params = new HashMap<>();
        params.put(URLP.key, url);

        DataStore dataStore = factory.createDataStore(params);
        Name typeName = dataStore.getNames().get(0);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        QueryCapabilities caps = featureSource.getQueryCapabilities();

        SortBy[] sortBy = {
            SortBy.NATURAL_ORDER,
        };
        assertTrue("Natural", caps.supportsSorting(sortBy));

        SimpleFeatureType schema = featureSource.getSchema();
        String attr = schema.getDescriptor(1).getLocalName();

        sortBy[0] = ff.sort(attr, SortOrder.ASCENDING);
        assertTrue("Sort " + attr, caps.supportsSorting(sortBy));

        sortBy[0] = ff.sort("the_geom", SortOrder.ASCENDING);
        assertFalse("Cannot sort the_geom", caps.supportsSorting(sortBy));
    }

    @Test
    public void testEnableIndexParameter() throws Exception {

        // remote (jar file) shapefiles
        URL remoteUrl = TestData.url(STATE_POP);

        // local shapefiles (copied out of jar)
        File f = copyShapefiles(STATE_POP);
        URL localUrl = f.toURI().toURL();

        // test remote file has spatial index disabled even if requested
        Map<String, Serializable> params = map(URLP.key, remoteUrl, ENABLE_SPATIAL_INDEX.key, true);
        ShapefileDataStore ds = (ShapefileDataStore) factory.createDataStore(params);
        assertNotNull("Null datastore should not be returned", ds);
        assertTrue("should be a non indexed shapefile", ds instanceof org.geotools.data.shapefile.ShapefileDataStore);
        ds.dispose();

        // test default has spatial index enabled
        params = map(URLP.key, localUrl);
        ds = (ShapefileDataStore) factory.createDataStore(params);
        assertNotNull("Null datastore should not be returned", ds);
        assertTrue(ds.isIndexed());
        assertTrue(ds.isIndexCreationEnabled());
        ds.dispose();

        // test disable works
        params = map(URLP.key, localUrl, ENABLE_SPATIAL_INDEX.key, false);
        ds = (ShapefileDataStore) factory.createDataStore(params);
        assertNotNull("Null datastore should not be returned", ds);
        assertFalse(ds.isIndexed());
        assertFalse(ds.isIndexCreationEnabled());
        ds.dispose();

        // text explicit enable works
        params = map(URLP.key, localUrl, ENABLE_SPATIAL_INDEX.key, true);
        ds = (ShapefileDataStore) factory.createDataStore(params);
        assertNotNull("Null datastore should not be returned", ds);
        assertTrue(ds.isIndexed());
        assertTrue(ds.isIndexCreationEnabled());
        ds.dispose();
    }

    private Map<String, Serializable> map(Object... pairs) {
        if ((pairs.length & 1) != 0) {
            throw new IllegalArgumentException("Pairs was not an even number");
        }
        Map<String, Serializable> result = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            String key = (String) pairs[i];
            Serializable value = (Serializable) pairs[i + 1];
            result.put(key, value);
        }

        return result;
    }

    /** Check non NullPointerException using a http URL instead Filesystem path */
    @Test
    public void testHttpUrl() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put(URLP.key, "http://geo-solution.it/");
        assertFalse(factory.canProcess(params));
    }
}
