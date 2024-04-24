package org.geoatlas.tile.vector;

import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.Map;

/** repackage from org.geoserver.wms.vector.VectorTileBuilder
 * <p>
 * Collects features into a vector tile
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 16:03
 * @since: 1.0
 **/
public interface VectorTileBuilder {

    /**
     * Add a feature to the tile
     *
     * @param layerName The name of the feature set
     * @param featureId The identifier of the feature within the feature set
     * @param geometryName The name of the geometry property
     * @param geometry The geometry value
     * @param properties The non-geometry attributes of the feature
     */
    void addFeature(
            String layerName,
            String featureId,
            String geometryName,
            Geometry geometry,
            Map<String, Object> properties);

    /**
     * Build the tile
     *
     * @return A WebMap containing the completed tile
     */
    byte[] build() throws IOException;
}
