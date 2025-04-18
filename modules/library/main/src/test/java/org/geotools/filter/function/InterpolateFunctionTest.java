/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.function;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.geotools.api.filter.capability.FunctionName;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.filter.expression.Function;
import org.geotools.api.filter.expression.Literal;
import org.geotools.api.filter.expression.VolatileFunction;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.util.Converter;
import org.geotools.util.Converters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Unit tests for the Interpolate function.
 *
 * @author Michael Bedward
 */
@RunWith(Parameterized.class)
public class InterpolateFunctionTest extends SEFunctionTestBase {

    private static final double TOL = 1.0e-6d;
    private final Double[] data = {10.0, 20.0, 40.0, 80.0};
    private final Double[] values = {1.0, 2.0, 3.0, 4.0};
    private final Color[] colors = {Color.RED, Color.ORANGE, Color.GREEN, Color.BLUE};
    private final boolean dynamic;

    public InterpolateFunctionTest(String name, boolean dynamic) {
        this.dynamic = dynamic;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[] {"static", false});
        result.add(new Object[] {"dynamic", true});

        return result;
    }

    @Before
    public void setup() {
        parameters = new ArrayList<>();
    }

    @Test
    public void testFindInterpolateFunction() throws Exception {
        Literal fallback = ff2.literal("NOT_FOUND");
        setupParameters(data, values);
        Function fn = finder.findFunction("Interpolate", parameters, fallback);
        Object result = fn.evaluate(feature(0));

        assertNotEquals("Could not locate 'Interpolate' function", result, fallback.getValue());
    }

    @Test
    public void testLinearNumericInterpolation() throws Exception {
        testLinearNumericInterpolation(Double.class);
    }

    @Test
    public void testLinearNumericInterpolationNullContext() throws Exception {
        testLinearNumericInterpolation(null);
    }

    @Test
    public void testLinearNumericInterpolationObjectContext() throws Exception {
        testLinearNumericInterpolation(Object.class);
    }

    void testLinearNumericInterpolation(Class<?> context) throws Exception {
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));
        parameters.add(ff2.literal(InterpolateFunction.MODE_LINEAR));

        Function fn = finder.findFunction("interpolate", parameters);

        // test mid-points
        Double result;
        double expected;
        for (int i = 1; i < data.length; i++) {
            double testValue = (data[i] + data[i - 1]) / 2.0;
            result = (Double) fn.evaluate(feature(Double.valueOf(testValue)), context);
            expected = (values[i] + values[i - 1]) / 2.0;
            assertEquals(expected, result, TOL);
        }

        // test boundaries
        for (int i = 0; i < data.length; i++) {
            result = (Double) fn.evaluate(feature(Double.valueOf(data[i])), context);
            expected = values[i];
            assertEquals(expected, result, TOL);
        }

        // test outside range of interpolation points
        result = (Double) fn.evaluate(feature(Double.valueOf(data[0] - 10)), context);
        assertEquals(values[0], result, TOL);

        result = (Double) fn.evaluate(feature(Double.valueOf(data[data.length - 1] + 10)), context);
        assertEquals(values[values.length - 1], result, TOL);
    }

    /** Verify the return value is {@link Converters#convert(Object, Class) converted} to the requested context type */
    @Test
    public void testNumericInterpolationStringContext() throws Exception {
        double testValue = data[0];
        assertThat(runNumeric(InterpolateFunction.MODE_LINEAR, testValue, String.class), instanceOf(String.class));
        assertThat(runNumeric(InterpolateFunction.MODE_CUBIC, testValue, String.class), instanceOf(String.class));
        assertThat(runNumeric(InterpolateFunction.MODE_COSINE, testValue, String.class), instanceOf(String.class));
    }

    /**
     * Verify the return value is {@link Converters#convert(Object, Class) converted} to the requested context type and
     * returns {@code null} if no conversion is possible
     */
    @Test
    public void testNumericInterpolationUnsupportedContextReturnsNull() throws Exception {
        double testValue = data[0];
        // preflight, make sure there's no converter for Double -> Date
        assertNull(Converters.convert(1d, Date.class));
        Class<?> context = Date.class;
        assertNull(runNumeric(InterpolateFunction.MODE_LINEAR, testValue, context));
        assertNull(runNumeric(InterpolateFunction.MODE_CUBIC, testValue, context));
        assertNull(runNumeric(InterpolateFunction.MODE_COSINE, testValue, context));
    }

    Object runNumeric(String mode, double testValue, Class<?> context) throws Exception {
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));
        parameters.add(ff2.literal(mode));

        Function fn = finder.findFunction("interpolate", parameters);
        return fn.evaluate(feature(testValue), context);
    }

    @Test
    public void testLinearColorInterpolation() throws Exception {
        testLinearColorInterpolation(Color.class);
    }

    @Test
    public void testLinearColorInterpolationNullContext() throws Exception {
        testLinearColorInterpolation(null);
    }

    @Test
    public void testLinearColorInterpolationObjectContext() throws Exception {
        testLinearColorInterpolation(Object.class);
    }

    void testLinearColorInterpolation(Class<?> context) throws Exception {
        // System.out.println("   testLinearColorInterpolation");

        setupParameters(data, colors);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_COLOR));
        parameters.add(ff2.literal(InterpolateFunction.MODE_LINEAR));

        Function fn = finder.findFunction("interpolate", parameters);
        Color result = null;

        // at mid-points
        for (int i = 1; i < data.length; i++) {
            double testValue = (data[i] + data[i - 1]) / 2.0;
            result = (Color) fn.evaluate(feature(testValue), context);
            Color expected = new Color(
                    (int) Math.round((colors[i].getRed() + colors[i - 1].getRed()) / 2.0),
                    (int) Math.round((colors[i].getGreen() + colors[i - 1].getGreen()) / 2.0),
                    (int) Math.round((colors[i].getBlue() + colors[i - 1].getBlue()) / 2.0));
            assertEquals(expected, result);
        }

        // at interpolation points
        for (int i = 0; i < data.length; i++) {
            result = (Color) fn.evaluate(feature(data[i]), context);
            assertEquals(colors[i], result);
        }

        // outside range of interpolation points
        result = (Color) fn.evaluate(feature(Double.valueOf(data[0] - 10)), context);
        assertEquals(colors[0], result);

        result = (Color) fn.evaluate(feature(Double.valueOf(data[data.length - 1] + 10)), context);
        assertEquals(colors[colors.length - 1], result);
    }

    @Test
    public void testCosineNumericInterpolation() throws Exception {
        // System.out.println("   testCosineNumericInterpolation");

        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));
        parameters.add(ff2.literal(InterpolateFunction.MODE_COSINE));

        Function fn = finder.findFunction("interpolate", parameters);

        // test points within segments away from mid-points
        double t = 0.1;
        Double result;
        double expected;
        for (int i = 1; i < data.length; i++) {
            double testValue = data[i - 1] + t * (data[i] - data[i - 1]);
            result = fn.evaluate(feature(Double.valueOf(testValue)), Double.class);

            expected = values[i - 1] + (values[i] - values[i - 1]) * (1.0 - Math.cos(t * Math.PI)) * 0.5;
            assertEquals(expected, result, TOL);
        }

        // test boundaries
        for (int i = 0; i < data.length; i++) {
            result = fn.evaluate(feature(Double.valueOf(data[i])), Double.class);
            expected = values[i];
            assertEquals(expected, result, TOL);
        }

        // test outside range of interpolation points
        result = fn.evaluate(feature(Double.valueOf(data[0] - 10)), Double.class);
        assertEquals(values[0], result, TOL);

        result = fn.evaluate(feature(Double.valueOf(data[data.length - 1] + 10)), Double.class);
        assertEquals(values[values.length - 1], result, TOL);
    }

    @Test
    public void testCubicNumericInterpolation() throws Exception {
        // System.out.println("   testCubicNumericInterpolation");

        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));
        parameters.add(ff2.literal(InterpolateFunction.MODE_CUBIC));

        Function fn = finder.findFunction("interpolate", parameters);

        // test points within segments away from mid-points
        double t = 0.1;
        Double result;
        double expected;
        for (int i = 2; i < data.length - 2; i++) {
            double testValue = data[i - 1] + t * (data[i] - data[i - 1]);
            result = fn.evaluate(feature(Double.valueOf(testValue)), Double.class);

            expected = cubic(testValue, new double[] {data[i - 2], data[i - 1], data[i], data[i + 1]}, new double[] {
                values[i - 2], values[i - 1], values[i], values[i + 1]
            });
            assertEquals(expected, result, TOL);
        }

        // test outside range of interpolation points
        result = fn.evaluate(feature(Double.valueOf(data[0] - 10)), Double.class);
        assertEquals(values[0], result, TOL);

        result = fn.evaluate(feature(Double.valueOf(data[data.length - 1] + 10)), Double.class);
        assertEquals(values[values.length - 1], result, TOL);
    }

    @Test
    public void testAsRasterData() throws Exception {
        // System.out.println("   testRasterData");

        setupParameters(data, colors);
        parameters.set(0, ff2.literal("RasterData"));
        parameters.add(ff2.literal(InterpolateFunction.METHOD_COLOR));
        parameters.add(ff2.literal(InterpolateFunction.MODE_LINEAR));

        Function fn = finder.findFunction("interpolate", parameters);
        Color result = null;

        // at mid-points
        for (int i = 1; i < data.length; i++) {
            double rasterValue = (data[i] + data[i - 1]) / 2.0;
            result = fn.evaluate(rasterValue, Color.class);
            Color expected = new Color(
                    (int) Math.round((colors[i].getRed() + colors[i - 1].getRed()) / 2.0),
                    (int) Math.round((colors[i].getGreen() + colors[i - 1].getGreen()) / 2.0),
                    (int) Math.round((colors[i].getBlue() + colors[i - 1].getBlue()) / 2.0));
            assertEquals(expected, result);
        }

        // at interpolation points
        for (int i = 0; i < data.length; i++) {
            result = fn.evaluate(data[i], Color.class);
            assertEquals(colors[i], result);
        }

        // outside range of interpolation points
        result = fn.evaluate(Double.valueOf(data[0] - 10), Color.class);
        assertEquals(colors[0], result);

        result = fn.evaluate(Double.valueOf(data[data.length - 1] + 10), Color.class);
        assertEquals(colors[colors.length - 1], result);
    }

    @Test
    public void testForOutOfRangeColorValues() {
        // System.out.println("   out of range color values");

        parameters = new ArrayList<>();
        parameters.add(ff2.literal("RasterData"));

        // Create interpolation points that will lead to a cubic
        // curve going out of range: the unclamped curve will dip
        // below 0 betwee points 1 and 2 and go above 255 between
        // points 3 and 4
        double[] x = {0, 1, 2, 3, 4, 5};
        int[] reds = {128, 0, 0, 255, 255, 128};

        for (int i = 0; i < x.length; i++) {
            parameters.add(ff2.literal(x[i]));
            String color = String.format("#%02x0000", reds[i]);
            parameters.add(ff2.literal(color));
        }

        parameters.add(ff2.literal(InterpolateFunction.METHOD_COLOR));
        parameters.add(ff2.literal(InterpolateFunction.MODE_CUBIC));

        Function fn = finder.findFunction("interpolate", parameters);

        // check between points 1 and 2
        Color result = fn.evaluate(Double.valueOf(1.5), Color.class);
        assertEquals(0, result.getRed());

        // check between points 3 and 4
        result = fn.evaluate(Double.valueOf(3.5), Color.class);
        assertEquals(255, result.getRed());
    }

    @Test
    public void testNoMethodParameter() throws Exception {
        // System.out.println("   testNoMethodParameter");

        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.MODE_LINEAR));

        Function fn = finder.findFunction("interpolate", parameters);

        // test mid-points
        Double result;
        double expected;
        for (int i = 1; i < data.length; i++) {
            double testValue = (data[i] + data[i - 1]) / 2.0;
            result = fn.evaluate(feature(Double.valueOf(testValue)), Double.class);
            expected = (values[i] + values[i - 1]) / 2.0;
            assertEquals(expected, result, TOL);
        }
    }

    @Test
    public void testNoModeParameter() throws Exception {
        // System.out.println("   testNoModeParameter");

        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));

        Function fn = finder.findFunction("interpolate", parameters);

        // test mid-points
        Double result;
        double expected;
        for (int i = 1; i < data.length; i++) {
            double testValue = (data[i] + data[i - 1]) / 2.0;
            result = fn.evaluate(feature(Double.valueOf(testValue)), Double.class);
            expected = (values[i] + values[i - 1]) / 2.0;
            assertEquals(expected, result, TOL);
        }
    }

    /** If {@code context == Color.class} and {@code method == NUMERIC} throws an IAE */
    @Test
    public void testColorValuesNumericMethodMismatch() throws Exception {
        /*
         * Set interpolation points but let the function default to
         * linear / numeric
         */
        setupParameters(data, colors);

        Function fn = finder.findFunction("interpolate", parameters);
        boolean gotEx = false;
        try {
            fn.evaluate(feature(data[1]), Color.class);
        } catch (IllegalArgumentException ex) {
            gotEx = true;
        }

        assertTrue(gotEx);
    }

    /**
     * Verify the output is delegated to {@link Converters#convert(Object, Class)} for the provided {@code context}
     * {@literal evaluate()} parameter when {@literal method == COLOR}
     */
    @Test
    public void testMethodColorWithStringContext() throws Exception {
        testMethodColorWithStringContext(InterpolateFunction.MODE_LINEAR);
        testMethodColorWithStringContext(InterpolateFunction.MODE_COSINE);
        testMethodColorWithStringContext(InterpolateFunction.MODE_CUBIC);
    }

    /**
     * Verify the function returns {@code null} when asked to convert the result to a type for which there's no capable
     * {@link Converter}, as specified in the interface contract
     */
    @Test
    public void testMethodColorWithUnknownConversionContextReturnsNull() throws Exception {
        double testValue = (data[1] + data[0]) / 2.0;
        Object result;

        // preflight, make sure there's no converter for Color -> Date
        assertNull(Converters.convert(Color.RED, Date.class));
        result = runMethodColor(InterpolateFunction.MODE_LINEAR, testValue, Date.class);
        assertNull(result);

        // preflight, make sure there's no converter for Color -> Double
        assertNull(Converters.convert(Color.RED, Double.class));
        result = runMethodColor(InterpolateFunction.MODE_COSINE, testValue, Double.class);
        assertNull(result);

        // preflight, make sure there's no converter for Color -> Integer
        assertNull(Converters.convert(Color.RED, Integer.class));
        result = runMethodColor(InterpolateFunction.MODE_CUBIC, testValue, Integer.class);
        assertNull(result);
    }

    void testMethodColorWithStringContext(String interpolationMode) throws Exception {
        double testValue = (data[1] + data[0]) / 2.0;
        Object result = runMethodColor(interpolationMode, testValue, String.class);
        assertThat(result, instanceOf(String.class));
        Pattern hexColor = Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$");
        assertTrue(hexColor.matcher((String) result).matches());
    }

    private Object runMethodColor(String interpolationMode, double testValue, Class<?> context) throws Exception {
        setupParameters(data, colors);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_COLOR));
        parameters.add(ff2.literal(interpolationMode));
        Function fn = finder.findFunction("interpolate", parameters);
        return fn.evaluate(feature(testValue), context);
    }

    /** Set up parameters for the Interpolate function with a set of input data and output values */
    private void setupParameters(Object[] data, Object[] values) {

        if (data.length != values.length) {
            throw new IllegalArgumentException("data and values arrays should be the same length");
        }

        parameters = new ArrayList<>();
        parameters.add(ff2.property("value"));

        for (int i = 0; i < data.length; i++) {
            if (dynamic) {
                parameters.add(new VolaliteLiteral(data[i]));
                parameters.add(new VolaliteLiteral(values[i]));
            } else {
                parameters.add(ff2.literal(data[i]));
                parameters.add(ff2.literal(values[i]));
            }
        }
    }

    private static double cubic(double x, double[] xi, double[] yi) {
        double span01 = xi[1] - xi[0];
        double span12 = xi[2] - xi[1];
        double span23 = xi[3] - xi[2];

        double t = (x - xi[1]) / span12;
        double t2 = t * t;
        double t3 = t2 * t;

        double m1 = 0.5 * ((yi[2] - yi[1]) / span12 + (yi[1] - yi[0]) / span01);
        double m2 = 0.5 * ((yi[3] - yi[2]) / span23 + (yi[2] - yi[1]) / span12);

        double y = (2 * t3 - 3 * t2 + 1) * yi[1]
                + (t3 - 2 * t2 + t) * span12 * m1
                + (-2 * t3 + 3 * t2) * yi[2]
                + (t3 - t2) * span12 * m2;

        return y;
    }

    @Test
    public void testNullSafeColor() throws Exception {
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_COLOR));

        Function fn = finder.findFunction("interpolate", parameters);
        Object result = fn.evaluate(null, Color.class);
        assertNull(result);
    }

    @Test
    public void testNullSafeNumeric() throws Exception {
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));

        Function fn = finder.findFunction("interpolate", parameters);
        Object result = fn.evaluate(null, Double.class);
        assertNull(result);
    }

    @Test
    public void testEqualsHashCode() {
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_COLOR));

        Function fn1 = finder.findFunction("interpolate", parameters);
        Function fn2 = finder.findFunction("interpolate", parameters);
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));
        Function fn3 = finder.findFunction("interpolate", parameters);

        // symmetric
        assertEquals(fn1, fn2);
        assertEquals(fn2, fn1);
        // same hashcode
        assertEquals(fn1.hashCode(), fn2.hashCode());

        // but not equal to fn3
        assertNotEquals(fn1, fn3);
        assertNotEquals(fn2, fn3);
    }

    @Test
    public void testInterpPointNumeric() {
        // numeric mode
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_NUMERIC));
        InterpolateFunctionSpy function = new InterpolateFunctionSpy(parameters);
        function.evaluate(null, Double.class); // force initialization
        if (dynamic) {
            assertTrue(function.isDynamic());
        } else {
            assertTrue(function.isStaticNumeric());
        }
    }

    @Test
    public void testInterpPointColor() {
        // numeric mode
        setupParameters(data, values);
        parameters.add(ff2.literal(InterpolateFunction.METHOD_COLOR));
        InterpolateFunctionSpy function = new InterpolateFunctionSpy(parameters);
        function.evaluate(null, Color.class); // force initialization
        if (dynamic) {
            assertTrue(function.isDynamic());
        } else {
            assertTrue(function.isStaticColor());
        }
    }

    /** A simple literal function wrapper that should force the interpolate function to work in */
    static class VolaliteLiteral extends FunctionExpressionImpl implements VolatileFunction {

        public static FunctionName NAME = new FunctionNameImpl("volatileLiteral", Double.class);
        private final Object value;

        public VolaliteLiteral(Object value) {
            super(NAME);
            this.value = value;
        }

        @Override
        public Object evaluate(Object feature) {
            return value;
        }
    }

    /** Subclass allowing to check which kind of InterpPoint was used for tests */
    static class InterpolateFunctionSpy extends InterpolateFunction {

        public InterpolateFunctionSpy(List<Expression> parameters) {
            super(parameters, null);
        }

        public boolean isStaticNumeric() {
            return interpPoints.stream().allMatch(ip -> ip instanceof ConstantNumericPoint);
        }

        public boolean isStaticColor() {
            return interpPoints.stream().allMatch(ip -> ip instanceof ConstantColorPoint);
        }

        public boolean isDynamic() {
            return interpPoints.stream().allMatch(ip -> ip instanceof DynamicPoint);
        }
    }
}
