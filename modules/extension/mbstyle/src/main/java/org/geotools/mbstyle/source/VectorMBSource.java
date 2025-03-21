/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2017, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.mbstyle.source;

import org.geotools.mbstyle.parse.MBObjectParser;
import org.json.simple.JSONObject;

/**
 * Wrapper around a {@link JSONObject} holding a Mapbox vectorsource. Tiled sources (vector and raster) must specify
 * their details in terms of the TileJSON specification.
 *
 * @see MBSource
 * @see <a
 *     href="https://www.mapbox.com/mapbox-gl-js/style-spec/#sources-vector">https://www.mapbox.com/mapbox-gl-js/style-spec/#sources-vector</a>
 */
public class VectorMBSource extends TileMBSource {

    public VectorMBSource(JSONObject json) {
        this(json, null);
    }

    public VectorMBSource(JSONObject json, MBObjectParser parser) {
        super(json, parser);
    }

    @Override
    public String getType() {
        return "vector";
    }
}
