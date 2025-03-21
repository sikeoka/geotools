/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geootols.filter.text.cql_2;

import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Function;
import org.geotools.filter.text.commons.BuildResultStack;
import org.geotools.filter.text.cql2.CQLException;

/**
 * Abstract class for function builder. Copied from gt-cql, should be removed once the modules are merged.
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
abstract class FunctionBuilder {

    private FilterFactory filterFactory;
    private BuildResultStack resultStack;

    public FunctionBuilder(final BuildResultStack resultStack, final FilterFactory filterFactory) {

        assert resultStack != null;
        assert filterFactory != null;

        this.resultStack = resultStack;
        this.filterFactory = filterFactory;
    }

    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    public BuildResultStack getResultStack() {
        return resultStack;
    }

    public abstract Function build() throws CQLException;
}
