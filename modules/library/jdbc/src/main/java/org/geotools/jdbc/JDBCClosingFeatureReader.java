/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.util.NoSuchElementException;
import org.geotools.api.data.DelegatingFeatureReader;
import org.geotools.api.data.FeatureReader;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

public class JDBCClosingFeatureReader implements DelegatingFeatureReader<SimpleFeatureType, SimpleFeature> {

    FeatureReader<SimpleFeatureType, SimpleFeature> reader;

    public JDBCClosingFeatureReader(FeatureReader<SimpleFeatureType, SimpleFeature> reader) {
        this.reader = reader;
    }

    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getDelegate() {
        return reader;
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return reader.getFeatureType();
    }

    @Override
    public boolean hasNext() throws IOException {
        return reader.hasNext();
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        return reader.next();
    }

    @Override
    @SuppressWarnings({"PMD.CloseResource", "unchecked"}) // we are actually closing
    public void close() throws IOException {
        FeatureReader<SimpleFeatureType, SimpleFeature> r = reader;
        while (r instanceof DelegatingFeatureReader) {
            if (r instanceof JDBCFeatureReader) {
                break;
            }

            r = ((DelegatingFeatureReader) r).getDelegate();
        }

        if (r instanceof JDBCFeatureReader) {
            JDBCFeatureReader jdbcReader = (JDBCFeatureReader) r;
            JDBCFeatureSource fs = jdbcReader.featureSource;
            Connection cx = jdbcReader.cx;

            try {
                reader.close();
            } finally {
                fs.getDataStore().releaseConnection(cx, fs.getState());
            }
        }
    }
}
