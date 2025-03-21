/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
 * Created on May 11, 2005, 6:21 PM
 */
package org.geotools.filter.function;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.api.filter.capability.FunctionName;
import org.geotools.api.filter.expression.Expression;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.capability.FunctionNameImpl;

/**
 * Calculates the unique value of an attribute for a given FeatureCollection and Expression.
 *
 * @author Cory Horner
 * @since 2.2M2
 */
public class Collection_UniqueFunction extends FunctionExpressionImpl {
    /** The logger for the filter module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(Collection_UniqueFunction.class);

    SimpleFeatureCollection previousFeatureCollection = null;
    Object unique = null;

    public static FunctionName NAME = new FunctionNameImpl(
            "Collection_Unique", parameter("unique", Object.class), parameter("expression", Object.class));

    /** Creates a new instance of Collection_UniqueFunction */
    public Collection_UniqueFunction() {
        super(NAME);
    }

    /**
     * Calculate unique (using FeatureCalc) - only one parameter is used.
     *
     * @param collection collection to calculate the unique
     * @param expression Single Expression argument
     * @return An object containing the unique value of the attributes
     */
    static CalcResult calculateUnique(SimpleFeatureCollection collection, Expression expression)
            throws IllegalFilterException, IOException {
        UniqueVisitor uniqueVisitor = new UniqueVisitor(expression);
        collection.accepts(uniqueVisitor, null);

        return uniqueVisitor.getResult();
    }

    /**
     * The provided arguments are evaulated with respect to the FeatureCollection.
     *
     * <p>For an aggregate function (like unique) please use the WFS mandated XPath syntax to refer to featureMember
     * content.
     *
     * <p>To refer to all 'X': <code>featureMember/asterisk/X</code>
     */
    @Override
    public void setParameters(List<Expression> args) {
        // if we see "featureMembers/*/ATTRIBUTE" change to "ATTRIBUTE"
        org.geotools.api.filter.expression.Expression expr = args.get(0);
        expr = (org.geotools.api.filter.expression.Expression)
                expr.accept(new CollectionFeatureMemberFilterVisitor(), null);
        args.set(0, expr);
        super.setParameters(args);
    }

    @Override
    public Object evaluate(Object feature) {
        if (feature == null) {
            return Integer.valueOf(0); // no features were visited in the making of this answer
        }
        SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) feature;
        Expression expr = getExpression(0);
        synchronized (featureCollection) {
            if (featureCollection != previousFeatureCollection) {
                previousFeatureCollection = featureCollection;
                unique = null;
                try {
                    CalcResult result = calculateUnique(featureCollection, expr);
                    if (result != null) {
                        unique = result.getValue();
                    }
                } catch (IllegalFilterException | IOException e) {
                    LOGGER.log(Level.FINER, e.getLocalizedMessage(), e);
                }
            }
        }
        return unique;
    }

    public void setExpression(Expression e) {
        setParameters(Collections.singletonList(e));
    }
}
