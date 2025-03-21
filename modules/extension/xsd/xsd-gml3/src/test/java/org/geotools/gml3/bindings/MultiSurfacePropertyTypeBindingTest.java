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
package org.geotools.gml3.bindings;

import static org.junit.Assert.assertEquals;

import org.geotools.geometry.jts.MultiSurface;
import org.geotools.gml3.GML;
import org.geotools.gml3.GML3TestSupport;
import org.geotools.gml3.GMLSchema;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class MultiSurfacePropertyTypeBindingTest extends GML3TestSupport {

    @Test
    public void testEncode() throws Exception {
        Document dom = encode(GML3MockData.multiSurface(), GML.multiSurfaceProperty);
        assertEquals(
                1,
                dom.getElementsByTagNameNS(GML.NAMESPACE, GML.MultiSurface.getLocalPart())
                        .getLength());
        assertEquals(
                2,
                dom.getElementsByTagNameNS(GML.NAMESPACE, GML.surfaceMember.getLocalPart())
                        .getLength());
    }

    @Test
    public void testMultiSurfacePropertyTypeAssignable() {
        Assert.assertTrue(GMLSchema.MULTISURFACEPROPERTYTYPE_TYPE.getBinding().isAssignableFrom(MultiSurface.class));
    }
}
