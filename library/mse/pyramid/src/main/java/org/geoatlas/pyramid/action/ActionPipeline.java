package org.geoatlas.pyramid.action;

import com.google.common.base.Stopwatch;
import org.geoatlas.io.ByteArrayResource;
import org.geoatlas.pyramid.action.vector.AbstractReadAction;
import org.geoatlas.pyramid.action.vector.Pipeline;
import org.geoatlas.pyramid.action.vector.PipelineBuilder;
import org.geoatlas.pyramid.index.BoundingBox;
import org.geoatlas.pyramid.index.TileMatrix;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSetContext;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.geoatlas.tile.vector.mapbox.MapBoxTileBuilder;
import org.geoatlas.tile.vector.mapbox.MapBoxTileBuilderFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 13:40
 * @since: 1.0
 **/
public class ActionPipeline {

    private AbstractReadAction readAction;
    private static int DEFAULT_BUFFER_FACTOR = 6;

    // 1=no oversampling, 4=four time oversample (generialization will be 1/4 pixel)
    private double overSamplingFactor = 2.0;

    private boolean transformToScreenCoordinates = true;

    private boolean clipToMapBounds = true;

    private final MapBoxTileBuilderFactory tileBuilderFactory = new MapBoxTileBuilderFactory();
    private static final Logger log = LoggerFactory.getLogger(ActionPipeline.class);

    public ActionPipeline(AbstractReadAction readAction) {
        this.readAction = readAction;
    }

    /** Multiplies density of simplification from its base value. */
    public void setOverSamplingFactor(double factor) {
        this.overSamplingFactor = factor;
    }

    /**
     * Does this format use screen coordinates
     * @param useScreenCoords
     */
    public void setTransformToScreenCoordinates(boolean useScreenCoords) {
        this.transformToScreenCoordinates = useScreenCoords;
    }

    /** Does this format use features clipped to the extent of the tile instead of whole features */
    public void setClipToMapBounds(boolean clip) {
        this.clipToMapBounds = clip;
    }

    public TileObject doAction(TileRequest request, SimpleFeatureSource featureSource, CoordinateReferenceSystem forceDeclaredCrs) throws IOException {
        Stopwatch sw = Stopwatch.createStarted();
        ActionContext context = prepareContext(request, featureSource, forceDeclaredCrs);

        TileMatrixSet tileMatrixSet = context.getTileMatrixSet();
        TileMatrix matrix = tileMatrixSet.getMatrix((int) request.getZ());
        int mapWidth = matrix.getTileWidth();
        int mapHeight = matrix.getTileHeight();
        Rectangle paintArea = new Rectangle(mapWidth, mapHeight);
        if (this.tileBuilderFactory.shouldOversampleScale()) {
            paintArea = new Rectangle(this.tileBuilderFactory.getOversampleX() * mapWidth, this.tileBuilderFactory.getOversampleY() * mapHeight);
        }

        Pipeline pipeline = getPipeline(context.getTiledBbox(), paintArea, context.getSourceCrs(), featureSource.getSupportedHints(),
                context.getHints(), context.getBuffer_factor());
        if (Objects.isNull(pipeline)) {
            return null;
        }
        MapBoxTileBuilder vectorTileBuilder = this.tileBuilderFactory.newBuilder(paintArea, context.getTiledBbox());
        // read
        int count = 0;
        int total = 0;
        try (SimpleFeatureIterator featureIterator = readAction.doAction(context, featureSource)){

            // others -> pipeline
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                ++total;
                Geometry originalGeom = (Geometry) feature.getDefaultGeometryProperty().getValue();

                Geometry finalGeom;
                try {
                    finalGeom = pipeline.execute(originalGeom);
                } catch (Exception var26) {
                    log.warn(var26.getLocalizedMessage(), var26);
                    continue;
                }

                if (!finalGeom.isEmpty()) {
                    String layerName = feature.getType().getName().getLocalPart();
                    String featureId = feature.getIdentifier().toString();
                    // 无效操作, 予以注释
//                    String geometryName = geometryDescriptor.getName().getLocalPart();
                    Map<String, Object> properties = this.getProperties(feature);
                    vectorTileBuilder.addFeature(layerName, featureId, null, finalGeom, properties);
                    ++count;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sw.stop();
        String msg = String.format("Added %,d out of %,d features of '%s' in %s", count, total, request.getLayer(), sw);
        log.info(msg);
        byte[] rawTile = vectorTileBuilder.build();
        return TileObject.createCompleteTileObject(request, new ByteArrayResource(rawTile), this.tileBuilderFactory.getMimeType());
    }

    protected ActionContext prepareContext(TileRequest request, SimpleFeatureSource featureSource, CoordinateReferenceSystem forceDeclaredCrs) {
        ActionContext context = new ActionContext(request, DEFAULT_BUFFER_FACTOR);

        TileMatrixSet tileMatrixSet = TileMatrixSetContext.getTileMatrixSet(request.getSchema());
        if (tileMatrixSet == null) {
            throw new IllegalArgumentException("TileMatrixSet not found");
        }
        context.setTileMatrixSet(tileMatrixSet);

        BoundingBox boundingBox = tileMatrixSet.boundsFromIndex(new long[]{request.getX(), request.getY(), request.getZ()});
        Envelope envelope = new Envelope(boundingBox.getMinX(), boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxY());
        ReferencedEnvelope tiledBbox = new ReferencedEnvelope(envelope, tileMatrixSet.getCrs());
        context.setTiledBbox(ReferencedEnvelope.create(tiledBbox));

        GeometryDescriptor geometryDescriptor = featureSource.getSchema().getGeometryDescriptor();
        context.setGeometryDescriptor(geometryDescriptor);
        if (forceDeclaredCrs != null) {
            context.setSourceCrs(forceDeclaredCrs);
        }else {
            if (geometryDescriptor == null) {
                throw new RuntimeException("GeometryDescriptor is null");
            }
            CoordinateReferenceSystem dataCrs = geometryDescriptor.getType().getCoordinateReferenceSystem();
            context.setSourceCrs(dataCrs);
        }
        return context;
    }

    /**
     * @param renderingArea
     * @param paintArea
     * @param sourceCrs     数据源坐标系
     * @param fsHints
     * @param qHints
     * @param buffer
     * @return
     */
    protected Pipeline getPipeline(ReferencedEnvelope renderingArea, Rectangle paintArea, CoordinateReferenceSystem sourceCrs, Set<RenderingHints.Key> fsHints,
                                   Hints qHints, int buffer) {
        final Pipeline pipeline;
        try {
            PipelineBuilder builder = PipelineBuilder.newBuilder(renderingArea, paintArea, sourceCrs, this.overSamplingFactor, buffer);
            pipeline = builder
                    .preprocess()
                    .transform(this.transformToScreenCoordinates)
                    .clip(this.clipToMapBounds, this.transformToScreenCoordinates)
                    .simplify(this.transformToScreenCoordinates, fsHints, qHints)
                    .collapseCollections().build();
        } catch (FactoryException var10) {
            throw new RuntimeException(var10);
        }
        return pipeline;
    }

    private Map<String, Object> getProperties(ComplexAttribute feature) {
        Map<String, Object> props = new TreeMap<>();
        Iterator var3 = feature.getProperties().iterator();

        while (var3.hasNext()) {
            Property p = (Property) var3.next();
            if (p instanceof Attribute && !(p instanceof GeometryAttribute)) {
                String name = p.getName().getLocalPart();
                Object value;
                if (p instanceof ComplexAttribute) {
                    value = this.getProperties((ComplexAttribute) p);
                } else {
                    value = p.getValue();
                }

                if (value != null) {
                    props.put(name, value);
                }
            }
        }

        return props;
    }
}
