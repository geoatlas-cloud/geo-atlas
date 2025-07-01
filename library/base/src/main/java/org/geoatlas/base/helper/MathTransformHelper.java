package org.geoatlas.base.helper;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 14:39
 * @since: 1.0
 **/
public class MathTransformHelper {

    /**
     * note: copy from org.geotools.renderer.lite.VectorMapRenderUtils#buildTransform
     *
     * <p>
     * Builds the transform from sourceCRS to destCRS/
     *
     * <p>Although we ask for 2D content (via {@link Hints#FEATURE_2D} ) not all DataStore
     * implementations are capable. With that in mind if the provided soruceCRS is not 2D we are
     * going to manually post-process the Geomtries into {@link DefaultGeographicCRS#WGS84} - and
     * the {@link MathTransform2D} returned here will transition from WGS84 to the requested
     * destCRS.
     *
     * @return the transform from {@code sourceCRS} to {@code destCRS}, will be an identity
     *     transform if the the two crs are equal
     * @throws FactoryException If no transform is available to the destCRS
     */
    public static MathTransform buildTransform(
            CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem destCRS)
            throws FactoryException {
        if (sourceCRS == null) {
            throw new NullPointerException("sourceCRS");
        }
        if (destCRS == null) {
            throw new NullPointerException("destCRS");
        }

        MathTransform transform = null;
        if (sourceCRS.getCoordinateSystem().getDimension() >= 3) {
            // We are going to transform over to DefaultGeographic.WGS84 on the fly
            // so we will set up our math transform to take it from there
            MathTransform toWgs84_3d =
                    CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84_3D);
            MathTransform toWgs84_2d =
                    CRS.findMathTransform(
                            DefaultGeographicCRS.WGS84_3D, DefaultGeographicCRS.WGS84);
            transform = ConcatenatedTransform.create(toWgs84_3d, toWgs84_2d);
            sourceCRS = DefaultGeographicCRS.WGS84;
        }

        // the basic crs transformation, if any
        MathTransform2D sourceToTarget =
                (MathTransform2D) CRS.findMathTransform(sourceCRS, destCRS, true);

        if (transform == null) {
            return sourceToTarget;
        }
        if (sourceToTarget.isIdentity()) {
            return transform;
        }
        return ConcatenatedTransform.create(transform, sourceToTarget);
    }

}
