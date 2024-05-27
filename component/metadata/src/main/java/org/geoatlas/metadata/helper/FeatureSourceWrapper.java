package org.geoatlas.metadata.helper;

import org.geoatlas.metadata.model.PyramidRuleExpression;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.Collections;
import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/29 15:26
 * @since: 1.0
 **/
public final class FeatureSourceWrapper {
    private final SimpleFeatureSource featureSource;
    private final CoordinateReferenceSystem crs;

    private final List<PyramidRuleExpression> rules;

    public FeatureSourceWrapper(SimpleFeatureSource featureSource, CoordinateReferenceSystem crs, List<PyramidRuleExpression> rules) {
        this.featureSource = featureSource;
        this.crs = crs;
        if (rules == null){
            this.rules = Collections.EMPTY_LIST;
        }else {
            this.rules = Collections.unmodifiableList(rules);
        }
    }

    public SimpleFeatureSource getFeatureSource() {
        return featureSource;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public List<PyramidRuleExpression> getRules() {
        return rules;
    }
}
