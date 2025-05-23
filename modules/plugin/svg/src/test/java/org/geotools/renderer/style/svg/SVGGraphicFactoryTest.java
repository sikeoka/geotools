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
package org.geotools.renderer.style.svg;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Icon;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Literal;
import org.geotools.data.ows.MockURLChecker;
import org.geotools.data.ows.URLCheckerException;
import org.geotools.data.ows.URLCheckers;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.renderer.style.GraphicCache;
import org.geotools.util.PreventLocalEntityResolver;
import org.geotools.util.factory.Hints;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SVGGraphicFactoryTest {

    private FilterFactory ff;

    @Before
    public void setUp() throws Exception {

        ff = CommonFactoryFinder.getFilterFactory(null);
    }

    @After
    public void cleanupCheckers() {
        URLCheckers.reset();
    }

    @Test
    public void testNull() throws Exception {
        SVGGraphicFactory svg = new SVGGraphicFactory();
        Assert.assertNull(svg.getIcon(null, ff.literal("http://www.nowhere.com"), null, 20));
    }

    @Test
    public void testInvalidPaths() throws Exception {
        SVGGraphicFactory svg = new SVGGraphicFactory();
        Assert.assertNull(svg.getIcon(null, ff.literal("http://www.nowhere.com"), "image/svg+not!", 20));
        try {
            svg.getIcon(null, ff.literal("ThisIsNotAUrl"), "image/svg", 20);
            Assert.fail("Should have throw an exception, invalid url");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLocalURL() throws Exception {
        SVGGraphicFactory svg = new SVGGraphicFactory();
        URL url = SVGGraphicFactory.class.getResource("gradient.svg");
        Assert.assertNotNull(url);
        // first call, non cached path
        Icon icon = svg.getIcon(null, ff.literal(url), "image/svg", 20);
        Assert.assertNotNull(icon);
        Assert.assertEquals(20, icon.getIconHeight());
        // check caching is working
        Assert.assertTrue(RenderableSVGCache.glyphCache.containsKey(url.toString()));
        // second call, hopefully using the cached path
        icon = svg.getIcon(null, ff.literal(url), "image/svg", 20);
        Assert.assertNotNull(icon);
        Assert.assertEquals(20, icon.getIconHeight());
    }

    @Test
    public void testLocalURLXEE() throws Exception {
        // disable references to entity stored on local file
        HashMap<Key, Object> hints = new HashMap<>();
        hints.put(Hints.ENTITY_RESOLVER, PreventLocalEntityResolver.INSTANCE);
        SVGGraphicFactory svg = new SVGGraphicFactory(hints);
        try {
            URL url = SVGGraphicFactory.class.getResource("attack.svg");
            SVGGraphicFactory.SVGIcon icon =
                    (SVGGraphicFactory.SVGIcon) svg.getIcon(null, ff.literal(url), "image/svg", 20);
            Assert.assertEquals("", getIconText(icon));
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("passwd"));
        }
    }

    private String getIconText(SVGGraphicFactory.SVGIcon icon) {
        GraphicsNode node = icon.svg.node;
        TextNode text = getTextNode(node);
        Assert.assertNotNull(text);
        return text.getText();
    }

    private TextNode getTextNode(GraphicsNode node) {
        if (node instanceof TextNode) {
            return (TextNode) node;
        } else if (node instanceof CompositeGraphicsNode) {
            List children = ((CompositeGraphicsNode) node).getChildren();
            for (Object child : children) {
                if (child instanceof GraphicsNode) {
                    TextNode result = getTextNode((GraphicsNode) child);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    @Test
    public void testNaturalSize() throws Exception {
        SVGGraphicFactory svg = new SVGGraphicFactory();
        URL url = SVGGraphicFactory.class.getResource("gradient.svg");
        Assert.assertNotNull(url);
        // first call, non cached path
        Icon icon = svg.getIcon(null, ff.literal(url), "image/svg", -1);
        Assert.assertNotNull(icon);
        Assert.assertEquals(500, icon.getIconHeight());
    }

    @Test
    public void testSizeWithPixels() throws Exception {
        SVGGraphicFactory svg = new SVGGraphicFactory();
        URL url = SVGGraphicFactory.class.getResource("gradient-pixels.svg");
        Assert.assertNotNull(url);
        // first call, non cached path
        Icon icon = svg.getIcon(null, ff.literal(url), "image/svg", -1);
        Assert.assertNotNull(icon);
        Assert.assertEquals(500, icon.getIconHeight());
    }

    /**
     * Tests that a fetched graphic is added to the cache, and that the {@link GraphicCache#clearCache()} method
     * correctly clears the cache.
     */
    @Test
    public void testClearCache() throws Exception {
        SVGGraphicFactory svg = new SVGGraphicFactory();
        URL url = SVGGraphicFactory.class.getResource("gradient.svg");

        Assert.assertNotNull(url);
        Icon icon = svg.getIcon(null, ff.literal(url), "image/svg", -1);
        Assert.assertNotNull(icon);

        String evaluatedUrl = ff.literal(url).evaluate(null, String.class);
        Assert.assertTrue(RenderableSVGCache.glyphCache.containsKey(evaluatedUrl));
        Assert.assertNotNull(RenderableSVGCache.glyphCache.get(evaluatedUrl));

        svg.clearCache();
        Assert.assertTrue(RenderableSVGCache.glyphCache.isEmpty());
    }

    @Test
    public void testConcurrentLoad() throws Exception {
        URL url = SVGGraphicFactory.class.getResource("gradient.svg");
        Assert.assertNotNull(url);
        Literal expression = ff.literal(url);

        // create N threads and have them load the same SVG over a graphic factory. Do it a number
        // of times with a new factory and a clear cache, trying to make sure the run was not just
        // a lucky one
        int THREADS = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < 50; i++) {
            // check that we are going to load the same path just once
            AtomicInteger counter = new AtomicInteger();
            SVGGraphicFactory svg = new SVGGraphicFactory() {
                @Override
                protected RenderableSVG toRenderableSVG(String svgfile, URL svgUrl) throws SAXException, IOException {
                    int value = counter.incrementAndGet();
                    Assert.assertEquals(1, value);
                    return super.toRenderableSVG(svgfile, svgUrl);
                }
            };

            // if all goes well, only one thread will actually load the SVG
            List<Future<Void>> futures = new ArrayList<>();
            for (int j = 0; j < THREADS * 4; j++) {
                executorService.submit(() -> {
                    Icon icon = svg.getIcon(null, expression, "image/svg", 20);
                    Assert.assertNotNull(icon);
                    Assert.assertEquals(20, icon.getIconHeight());
                    Assert.assertTrue(RenderableSVGCache.glyphCache.containsKey(url.toString()));
                    return null;
                });
            }
            // get all
            for (Future<Void> future : futures) {
                future.get();
            }
            // clear the cache
            RenderableSVGCache.glyphCache.clear();
        }
    }

    @Test
    public void testURLCheckerAllowed() throws Exception {
        URLCheckers.register(new MockURLChecker(u -> u.contains("gradient-pixels")));

        SVGGraphicFactory svg = new SVGGraphicFactory();
        URL url = SVGGraphicFactory.class.getResource("gradient-pixels.svg");
        assertNotNull(svg.getIcon(null, ff.literal(url), "image/svg", -1));
    }

    @Test
    public void testURLCheckerDisallowed() throws Exception {
        URLCheckers.register(new MockURLChecker("nope", u -> false));

        SVGGraphicFactory svg = new SVGGraphicFactory();
        URL url = SVGGraphicFactory.class.getResource("gradient-pixels.svg");
        assertThrows(URLCheckerException.class, () -> svg.getIcon(null, ff.literal(url), "image/svg", -1));
    }
}
