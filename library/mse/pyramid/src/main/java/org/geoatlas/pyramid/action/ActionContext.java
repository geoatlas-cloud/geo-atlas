package org.geoatlas.pyramid.action;

import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.tile.TileRequest;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.factory.Hints;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:48
 * @since: 1.0
 **/
public class ActionContext {

    private TileRequest request;

    private ReferencedEnvelope tiledBbox;

    private ReferencedEnvelope tiledExpandedBbox;

    private ReferencedEnvelope dataBbox;

    private CoordinateReferenceSystem sourceCrs;
    private TileMatrixSet tileMatrixSet;

    private int buffer_factor;

    private Hints hints;

    private GeometryDescriptor geometryDescriptor;

    public ActionContext(TileRequest request, int buffer_factor) {
        this.request = request;
        this.buffer_factor = buffer_factor;
    }

    public TileRequest getRequest() {
        return request;
    }

    public void setRequest(TileRequest request) {
        this.request = request;
    }

    public ReferencedEnvelope getTiledBbox() {
        return tiledBbox;
    }

    public void setTiledBbox(ReferencedEnvelope tiledBbox) {
        this.tiledBbox = tiledBbox;
    }

    public ReferencedEnvelope getTiledExpandedBbox() {
        return tiledExpandedBbox;
    }

    public void setTiledExpandedBbox(ReferencedEnvelope tiledExpandedBbox) {
        this.tiledExpandedBbox = tiledExpandedBbox;
    }

    public ReferencedEnvelope getDataBbox() {
        return dataBbox;
    }

    public void setDataBbox(ReferencedEnvelope dataBbox) {
        this.dataBbox = dataBbox;
    }

    public CoordinateReferenceSystem getSourceCrs() {
        return sourceCrs;
    }

    public void setSourceCrs(CoordinateReferenceSystem sourceCrs) {
        this.sourceCrs = sourceCrs;
    }

    public TileMatrixSet getTileMatrixSet() {
        return tileMatrixSet;
    }

    public void setTileMatrixSet(TileMatrixSet tileMatrixSet) {
        this.tileMatrixSet = tileMatrixSet;
    }

    public Hints getHints() {
        return hints;
    }

    public void setHints(Hints hints) {
        this.hints = hints;
    }

    public int getBuffer_factor() {
        return buffer_factor;
    }

    public void setBuffer_factor(int buffer_factor) {
        this.buffer_factor = buffer_factor;
    }

    public GeometryDescriptor getGeometryDescriptor() {
        return geometryDescriptor;
    }

    public void setGeometryDescriptor(GeometryDescriptor geometryDescriptor) {
        this.geometryDescriptor = geometryDescriptor;
    }
}
