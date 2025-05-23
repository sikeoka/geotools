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
package org.geotools.feature.type;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.AttributeType;
import org.geotools.api.feature.type.FeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.PropertyIsEqualTo;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Assert;
import org.junit.Test;

/** Test classes used to create and build {@link FeatureType} data structure. */
public class TypesTest {

    @Test
    public void testAttributeBuilder() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();
        AttributeTypeBuilder builder = new AttributeTypeBuilder();

        builder.binding(Integer.class);
        builder.minOccurs(1).maxOccurs(1);
        builder.defaultValue(0);
        builder.name("percent").description("Percent between 0 and 100");
        builder.restriction(ff.greaterOrEqual(ff.property("."), ff.literal(0)))
                .restriction(ff.lessOrEqual(ff.property("."), ff.literal(100)));

        final AttributeType PERCENT = builder.buildType();

        builder.minOccurs(1).maxOccurs(1);
        builder.defaultValue(0);
        builder.name("percent").description("Percent between 0 and 100");

        AttributeDescriptor a = builder.buildDescriptor("a", PERCENT);

        Assert.assertSame(a.getType(), PERCENT);
        Assert.assertEquals(a.getDefaultValue(), 0);

        Filter restrictions = ff.and(PERCENT.getRestrictions());
        Assert.assertTrue(restrictions.evaluate(50));
        Assert.assertFalse(restrictions.evaluate(150));
    }

    @Test
    public void testWithoutRestriction() {
        String attributeName = "string";
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder(); // $NON-NLS-1$
        builder.setName("test");
        builder.add(attributeName, String.class);
        SimpleFeatureType featureType = builder.buildFeatureType();

        SimpleFeature feature = SimpleFeatureBuilder.build(featureType, new Object[] {"Value"}, null);

        Assert.assertNotNull(feature);
    }
    /** This utility class is used by Types to prevent attribute modification. */
    @Test
    public void testRestrictionCheck() {
        FilterFactory fac = CommonFactoryFinder.getFilterFactory(null);

        String attributeName = "string";
        PropertyIsEqualTo filter = fac.equals(fac.property("."), fac.literal("Value"));

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder(); // $NON-NLS-1$
        builder.setName("test");
        builder.restriction(filter).minOccurs(1).nillable(false).add(attributeName, String.class);
        SimpleFeatureType featureType = builder.buildFeatureType();

        SimpleFeature feature = SimpleFeatureBuilder.build(featureType, new Object[] {"Value"}, null);

        Assert.assertNotNull(feature);
        Assert.assertTrue("valid", Types.isValid(feature));

        feature.setAttribute("string", null);
        Assert.assertFalse("invalid", Types.isValid(feature));
    }

    @Test
    public void testAssertNamedAssignable() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Test");
        builder.add("name", String.class);
        builder.add("age", Double.class);
        SimpleFeatureType test = builder.buildFeatureType();

        builder.setName("Test");
        builder.add("age", Double.class);
        builder.add("name", String.class);
        SimpleFeatureType test2 = builder.buildFeatureType();

        builder.setName("Test");
        builder.add("name", String.class);
        SimpleFeatureType test3 = builder.buildFeatureType();

        builder.setName("Test");
        builder.add("name", String.class);
        builder.add("distance", Double.class);
        SimpleFeatureType test4 = builder.buildFeatureType();

        Types.assertNameAssignable(test, test);
        Types.assertNameAssignable(test, test2);
        Types.assertNameAssignable(test2, test);
        try {
            Types.assertNameAssignable(test, test3);
            Assert.fail("Expected assertNameAssignable to fail as age is not covered");
        } catch (IllegalArgumentException expected) {
        }

        Types.assertOrderAssignable(test, test4);
    }
}
