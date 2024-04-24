package org.geoatlas.tile.vector.mapbox;

import no.ecc.vectortile.VectorTileEncoder;
import no.ecc.vectortile.VectorTileEncoderNoClip;
import org.geoatlas.tile.vector.VectorTileBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

/** repackage from org.geoserver.wms.mapbox.MapBoxTileBuilder
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 16:08
 * @since: 1.0
 **/
public class MapBoxTileBuilder implements VectorTileBuilder {

    private VectorTileEncoder encoder;
    private static final Logger log = LoggerFactory.getLogger(MapBoxTileBuilder.class);

    public MapBoxTileBuilder(Rectangle mapSize, ReferencedEnvelope mapArea) {
        final int extent = Math.max(mapSize.width, mapSize.height);
        final int polygonClipBuffer = extent / 32;
        final boolean autoScale = false;
        this.encoder = new VectorTileEncoderNoClip(extent, polygonClipBuffer, autoScale);
    }

    @Override
    public void addFeature(String layerName, String featureId, String geometryName,
                           Geometry geometry, Map<String, Object> properties) {
        int id = -1;
        if (featureId.matches(".*\\.[0-9]+")) {
            try {
                id = Integer.parseInt(featureId.split("\\.")[1]);
            } catch (NumberFormatException e) {
            }
        }

        if (id < 0) {
            log.warn("Cannot obtain numeric id from featureId: " + featureId);
        }

        encoder.addFeature(layerName, properties, geometry, id);
    }

    @Override
    public byte[] build() throws IOException {
        long start = System.currentTimeMillis();
        byte[] contents = this.encoder.encode();
        System.out.println("Vector Tile Codec 耗时：" + (System.currentTimeMillis() - start));
        return contents;
    }
}
