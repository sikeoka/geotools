/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014 Open Source Geospatial Foundation (OSGeo)
 * 	  (c) 2012 - 2014 OpenPlans
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
package org.geotools.data.csv;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.geotools.api.data.FeatureReader;
import org.geotools.api.data.Query;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.csv.parse.CSVIterator;
import org.geotools.data.csv.parse.CSVStrategy;

public class CSVFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private SimpleFeatureType featureType;

    private CSVIterator iterator;

    public CSVFeatureReader(CSVStrategy csvStrategy) throws IOException {
        this(csvStrategy, Query.ALL);
    }

    public CSVFeatureReader(CSVStrategy csvStrategy, Query query) throws IOException {
        this.featureType = csvStrategy.getFeatureType();
        this.iterator = csvStrategy.iterator();
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public void close() throws IOException {
        iterator.close();
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        return iterator.next();
    }

    @Override
    public boolean hasNext() throws IOException {
        return iterator.hasNext();
    }
}
