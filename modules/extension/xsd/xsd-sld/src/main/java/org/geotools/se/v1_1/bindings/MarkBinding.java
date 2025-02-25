/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.se.v1_1.bindings;

import java.net.URI;
import javax.swing.Icon;
import javax.xml.namespace.QName;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.style.ExternalMark;
import org.geotools.api.style.Mark;
import org.geotools.api.style.ResourceLocator;
import org.geotools.api.style.StyleFactory;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.se.v1_1.SE;
import org.geotools.sld.bindings.SLDMarkBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;

/**
 * Binding object for the element http://www.opengis.net/se:Mark.
 *
 * <p>
 *
 * <pre>
 *  <code>
 *  &lt;xsd:element name="Mark" type="se:MarkType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *          A "Mark" specifies a geometric shape and applies coloring to it.
 *        &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *  &lt;/xsd:element&gt;
 *
 *   </code>
 * </pre>
 *
 * <pre>
 *       <code>
 *  &lt;xsd:complexType name="MarkType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:choice minOccurs="0"&gt;
 *              &lt;xsd:element ref="se:WellKnownName"/&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:choice&gt;
 *                      &lt;xsd:element ref="se:OnlineResource"/&gt;
 *                      &lt;xsd:element ref="se:InlineContent"/&gt;
 *                  &lt;/xsd:choice&gt;
 *                  &lt;xsd:element ref="se:Format"/&gt;
 *                  &lt;xsd:element minOccurs="0" ref="se:MarkIndex"/&gt;
 *              &lt;/xsd:sequence&gt;
 *          &lt;/xsd:choice&gt;
 *          &lt;xsd:element minOccurs="0" ref="se:Fill"/&gt;
 *          &lt;xsd:element minOccurs="0" ref="se:Stroke"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *        </code>
 *       </pre>
 *
 * @generated
 */
public class MarkBinding extends SLDMarkBinding {

    public MarkBinding(StyleFactory styleFactory, FilterFactory filterFactory, ResourceLocator resourceLocator) {
        super(styleFactory, filterFactory, resourceLocator);
    }

    /** @generated */
    @Override
    public QName getTarget() {
        return SE.Mark;
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
        Mark mark = (Mark) super.parse(instance, node, value);

        if (node.getChildValue("WellKnownName") == null) {
            String format = (String) node.getChildValue("Format");
            int markIndex = -1;

            if (node.hasChild("MarkIndex")) {
                markIndex = ((Number) node.getChildValue("MarkIndex")).intValue();
            }

            ExternalMark emark = null;

            if (node.hasChild("OnlineResource")) {
                emark = styleFactory.externalMark(
                        new OnLineResourceImpl((URI) node.getChildValue("OnlineResource")), format, markIndex);
            } else if (node.hasChild("InlineContent")) {
                Icon ic = (Icon) node.getChildValue("InlineContent");
                emark = styleFactory.externalMark(ic);
                emark.setFormat((String) node.getChildValue("Format"));
            }

            mark.setExternalMark(emark);
        }

        return mark;
    }
}
