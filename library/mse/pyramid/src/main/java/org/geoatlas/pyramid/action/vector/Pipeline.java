package org.geoatlas.pyramid.action.vector;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * repackage from org.geoserver.wms.vector.Pipeline
 *
 * A chainable unary operation on a geometry.
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 14:18
 * @since: 1.0
 **/
public abstract class Pipeline {

    protected static final Geometry EMPTY = new GeometryFactory().createPoint((Coordinate) null);

    /** Pipeline terminator which returns the geometry without change. */
    static final Pipeline END =
            new Pipeline() {

                @Override
                public final Geometry execute(Geometry geom) {
                    return geom;
                }

                @Override
                protected final Geometry _run(Geometry geom) {
                    throw new UnsupportedOperationException();
                }
            };

    private Pipeline next = END;

    /** Set the next operation in the pipeline */
    void setNext(Pipeline step) {
        if (next == null) {
            throw new NullPointerException();
        }
        this.next = step;
    }

    /** Execute pipeline including all downstream pipelines. */
    public Geometry execute(Geometry geom) throws Exception {
        if (next == null) {
            throw new NullPointerException(getClass().getName());
        }
        Geometry g = _run(geom);
        if (g == null || g.isEmpty()) {
            return EMPTY;
        }
        return next.execute(g);
    }

    /** Implementation of the pipeline. A unary operation on a geometry. */
    protected abstract Geometry _run(Geometry geom) throws Exception;
}
