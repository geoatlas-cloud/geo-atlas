package org.geoatlas.tile.vector.mapbox;

import org.geoatlas.tile.vector.VectorTileBuilderFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;

import java.awt.*;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 16:16
 * @since: 1.0
 **/
public class MapBoxTileBuilderFactory implements VectorTileBuilderFactory {

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";
    public static final String LEGACY_MIME_TYPE = "application/x-protobuf;type=mapbox-vector";

    public static final Set<String> OUTPUT_FORMATS =
            Collections.unmodifiableSet(Stream.of(MIME_TYPE, LEGACY_MIME_TYPE, "pbf").collect(Collectors.toSet()));


    @Override
    public Set<String> getOutputFormats() {
        return OUTPUT_FORMATS;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public MapBoxTileBuilder newBuilder(Rectangle screenSize, ReferencedEnvelope mapArea) {
        return new MapBoxTileBuilder(screenSize, mapArea);
    }

    /**
     * For Mapbox tiles, since they are rendered in screen/tile space, oversampling produces more
     * consistent results when zooming. See this question here:
     *
     * <p>https://github.com/mapbox/vector-tiles/issues/45
     */
    @Override
    public boolean shouldOversampleScale() {
        return true;
    }

    /** Use 16x oversampling to match actual Mapbox tile extent, which is 4096 for 900913 tiles */
    @Override
    public int getOversampleX() {
        return 16;
    }

    /** Use 16x oversampling to match actual Mapbox tile extent, which is 4096 for 900913 tiles */
    @Override
    public int getOversampleY() {
        return 16;
    }
}
