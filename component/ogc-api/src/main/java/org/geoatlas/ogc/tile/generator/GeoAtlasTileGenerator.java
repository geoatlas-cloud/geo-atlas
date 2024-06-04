package org.geoatlas.ogc.tile.generator;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.pyramid.index.OutsideCoverageException;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/3 22:43
 * @since: 1.0
 **/
public interface GeoAtlasTileGenerator {
    ConveyorTile generator(ConveyorTile tile)
            throws IOException, GeoAtlasCacheException, OutsideCoverageException;
}
