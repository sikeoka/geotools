/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2018, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.mbstyle.expression;

import org.geotools.api.filter.expression.Expression;
import org.geotools.mbstyle.parse.MBFormatException;
import org.json.simple.JSONArray;

public class MBVariableBinding extends MBExpression {

    public MBVariableBinding(JSONArray json) {
        super(json);
    }

    /**
     * Returns a string describing the type of the given value. Example: ["typeof", value]: string
     *
     * @return type of provided value expression (not implemented)
     */
    public Expression variableBindingLet() {
        return null; // new UnsupportedOperationException( ... )
    }

    /**
     * Returns a string describing the type of the given value. Example: ["typeof", value]: string
     *
     * @return type of provided value expresion (not implemented)
     */
    public Expression variableBindingVar() {
        return null; // new UnsupportedOperationException( ... )
    }

    @Override
    public Expression getExpression() throws MBFormatException {
        switch (name) {
            case "let":
                return variableBindingLet();
            case "var":
                return variableBindingVar();
            default:
                throw new MBFormatException(name + " is an unsupported variable binding expression");
        }
    }
}
