/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2015, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.geotools.api.util.InternationalString;
import org.geotools.api.util.ProgressListener;
import org.junit.Test;

/**
 * Test suite for {@link SubProgressListener}
 *
 * @author groldan
 * @version $Id$
 */
public class SubProgressListenerTest {

    @Test
    public void testSubProgressStartComplete() {
        SimpleProgressListener parent = new SimpleProgressListener();
        SubProgressListener sub = new SubProgressListener(parent, 50);

        sub.started();
        assertEquals(0F, sub.getProgress(), 0f);
        sub.complete();
        assertEquals(100F, sub.getProgress(), 0f);
    }

    @Test
    public void testSubProgressStartFirstListener() {
        SimpleProgressListener parent = new SimpleProgressListener();
        SubProgressListener sub = new SubProgressListener(parent, 0, 50);
        sub.started();
        assertTrue(parent.getStarted());
    }

    @Test
    public void testSubProgressStartSubsequentListener() {
        SimpleProgressListener parent = new SimpleProgressListener();
        SubProgressListener sub = new SubProgressListener(parent, 50, 50);
        sub.started();
        assertFalse(parent.getStarted());
    }

    @Test
    public void testSubProgressBounds() {
        SimpleProgressListener parent = new SimpleProgressListener();

        parent.progress(50f);

        SubProgressListener sub = new SubProgressListener(parent, 50);

        sub.started();
        sub.progress(50f);

        assertEquals(50f, sub.getProgress(), 0f);
        assertEquals(75f, parent.getProgress(), 0f);

        sub.progress(100f);

        assertEquals(100f, sub.getProgress(), 0f);
        assertEquals(100f, parent.getProgress(), 0f);
    }

    private static class SimpleProgressListener implements ProgressListener {

        private float progress;
        private boolean startedCalled;

        public SimpleProgressListener() {
            this.startedCalled = false;
        }

        @Override
        public void progress(float percent) {
            this.progress = percent;
        }

        @Override
        public float getProgress() {
            return this.progress;
        }

        @Override
        public void complete() {}

        public boolean getStarted() {
            return this.startedCalled;
        }

        @Override
        public void started() {
            this.startedCalled = true;
        }

        @Override
        public void dispose() {}

        @Override
        public void exceptionOccurred(Throwable exception) {}

        public String getDescription() {
            return null;
        }

        @Override
        public InternationalString getTask() {
            return null;
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void setCanceled(boolean cancel) {}

        public void setDescription(String description) {}

        @Override
        public void setTask(InternationalString task) {}

        @Override
        public void warningOccurred(String source, String location, String warning) {}
    }
}
