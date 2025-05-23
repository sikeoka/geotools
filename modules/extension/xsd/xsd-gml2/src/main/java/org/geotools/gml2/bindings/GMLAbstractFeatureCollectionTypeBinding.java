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
package org.geotools.gml2.bindings;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml2.GML;
import org.geotools.xsd.AbstractComplexBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;

/**
 * Binding object for the type http://www.opengis.net/gml:AbstractFeatureCollectionType.
 *
 * <p>
 *
 * <pre>
 *         <code>
 *  &lt;complexType name="AbstractFeatureCollectionType" abstract="true"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         A feature collection contains zero or
 *              more featureMember elements.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractFeatureCollectionBaseType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element ref="gml:featureMember" minOccurs="0" maxOccurs="unbounded"/&gt;
 *              &lt;/sequence&gt;
 *          &lt;/extension&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 *
 * @generated
 */
public class GMLAbstractFeatureCollectionTypeBinding extends AbstractComplexBinding {
    /** @generated */
    @Override
    public QName getTarget() {
        return GML.AbstractFeatureCollectionType;
    }

    /**
     *
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    @Override
    public int getExecutionMode() {
        return AFTER;
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
        return FeatureCollection.class;
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
        // call "super"
        SimpleFeatureCollection fc = (SimpleFeatureCollection) value;

        // add all feature member children
        if (fc instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<SimpleFeature> collection = (Collection) fc;
            collection.addAll(node.getChildValues(SimpleFeature.class));
        } else {
            throw new IllegalStateException("Please provide DefaultFeatureCollection or ListFeatureCollection");
        }

        return fc;
    }
}
