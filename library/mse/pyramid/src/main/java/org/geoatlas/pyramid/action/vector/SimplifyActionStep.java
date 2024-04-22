package org.geoatlas.pyramid.action.vector;

import org.geoatlas.pyramid.action.ActionChain;
import org.geoatlas.pyramid.action.ActionContext;
import org.geoatlas.pyramid.action.ActionStep;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.locationtech.jts.util.Stopwatch;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:43
 * @since: 1.0
 **/
public class SimplifyActionStep implements ActionStep {

    private final double simplificationDistanceTolerance;
    private final Logger log = LoggerFactory.getLogger(SimplifyActionStep.class);

    public SimplifyActionStep(double simplificationDistanceTolerance) {
        this.simplificationDistanceTolerance = simplificationDistanceTolerance;
    }

    @Override
    public void doAction(ActionContext context, ActionChain chain) throws IOException, SQLException {
        if (simplificationDistanceTolerance > 0.0) {
            SimpleFeatureIterator simplifyReader = context.getSimplifyReader();
            if (Objects.nonNull(simplifyReader)) {
                Stopwatch sw = new Stopwatch();
                int count = 0;
                int total = 0;
                String layer = context.getFeatureSource().getName().toString();

                DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(layer, context.getFeatureSource().getSchema());

                try {
                    while (simplifyReader.hasNext()) {
                        SimpleFeature feature = simplifyReader.next();
                        ++total;
                        Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
                        if (!(geometry instanceof Point)) {
                            if (geometry instanceof LineString || geometry instanceof MultiLineString) {
                                geometry = DouglasPeuckerSimplifier.simplify(geometry, simplificationDistanceTolerance);
                            } else if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                                Geometry simplified = DouglasPeuckerSimplifier.simplify(geometry, simplificationDistanceTolerance);
                                // extra check to prevent polygon converted to line
                                if (simplified instanceof Polygon || simplified instanceof MultiPolygon) {
                                    geometry = simplified;
                                } else {
                                    geometry = TopologyPreservingSimplifier.simplify(geometry, simplificationDistanceTolerance);
                                }
                            } else {
                                geometry = TopologyPreservingSimplifier.simplify(geometry, simplificationDistanceTolerance);
                            }
                        }

                        if (!geometry.isEmpty()) {
                            feature.getDefaultGeometryProperty().setValue(geometry);
                            featureCollection.add(feature);
                            ++count;
                        }
                    }
                } finally {
                    simplifyReader.close();
                }
                context.setContent(featureCollection);
                sw.stop();
                String msg = String.format("Added %,d out of %,d features of '%s' in %s", count, total, layer, sw);
                log.info(msg);
            }
        }

    }
}
