package no.ecc.vectortile;

import org.locationtech.jts.geom.Geometry;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 16:11
 * @since: 1.0
 **/
public class VectorTileEncoderNoClip extends VectorTileEncoder {

    public VectorTileEncoderNoClip(int extent, int polygonClipBuffer, boolean autoScale) {
        super(extent, polygonClipBuffer, autoScale);
    }

    /*
     * returns original geometry - no clipping. Assume upstream has already clipped!
     */
    @Override
    protected Geometry clipGeometry(Geometry geometry) {
        return geometry;
    }
}