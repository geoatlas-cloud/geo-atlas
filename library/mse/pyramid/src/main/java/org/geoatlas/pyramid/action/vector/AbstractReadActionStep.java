package org.geoatlas.pyramid.action.vector;

import org.geoatlas.pyramid.action.ActionChain;
import org.geoatlas.pyramid.action.ActionContext;
import org.geoatlas.pyramid.action.ActionStep;
import org.geoatlas.pyramid.index.BoundingBox;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSetContext;
import org.geoatlas.tile.TileRequest;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 18:13
 * @since: 1.0
 **/
public abstract class AbstractReadActionStep implements ActionStep {

    private static int DEFAULT_BUFFER_FACTOR = 6;

    private static final FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doAction(ActionContext context, ActionChain chain) throws IOException, SQLException {
        // 1. convert tile index to bbox
        // 2. convert bbox crs to origin data crs
        // 3. read data -> get SimpleFeatureCollection instance
        TileRequest request = context.getRequest();
        TileMatrixSet tileMatrixSet = TileMatrixSetContext.getTileMatrixSet(request.getSchema());
        if (tileMatrixSet == null) {
            throw new IllegalArgumentException("TileMatrixSet not found");
        }

        BoundingBox boundingBox = tileMatrixSet.boundsFromIndex(new long[]{request.getX(), request.getY(), request.getZ()});
        SimpleFeatureSource featureSource = context.getFeatureSource();
        GeometryDescriptor geometryDescriptor = featureSource.getSchema().getGeometryDescriptor();
        CoordinateReferenceSystem dataCrs = geometryDescriptor.getType().getCoordinateReferenceSystem();
        Envelope envelope = new Envelope(boundingBox.getMinX(), boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxY());
        if (!tileMatrixSet.getCrs().equals(dataCrs)) {
            MathTransform mathTransform = null;
            try {
                mathTransform = CRS.findMathTransform(dataCrs, tileMatrixSet.getCrs(), true).inverse();
                envelope = JTS.transform(envelope, mathTransform);
            } catch (FactoryException | TransformException e) {
                throw new RuntimeException(e);
            }
        }
        // 计算外扩缓存范围
        int buffer = DEFAULT_BUFFER_FACTOR * 1;
        // buffer is in pixels (style pixels), need to convert to paint area pixels
        String geometryPropertyName = geometryDescriptor.getLocalName();
        ReferencedEnvelope fBbox = new ReferencedEnvelope(envelope.getMinX() - buffer, envelope.getMaxX() + buffer,
                envelope.getMinY() - buffer, envelope.getMaxY() + buffer, null);


        Query query = buildQuery(context, fBbox, geometryPropertyName);
        SimpleFeatureCollection features = featureSource.getFeatures(query);
        context.setSimplifyReader(features.features());
    }

    protected Query buildQuery(ActionContext context, ReferencedEnvelope bbox, String geometryPropertyName) {
        Filter bboxFilter = filterFactory.bbox(filterFactory.property(geometryPropertyName), bbox);
        return new Query(context.getRequest().getLayer(), bboxFilter);
    }
}
