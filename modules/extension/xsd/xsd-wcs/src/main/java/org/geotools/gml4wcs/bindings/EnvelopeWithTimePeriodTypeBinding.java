/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2019, Open Source Geospatial Foundation (OSGeo)
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
 *
 */

package org.geotools.gml4wcs.bindings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.geotools.api.referencing.crs.CompoundCRS;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.TemporalCRS;
import org.geotools.api.temporal.Position;
import org.geotools.geometry.GeneralBounds;
import org.geotools.geometry.GeneralPosition;
import org.geotools.gml4wcs.GML;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.temporal.object.DefaultPosition;
import org.geotools.xsd.AbstractComplexBinding;
import org.geotools.xsd.ElementInstance;
import org.geotools.xsd.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Binding object for the type http://www.opengis.net/gml:EnvelopeWithTimePeriodType.
 *
 * <p>
 *
 * <pre>
 *  <code>
 *  &lt;complexType name=&quot;EnvelopeWithTimePeriodType&quot;&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Envelope that includes also a temporal extent.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base=&quot;gml:EnvelopeType&quot;&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs=&quot;2&quot; minOccurs=&quot;2&quot; ref=&quot;gml:timePosition&quot;/&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute default=&quot;#ISO-8601&quot; name=&quot;frame&quot; type=&quot;anyURI&quot; use=&quot;optional&quot;/&gt;
 *          &lt;/extension&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt;
 *
 * </code>
 *  </pre>
 *
 * @generated
 */
public class EnvelopeWithTimePeriodTypeBinding extends AbstractComplexBinding {

    /** @generated */
    @Override
    public QName getTarget() {
        return GML.EnvelopeWithTimePeriodType;
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
        return GeneralBounds.class;
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
        GeneralBounds envelope = (GeneralBounds) value;

        List<Node> timePositions = node.getChildren("timePosition");

        if (timePositions != null && !timePositions.isEmpty()) {
            final Map<String, Object> properties = new HashMap<>(4);
            properties.put(CoordinateReferenceSystem.NAME_KEY, "Compound");
            properties.put(CoordinateReferenceSystem.DOMAIN_OF_VALIDITY_KEY, ExtentImpl.WORLD);

            CoordinateReferenceSystem crs = new DefaultCompoundCRS(properties, new CoordinateReferenceSystem[] {
                envelope.getCoordinateReferenceSystem(), DefaultTemporalCRS.TRUNCATED_JULIAN
            });

            double[] minCP = new double[envelope.getDimension() + 1];
            double[] maxCP = new double[envelope.getDimension() + 1];

            for (int i = 0; i < envelope.getDimension(); i++) {
                minCP[i] = envelope.getLowerCorner().getOrdinate(i);
                maxCP[i] = envelope.getUpperCorner().getOrdinate(i);
            }

            DefaultTemporalCRS TCRS = (DefaultTemporalCRS)
                    ((CompoundCRS) crs).getCoordinateReferenceSystems().get(1);

            Node timePositionNodeBegin = timePositions.get(0);
            Node timePositionNodeEnd = timePositions.get(1);
            minCP[minCP.length - 1] = TCRS.toValue(((DefaultPosition) timePositionNodeBegin.getValue()).getDate());
            maxCP[maxCP.length - 1] = TCRS.toValue(((DefaultPosition) timePositionNodeEnd.getValue()).getDate());

            GeneralPosition minDP = new GeneralPosition(minCP);
            minDP.setCoordinateReferenceSystem(crs);
            GeneralPosition maxDP = new GeneralPosition(maxCP);
            maxDP.setCoordinateReferenceSystem(crs);

            GeneralBounds envelopeWithTime = new GeneralBounds(minDP, maxDP);

            return envelopeWithTime;
        }

        return envelope;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.xsd.AbstractComplexBinding#getExecutionMode()
     */
    @Override
    public int getExecutionMode() {
        return AFTER;
    }

    /*
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     */
    @Override
    public Element encode(Object object, Document document, Element value) throws Exception {
        GeneralBounds envelope = (GeneralBounds) object;

        if (envelope == null) {
            value.appendChild(document.createElementNS(GML.NAMESPACE, org.geotools.gml3.GML.Null.getLocalPart()));
        }

        return null;
    }

    @Override
    public Object getProperty(Object object, QName name) {
        GeneralBounds envelope = (GeneralBounds) object;

        if (envelope == null) {
            return null;
        }

        if (name.getLocalPart().equals("timePosition")) {
            CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();

            TemporalCRS temporalCRS = null;

            if (crs instanceof CompoundCRS) {
                List CRSs = ((DefaultCompoundCRS) crs).getCoordinateReferenceSystems();

                for (Object item : CRSs) {
                    if (item instanceof TemporalCRS) {
                        temporalCRS = (TemporalCRS) item;
                    }
                }
            }

            if (temporalCRS != null) {
                List<Position> envelopePositions = new LinkedList<>();

                Position beginning = new DefaultPosition(((DefaultTemporalCRS) temporalCRS)
                        .toDate(envelope.getLowerCorner().getOrdinate(envelope.getDimension() - 1)));
                Position ending = new DefaultPosition(((DefaultTemporalCRS) temporalCRS)
                        .toDate(envelope.getUpperCorner().getOrdinate(envelope.getDimension() - 1)));

                envelopePositions.add(beginning);
                envelopePositions.add(ending);

                return envelopePositions;
            }
        }

        return null;
    }
}
