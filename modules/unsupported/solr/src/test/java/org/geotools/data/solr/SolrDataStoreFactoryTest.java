/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2015, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.solr;

import static org.geotools.data.solr.SolrDataStoreFactory.FIELD;
import static org.geotools.data.solr.SolrDataStoreFactory.LAYER_MAPPER;
import static org.geotools.data.solr.SolrDataStoreFactory.URL;

import org.geotools.data.solr.SolrLayerMapper.Type;
import org.geotools.util.KVP;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SolrDataStoreFactoryTest {

    SolrDataStoreFactory dataStoreFactory;

    @Before
    public void setUp() throws Exception {
        dataStoreFactory = new SolrDataStoreFactory();
    }

    @Test
    public void testDefaultMapper() throws Exception {
        SolrDataStore dataStore = (SolrDataStore) dataStoreFactory.createDataStore(
                new KVP(URL.key, "http://localhost:8080/solr/geotools", FIELD.key, "foo"));
        Assert.assertTrue(dataStore.getLayerMapper() instanceof FieldLayerMapper);
    }

    @Test
    public void testSingleLayerMapper() throws Exception {
        SolrDataStore dataStore = (SolrDataStore) dataStoreFactory.createDataStore(
                new KVP(URL.key, "http://localhost:8080/solr/geotools", LAYER_MAPPER.key, Type.SINGLE.name()));
        Assert.assertTrue(dataStore.getLayerMapper() instanceof SingleLayerMapper);
    }
}
