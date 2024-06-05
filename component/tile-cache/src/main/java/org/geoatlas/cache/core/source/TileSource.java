package org.geoatlas.cache.core.source;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/5 14:26
 * @since: 1.0
 **/
public interface TileSource {
    /**
     * 即默认不使用MetaTiles
     */
    int[] META_TILING_FACTORS = {1,1};

    /**
     * The size of a metatile in tiles.
     *
     * @return the {x,y} metatiling factors
     */
    default int[] getMetaTilingFactors() {
        return META_TILING_FACTORS;
    }

    void seedTile(ConveyorTile tile, boolean tryCache)
            throws GeoAtlasCacheException, IOException;
}
