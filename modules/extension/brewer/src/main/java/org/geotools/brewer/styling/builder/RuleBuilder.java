/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.brewer.styling.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.geotools.api.filter.Filter;
import org.geotools.api.style.GraphicLegend;
import org.geotools.api.style.Rule;
import org.geotools.api.style.Symbolizer;
import org.geotools.api.util.InternationalString;
import org.geotools.util.SimpleInternationalString;

public class RuleBuilder extends AbstractStyleBuilder<Rule> {
    List<Symbolizer> symbolizers = new ArrayList<>();

    Builder<? extends Symbolizer> symbolizerBuilder;

    String name;

    InternationalString ruleAbstract;

    double minScaleDenominator;

    double maxScaleDenominator;

    Filter filter = null;

    boolean elseFilter;

    InternationalString title;

    protected Map<String, String> options = new LinkedHashMap<>();

    private GraphicLegendBuilder legend = new GraphicLegendBuilder(this).unset();

    public RuleBuilder() {
        this(null);
    }

    public RuleBuilder(FeatureTypeStyleBuilder parent) {
        super(parent);
        reset();
    }

    public RuleBuilder name(String name) {
        unset = false;
        this.name = name;
        return this;
    }

    public RuleBuilder title(InternationalString title) {
        unset = false;
        this.title = title;
        return this;
    }

    public RuleBuilder title(String title) {
        unset = false;
        this.title = new SimpleInternationalString(title);
        return this;
    }

    public RuleBuilder ruleAbstract(InternationalString ruleAbstract) {
        unset = false;
        this.ruleAbstract = ruleAbstract;
        return this;
    }

    public RuleBuilder ruleAbstract(String ruleAbstract) {
        unset = false;
        this.ruleAbstract = new SimpleInternationalString(ruleAbstract);
        return this;
    }

    public GraphicLegendBuilder legend() {
        unset = false;
        return legend;
    }

    public RuleBuilder min(double minScaleDenominator) {
        unset = false;
        if (minScaleDenominator < 0)
            throw new IllegalArgumentException("Invalid min scale denominator, should be positive or 0");
        this.minScaleDenominator = minScaleDenominator;
        return this;
    }

    public RuleBuilder max(double maxScaleDenominator) {
        unset = false;
        if (maxScaleDenominator < 0)
            throw new IllegalArgumentException("Invalid max scale denominator, should be positive or 0");
        this.maxScaleDenominator = maxScaleDenominator;
        return this;
    }

    public RuleBuilder elseFilter() {
        unset = false;
        this.elseFilter = true;
        this.filter = null;
        return this;
    }

    public RuleBuilder filter(Filter filter) {
        unset = false;
        this.elseFilter = false;
        this.filter = filter;
        return this;
    }

    public RuleBuilder filter(String cql) {
        unset = false;
        this.elseFilter = false;
        this.filter = cqlFilter(cql);
        return this;
    }

    public PointSymbolizerBuilder point() {
        unset = false;
        if (symbolizerBuilder != null) symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new PointSymbolizerBuilder(this);
        return (PointSymbolizerBuilder) symbolizerBuilder;
    }

    public LineSymbolizerBuilder line() {
        unset = false;
        if (symbolizerBuilder != null) symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new LineSymbolizerBuilder(this);
        return (LineSymbolizerBuilder) symbolizerBuilder;
    }

    public PolygonSymbolizerBuilder polygon() {
        unset = false;
        if (symbolizerBuilder != null) symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new PolygonSymbolizerBuilder(this);
        return (PolygonSymbolizerBuilder) symbolizerBuilder;
    }

    public TextSymbolizerBuilder text() {
        unset = false;
        if (symbolizerBuilder != null) symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new TextSymbolizerBuilder(this);
        return (TextSymbolizerBuilder) symbolizerBuilder;
    }

    public RasterSymbolizerBuilder raster() {
        unset = false;
        if (symbolizerBuilder != null) symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new RasterSymbolizerBuilder(this);
        return (RasterSymbolizerBuilder) symbolizerBuilder;
    }

    public ExtensionSymbolizerBuilder extension() {
        unset = false;
        if (symbolizerBuilder != null) symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new ExtensionSymbolizerBuilder(this);
        return (ExtensionSymbolizerBuilder) symbolizerBuilder;
    }

    @Override
    public Rule build() {
        if (unset) {
            return null;
        }
        if (symbolizerBuilder == null && symbolizers.isEmpty()) {
            symbolizerBuilder = new PointSymbolizerBuilder();
        }
        if (symbolizerBuilder != null) {
            symbolizers.add(symbolizerBuilder.build());
        }

        Rule rule = sf.createRule();
        rule.setName(name);
        // TODO: rule's description cannot be set
        rule.getDescription().setTitle(title);
        rule.getDescription().setAbstract(ruleAbstract);
        rule.setMinScaleDenominator(minScaleDenominator);
        rule.setMaxScaleDenominator(maxScaleDenominator);
        rule.setFilter(filter);
        rule.setElseFilter(elseFilter);
        rule.symbolizers().addAll(symbolizers);
        GraphicLegend gl = legend.build();
        if (gl != null) {
            rule.setLegend(gl);
        }
        rule.getOptions().putAll(options);
        if (parent == null) {
            reset();
        }
        return rule;
    }

    @Override
    public RuleBuilder unset() {
        return (RuleBuilder) super.unset();
    }

    @Override
    public RuleBuilder reset() {
        name = null;
        title = null;
        ruleAbstract = null;
        minScaleDenominator = 0;
        maxScaleDenominator = Double.POSITIVE_INFINITY;
        filter = Filter.INCLUDE;
        elseFilter = false;
        symbolizers.clear();
        legend.unset();
        unset = false;
        this.options.clear();
        return this;
    }

    @Override
    public RuleBuilder reset(Rule rule) {
        if (rule == null) {
            return unset();
        }
        name = rule.getName();
        title = Optional.ofNullable(rule.getDescription())
                .map(d -> d.getTitle())
                .orElse(null);
        ruleAbstract = Optional.ofNullable(rule.getDescription())
                .map(d -> d.getAbstract())
                .orElse(null);
        minScaleDenominator = rule.getMinScaleDenominator();
        maxScaleDenominator = rule.getMaxScaleDenominator();
        filter = rule.getFilter();
        elseFilter = rule.isElseFilter();
        symbolizers.clear();
        symbolizers.addAll(rule.symbolizers()); // TODO: unpack into builders in order to "copy"
        symbolizerBuilder = null;
        unset = false;
        legend.reset(rule.getLegend());
        options.putAll(rule.getOptions());
        return this;
    }

    @Override
    protected void buildStyleInternal(StyleBuilder sb) {
        sb.featureTypeStyle().rule().init(this);
    }

    public RuleBuilder option(String name, String value) {
        options.put(name, value);
        return this;
    }
}
