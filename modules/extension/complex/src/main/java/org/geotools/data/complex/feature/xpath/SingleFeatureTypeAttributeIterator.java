/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.complex.feature.xpath;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.api.feature.type.ComplexType;
import org.geotools.api.feature.type.Name;

/**
 * Iterates over a single attribute of a feature type.
 *
 * @author Niels Charlier (Curtin University of Technology)
 */
public class SingleFeatureTypeAttributeIterator implements NodeIterator {

    /** The feature type node pointer */
    protected NodePointer pointer;

    protected Name name;

    protected ComplexType featureType;

    /**
     * Creates the iteartor.
     *
     * @param pointer The pointer to the feature.
     */
    public SingleFeatureTypeAttributeIterator(NodePointer pointer, ComplexType featureType, Name name) {
        this.pointer = pointer;
        this.name = name;
        this.featureType = featureType;
    }

    /** Always return 1, only a single property. */
    @Override
    public int getPosition() {
        return 1;
    }

    /** Return true if position == 1. */
    @Override
    public boolean setPosition(int position) {
        return position < 2;
    }

    /** Return a pointer to the property at the set index. */
    @Override
    public NodePointer getNodePointer() {
        return new FeatureTypeAttributePointer(pointer, featureType, name);
    }
}
