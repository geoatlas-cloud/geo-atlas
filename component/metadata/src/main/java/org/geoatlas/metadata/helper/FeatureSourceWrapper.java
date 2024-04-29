package org.geoatlas.metadata.helper;

import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/29 15:26
 * @since: 1.0
 **/
public final class FeatureSourceWrapper {
    private final SimpleFeatureSource featureSource;
    private final CoordinateReferenceSystem crs;

    public FeatureSourceWrapper(SimpleFeatureSource featureSource, CoordinateReferenceSystem crs) {
        this.featureSource = featureSource;
        this.crs = crs;
    }

    public SimpleFeatureSource getFeatureSource() {
        return featureSource;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }
}
