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
package org.geotools.xsd;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.geotools.xs.XSConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class FacetTest {
    @Test
    public void testList() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getClass().getResourceAsStream("list.xml"));

        String schemaLocation = "http://geotools.org/test "
                + getClass().getResource("facets.xsd").getFile();

        doc.getDocumentElement()
                .setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", schemaLocation);

        DOMParser parser = new DOMParser(new XSConfiguration(), doc);
        Object o = parser.parse();
        Assert.assertTrue(o instanceof List);

        List list = (List) o;
        Assert.assertEquals(3, list.size());

        Assert.assertEquals(Integer.valueOf(1), list.get(0));
        Assert.assertEquals(Integer.valueOf(2), list.get(1));
        Assert.assertEquals(Integer.valueOf(3), list.get(2));
    }

    @Test
    public void testWhitespace() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getClass().getResourceAsStream("whitespace.xml"));

        String schemaLocation = "http://geotools.org/test "
                + getClass().getResource("facets.xsd").getFile();

        doc.getDocumentElement()
                .setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", schemaLocation);

        DOMParser parser = new DOMParser(new XSConfiguration(), doc);
        String s = (String) parser.parse();

        Assert.assertEquals("this is a normal string with some whitespace and some new lines", s);
    }

    @Test
    public void testCDATAWhitespace() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getClass().getResourceAsStream("whitespace-cdata.xml"));

        String schemaLocation = "http://geotools.org/test "
                + getClass().getResource("facets.xsd").getFile();

        doc.getDocumentElement()
                .setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", schemaLocation);

        DOMParser parser = new DOMParser(new XSConfiguration(), doc);
        String s = (String) parser.parse();

        Assert.assertEquals(
                " this is a \n" + " normal string \n" + " with some whitespace and \n" + " some new lines", s);
    }
}
