package org.geotools.renderer.lite;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.renderer.RenderListener;

public class CountingRenderListener implements RenderListener {
    public int features = 0;
    public int errors = 0;

    /* (non-Javadoc)
     * @see org.geotools.renderer.lite.RenderListener#featureRenderer(org.geotools.feature.Feature)
     */
    @Override
    public void featureRenderer(SimpleFeature feature) {
        features++;
    }

    /* (non-Javadoc)
     * @see org.geotools.renderer.lite.RenderListener#errorOccurred(java.lang.Exception)
     */
    @Override
    public void errorOccurred(Exception e) {
        errors++;
    }
}
