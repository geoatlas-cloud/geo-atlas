package org.geoatlas.pyramid.action.vector;

import org.geoatlas.pyramid.action.ActionContext;
import org.geoatlas.pyramid.index.BoundingBox;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSetContext;
import org.geoatlas.tile.TileRequest;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
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
public abstract class AbstractReadAction {

    protected static final FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public SimpleFeatureIterator doAction(ActionContext context, SimpleFeatureSource featureSource) throws IOException, SQLException {
        // 1. convert tile index to bbox
        // 2. convert bbox crs to origin data crs
        // 3. read data -> get SimpleFeatureCollection instance
        TileRequest request = context.getRequest();
        TileMatrixSet tileMatrixSet = TileMatrixSetContext.getTileMatrixSet(request.getSchema());
        if (tileMatrixSet == null) {
            throw new IllegalArgumentException("TileMatrixSet not found");
        }
        context.setTileMatrixSet(tileMatrixSet);

        BoundingBox boundingBox = tileMatrixSet.boundsFromIndex(new long[]{request.getX(), request.getY(), request.getZ()});
        Envelope envelope = new Envelope(boundingBox.getMinX(), boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxY());
        ReferencedEnvelope tiledBbox = new ReferencedEnvelope(envelope, tileMatrixSet.getCrs());
        context.setTiledBbox(ReferencedEnvelope.create(tiledBbox));

        // 计算外扩缓存范围, 默认外扩 6个 pixel size的距离, 后续可以考虑根据层级浮动
        int buffer = context.getBuffer_factor();
        double pixelSize = tileMatrixSet.getMatrix((int) request.getZ()).getResolution();
        tiledBbox.expandBy(buffer * pixelSize);
        context.setTiledExpandedBbox(tiledBbox);

        GeometryDescriptor geometryDescriptor = featureSource.getSchema().getGeometryDescriptor();
        CoordinateReferenceSystem dataCrs = geometryDescriptor.getType().getCoordinateReferenceSystem();
        context.setSourceCrs(dataCrs);

        ReferencedEnvelope finalBbox = ReferencedEnvelope.reference(tiledBbox);
        if (!CRS.equalsIgnoreMetadata(tileMatrixSet.getCrs(), dataCrs)) {
            try {
                finalBbox = finalBbox.transform(dataCrs, true);
            } catch (FactoryException | TransformException e) {
                throw new RuntimeException(e);
            }
        }
        context.setDataBbox(finalBbox);

        String geometryPropertyName = geometryDescriptor.getLocalName();
        Query query = buildQuery(request, finalBbox, geometryPropertyName);
        context.setHints(query.getHints());
        SimpleFeatureCollection features = featureSource.getFeatures(query);
        return features.features();
    }

    protected Query buildQuery(TileRequest request, ReferencedEnvelope bbox, String geometryPropertyName) {
        Filter bboxFilter = filterFactory.bbox(filterFactory.property(geometryPropertyName), bbox);
        return new Query(request.getLayer(), bboxFilter);
    }
}
