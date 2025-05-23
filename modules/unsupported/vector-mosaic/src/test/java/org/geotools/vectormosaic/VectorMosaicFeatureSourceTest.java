/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2022, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.vectormosaic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.Feature;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.expression.PropertyName;
import org.geotools.data.DataUtilities;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.visitor.MaxVisitor;
import org.geotools.feature.visitor.NearestVisitor;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;

public class VectorMosaicFeatureSourceTest extends VectorMosaicTest {

    @Test
    public void testCountAll() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;

        assertEquals(4, featureSource.getCount(Query.ALL));
        assertEquals(4, tracker.getCount());
    }

    @Test
    public void testCountAllMaxFeatures() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;

        Query q = new Query();
        q.setMaxFeatures(2);
        assertEquals(2, featureSource.getCount(q));
        assertEquals(2, tracker.getCount());
    }

    @Test
    public void testCountFilterDelegate() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;

        Query query = new Query();
        query.setFilter(FF.lessOrEqual(FF.property("rank"), FF.literal(2)));
        assertEquals(2, featureSource.getCount(query));
        assertEquals(2, tracker.getCount());
    }

    @Test
    public void testCountFilterDelegateMaxFeatures() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;

        Query query = new Query();
        query.setMaxFeatures(1); // this will stop the counting before the end
        query.setFilter(FF.lessOrEqual(FF.property("rank"), FF.literal(2)));
        assertEquals(1, featureSource.getCount(query));
        assertEquals(1, tracker.getCount());
    }

    @Test
    public void testCountFilterGranules() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;

        // this one has to visit all granules to find the actual count
        Query query = new Query();
        query.setFilter(FF.lessOrEqual(FF.property("tractorid"), FF.literal("deere2")));
        assertEquals(2, featureSource.getCount(query));
        assertEquals(4, tracker.getCount());
    }

    @Test
    public void testCountFilterGranulesMaxFeatures() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;

        // this one has to visit all granules to find the actual count, but the max features
        // will stop it before the end
        Query q = new Query();
        q.setMaxFeatures(2);
        assertEquals(2, featureSource.getCount(q));
        assertEquals(2, tracker.getCount());
    }

    @Test
    /** Test that the cached feature type is used when available */
    public void testCacheFeatureType() throws Exception {
        VectorMosaicFeatureSource featureSource =
                (VectorMosaicFeatureSource) MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        VectorMosaicState state = new VectorMosaicState(
                new ContentEntry((ContentDataStore) MOSAIC_STORE, new NameImpl(MOSAIC_TYPE_NAME)));
        SimpleFeatureType oldType = MOSAIC_STORE.getSchema(MOSAIC_TYPE_NAME);
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("cachedType");
        tb.setNamespaceURI(oldType.getName().getNamespaceURI());
        tb.setCRS(oldType.getCoordinateReferenceSystem());
        tb.addAll(oldType.getAttributeDescriptors());
        tb.setDefaultGeometry(oldType.getGeometryDescriptor().getLocalName());
        SimpleFeatureType cachedType = tb.buildFeatureType();
        state.setGranuleFeatureType(cachedType);
        featureSource.state = state;
        SimpleFeatureType simpleFeatureType = featureSource.getGranuleType();
        assertEquals("cachedType", simpleFeatureType.getTypeName());
    }

    @Test
    public void testGetBoundsReturnsAll() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query query = new Query(MOSAIC_TYPE_NAME);
        query.setFilter(Filter.INCLUDE);
        assertEquals(
                new ReferencedEnvelope(
                        -87.93455924124713,
                        -76.66065404625304,
                        33.75260500029857,
                        39.65010808794791,
                        DefaultGeographicCRS.WGS84),
                featureSource.getBounds(query));
    }

    @Test
    public void testGetBounds() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query query = new Query(MOSAIC_TYPE_NAME);
        Filter qequals = FF.equals(FF.property("queryables"), FF.literal("time"));
        query.setFilter(qequals);
        assertEquals(
                new ReferencedEnvelope(
                        -81.8210129839034,
                        -76.66065404625304,
                        36.65259600815406,
                        39.65010808794791,
                        DefaultGeographicCRS.WGS84),
                featureSource.getBounds(query));
    }

    @Test
    public void testPassedInSPI() throws Exception {
        FeatureSource featureSource = MOSAIC_STORE_WITH_SPI.getFeatureSource(MOSAIC_TYPE_NAME);
        Query query = new Query(MOSAIC_TYPE_NAME);
        Filter qequals = FF.equals(FF.property("queryables"), FF.literal("time"));
        query.setFilter(qequals);
        assertEquals(
                new ReferencedEnvelope(
                        -81.8210129839034,
                        -76.66065404625304,
                        36.65259600815406,
                        39.65010808794791,
                        DefaultGeographicCRS.WGS84),
                featureSource.getBounds(query));
    }

    @Test
    public void testGetMaxIndexHasExpressionAndFilter() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName p = FF.property("rank");
        Query q = new Query();
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);

        MaxVisitor v = new MaxVisitor(p);
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        int max = v.getResult().toInt();
        assertEquals(4, max);
        // no granules touched because all expressions match index
        assertEquals(0, tracker.getGranuleNames().size());
    }

    @Test
    public void testGetMaxIndexHasExpressionButFilterNeedsGranules() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName granuleOnly = FF.property("tractorid");
        Query q = new Query();
        Filter f = FF.equals(granuleOnly, FF.literal("deere1"));
        q.setFilter(f);
        PropertyName p = FF.property("rank");
        MaxVisitor v = new MaxVisitor(p);
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        int max = v.getResult().toInt();
        assertEquals(1, max);
        // all granules touched because filter references granule attribute
        assertEquals(3, tracker.getGranuleNames().size());
    }

    @Test
    public void testGetMaxIndexDoesNotMatchExpression() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName p = FF.property("rank");
        Query q = new Query();
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);
        PropertyName granuleOnly = FF.property("weight");
        MaxVisitor v = new MaxVisitor(granuleOnly);
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        int max = v.getResult().toInt();
        assertEquals(9, max);
        // all granules touched because visitor expression references granule attribute
        assertEquals(3, tracker.getGranuleNames().size());
    }

    @Test
    public void testGetUniqueIndexHasExpressionAndFilter() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName p = FF.property("rank");
        Query q = new Query();
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);

        UniqueVisitor v = new UniqueVisitor(p);
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        Set uniques = v.getResult().toSet();
        assertEquals(4, uniques.size());
        // no granules touched because all expressions match index
        assertEquals(0, tracker.getGranuleNames().size());
    }

    @Test
    public void testGetUniqueIndexDoesNotMatchExpression() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName p = FF.property("rank");
        Query q = new Query();
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);

        PropertyName granuleOnly = FF.property("weight");
        UniqueVisitor v = new UniqueVisitor(p, granuleOnly);
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        Set uniques = v.getResult().toSet();
        assertEquals(4, uniques.size());
        // all granules touched because visitor expression references granule-only attribute and
        // index-only attribute
        assertEquals(3, tracker.getGranuleNames().size());
    }

    @Test
    public void testMixedQueryFilter() throws Exception {
        VectorMosaicFeatureSource featureSource =
                (VectorMosaicFeatureSource) MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query q = new Query();
        // rank is in the index
        PropertyName p = FF.property("rank");
        // weight is in the granule
        PropertyName p2 = FF.property("weight");
        Filter f = FF.and(FF.greaterOrEqual(p, FF.literal(2)), FF.lessOrEqual(p2, FF.literal(7)));
        q.setFilter(f);
        ContentFeatureCollection features = featureSource.getFeatures(q);
        List<SimpleFeature> list = DataUtilities.list(features);
        assertEquals(1, list.size());
        assertEquals("granule2.1", list.get(0).getID());
    }

    @Test
    public void testNearestIndexHasExpressionAndFilter() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName rank = FF.property("rank");
        Query q = new Query();
        Filter f = FF.lessOrEqual(rank, FF.literal(100));
        q.setFilter(f);

        NearestVisitor v = new NearestVisitor(rank, 2.7);
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        int max = v.getResult().toInt();
        assertEquals(3, max);
        // no granules touched because all expressions match index
        assertEquals(0, tracker.getGranuleNames().size());
    }

    @Test
    public void testNearestIndexHasExpressionButFilterNeedsGranules() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName granuleOnly = FF.property("tractorid");
        Query q = new Query();
        Filter f = FF.equals(granuleOnly, FF.literal("deere1"));
        q.setFilter(f);
        PropertyName p = FF.property("rank");
        NearestVisitor v = new NearestVisitor(p, 100); // only one matching value anyways, 1
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        int nearest = v.getResult().toInt();
        assertEquals(1, nearest);
        // all granules touched because filter references granule attribute
        assertEquals(3, tracker.getGranuleNames().size());
    }

    @Test
    public void testNearestIndexDoesNotMatchExpression() throws Exception {
        SimpleFeatureSource featureSource = MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        GranuleTracker tracker = new GranuleTracker();
        GranuleStoreFinder finder = ((VectorMosaicFeatureSource) featureSource).finder;
        finder.granuleTracker = tracker;
        PropertyName rank = FF.property("rank");
        Query q = new Query();
        Filter f = FF.lessOrEqual(rank, FF.literal(100));
        q.setFilter(f);
        PropertyName granuleOnly = FF.property("weight");
        NearestVisitor v = new NearestVisitor(granuleOnly, 8.7);
        ((VectorMosaicFeatureSource) featureSource).accepts(q, v, null);
        int nearest = v.getResult().toInt();
        assertEquals(9, nearest);
        // all granules touched because visitor expression references granule attribute
        assertEquals(3, tracker.getGranuleNames().size());
    }

    /** Test filtering on attributes found both in the index and in the granule, but return a third property */
    @Test
    public void testMixedFilterAttributeSelection() throws Exception {
        VectorMosaicFeatureSource featureSource =
                (VectorMosaicFeatureSource) MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query q = new Query();
        // rank is in the index
        PropertyName p = FF.property("rank");
        // weight is in the granule
        PropertyName p2 = FF.property("weight");
        Filter f = FF.and(FF.greaterOrEqual(p, FF.literal(2)), FF.lessOrEqual(p2, FF.literal(7)));
        q.setFilter(f);
        q.setPropertyNames("tractorid");
        ContentFeatureCollection features = featureSource.getFeatures(q);

        // expected, just one feature, with only the tractorid attribute
        List<SimpleFeature> list = DataUtilities.list(features);
        assertEquals(1, list.size());
        SimpleFeature feature = list.get(0);
        assertEquals("granule2.1", feature.getID());
        assertEquals(1, feature.getType().getAttributeCount());
        assertEquals("deere2", feature.getAttribute("tractorid"));
    }

    @Test
    public void testIndexQueryFilterDoesNotUseFilteringFeatureReader() throws Exception {
        VectorMosaicFeatureSource featureSource =
                (VectorMosaicFeatureSource) MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query q = new Query();
        // rank is in the index only
        PropertyName p = FF.property("rank");
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);
        assertNotEquals(
                FilteringFeatureReader.class, featureSource.getReaderInternal(q).getClass());
    }

    @Test
    public void testGranuleIteratorLimitsProperties() throws Exception {
        VectorMosaicFeatureSource featureSource =
                (VectorMosaicFeatureSource) MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query q = new Query();
        PropertyName p = FF.property("weight");
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);
        q.setPropertyNames("weight");
        try (VectorMosaicFeatureReader featureReader = (VectorMosaicFeatureReader) featureSource.getReader(q)) {
            if (featureReader.hasNext()) {
                String granuleAttributes = featureReader.rawGranule.getType().getDescriptors().stream()
                        .map(d -> d.getName().getLocalPart())
                        .collect(Collectors.joining(","));
                assertEquals("weight", granuleAttributes);
            }
        }
    }

    @Test
    public void testDelegateIteratorLimitsProperties() throws Exception {
        VectorMosaicFeatureSource featureSource =
                (VectorMosaicFeatureSource) MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query q = new Query();
        PropertyName p = FF.property("rank");
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);
        q.setPropertyNames("rank");
        try (VectorMosaicFeatureReader featureReader = (VectorMosaicFeatureReader) featureSource.getReader(q)) {
            if (featureReader.hasNext()) {
                String delegateAttributes = featureReader.delegateFeature.getType().getDescriptors().stream()
                        .map(d -> d.getName().getLocalPart())
                        .collect(Collectors.joining(","));
                // params is a required element of the delegate feature, so is type
                assertEquals("rank,params,type", delegateAttributes);
            }
        }
    }

    @Test
    public void testDelegateAndGranuleIteratorLimitsProperties() throws Exception {
        VectorMosaicFeatureSource featureSource =
                (VectorMosaicFeatureSource) MOSAIC_STORE.getFeatureSource(MOSAIC_TYPE_NAME);
        Query q = new Query();
        PropertyName p = FF.property("rank");
        Filter f = FF.lessOrEqual(p, FF.literal(100));
        q.setFilter(f);
        q.setPropertyNames("rank", "weight");
        try (VectorMosaicFeatureReader featureReader = (VectorMosaicFeatureReader) featureSource.getReader(q)) {
            if (featureReader.hasNext()) {
                String delegateAttributes = featureReader.delegateFeature.getType().getDescriptors().stream()
                        .map(d -> d.getName().getLocalPart())
                        .collect(Collectors.joining(","));
                // params is a required element of the delegate feature, so is type
                assertEquals("rank,params,type", delegateAttributes);
                String granuleAttributes = featureReader.rawGranule.getType().getDescriptors().stream()
                        .map(d -> d.getName().getLocalPart())
                        .collect(Collectors.joining(","));
                assertEquals("weight", granuleAttributes);
                // validate that both show in final merged feature, following the query order
                Feature f1 = featureReader.next();
                assertEquals(
                        "rank,weight",
                        f1.getType().getDescriptors().stream()
                                .map(d -> d.getName().getLocalPart())
                                .collect(Collectors.joining(",")));
            }
        }
    }
}
