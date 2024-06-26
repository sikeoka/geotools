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
package org.geotools.filter.v1_1.capabilities;

import javax.xml.namespace.QName;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.capability.IdCapabilities;
import org.geotools.filter.v1_1.OGC;
import org.geotools.xsd.AbstractComplexBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;

/**
 * Binding object for the type http://www.opengis.net/ogc:Id_CapabilitiesType.
 *
 * <p>
 *
 * <pre>
 *         <code>
 *  &lt;xsd:complexType name="Id_CapabilitiesType"&gt;
 *      &lt;xsd:choice maxOccurs="unbounded"&gt;
 *          &lt;xsd:element ref="ogc:EID"/&gt;
 *          &lt;xsd:element ref="ogc:FID"/&gt;
 *      &lt;/xsd:choice&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 *
 * @generated
 */
public class Id_CapabilitiesTypeBinding extends AbstractComplexBinding {
    FilterFactory factory;

    public Id_CapabilitiesTypeBinding(FilterFactory factory) {
        this.factory = factory;
    }

    /** @generated */
    @Override
    public QName getTarget() {
        return OGC.Id_CapabilitiesType;
    }

    /**
     *
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    @Override
    public Class getType() {
        return IdCapabilities.class;
    }

    /**
     *
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    @Override
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
        // &lt;xsd:element ref="ogc:EID"/&gt;
        boolean eid = node.hasChild("EID");

        // &lt;xsd:element ref="ogc:FID"/&gt;
        boolean fid = node.hasChild("FID");

        return factory.idCapabilities(eid, fid);
    }

    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        IdCapabilities id = (IdCapabilities) object;

        if (OGC.EID.equals(name) && id.hasEID()) {
            return new Object();
        }

        if (OGC.FID.equals(name) && id.hasFID()) {
            return new Object();
        }

        return null;
    }
}
